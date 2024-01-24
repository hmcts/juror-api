package uk.gov.hmcts.juror.api.juror.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.juror.controller.PublicAuthenticationController.PublicAuthenticationRequestDto;
import uk.gov.hmcts.juror.api.juror.controller.PublicAuthenticationController.PublicAuthenticationResponseDto;
import uk.gov.hmcts.juror.api.juror.domain.CourtWhitelistRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.LoginAttempt;
import uk.gov.hmcts.juror.api.juror.domain.LoginAttemptRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolExtend;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.QPool;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.WHITESPACE_MATCHER;

/**
 * Implementation for Authentication service interface for public authentication operations.
 */
@Service
@Slf4j
public class PublicAuthenticationServiceImpl implements PublicAuthenticationService {
    private static final String JUROR_ROLE = "juror";
    private static final String JUROR_ALREADY_RESPONDED = "Juror already responded";
    private static final Integer MAX_FAILED_LOGIN_ATTEMPTS = 3;
    private final CourtWhitelistRepository courtWhitelistRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final JurorResponseRepository jurorResponseRepository;
    private final PoolRepository poolRepository;

    @Autowired
    public PublicAuthenticationServiceImpl(final CourtWhitelistRepository courtWhitelistRepository,
                                           final LoginAttemptRepository loginAttemptRepository,
                                           final JurorResponseRepository jurorResponseRepository,
                                           final PoolRepository poolRepository) {
        Assert.notNull(courtWhitelistRepository, "CourtWhitelistRepository cannot be null");
        Assert.notNull(loginAttemptRepository, "LoginAttemptRepository cannot be null");
        Assert.notNull(jurorResponseRepository, "JurorResponseRepository cannot be null");
        Assert.notNull(poolRepository, "PoolRepository cannot be null");
        this.courtWhitelistRepository = courtWhitelistRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.jurorResponseRepository = jurorResponseRepository;
        this.poolRepository = poolRepository;
    }

    /**
     * @throws InvalidJurorCredentialsException Credentials do not match persistence
     */
    @Override
    @Transactional(noRollbackFor = InvalidJurorCredentialsException.class)
    public PublicAuthenticationResponseDto authenticationJuror(final PublicAuthenticationRequestDto credentials) {
        log.debug("Authenticating juror with {}", credentials);

        try {
            //final Pool jurorAuthentication = poolRepository.findOne(credentials.getJurorNumber());
            Optional<Pool> optPool = poolRepository.findOne(QPool.pool.jurorNumber.eq(credentials.getJurorNumber()));
            final Pool jurorAuthentication = optPool.isPresent()
                ?
                optPool.get()
                :
                    null;

            // validate credentials.
            if (jurorAuthentication == null) {
                log.info("Could not find juror using credentials {}", credentials);
                log.debug("Juror may not be active");
                throw new InvalidJurorCredentialsException("Bad credentials");
            }

            //auth checks
            final String locCode = jurorAuthentication.getCourt() != null
                ?
                jurorAuthentication.getCourt().getLocCode()
                :
                    null;
            if (jurorAuthentication.getPoolExtend() != null && jurorAuthentication.getPoolExtend().getIsLocked()) {
                log.debug("Account locked: {}", jurorAuthentication.getJurorNumber());
                throw new JurorAccountBlockedException("Juror account is locked");
            } else if (!isValidCredentials(jurorAuthentication, credentials)) {
                log.debug("Credentials do not match");
                saveFailedLoginAttempts(jurorAuthentication);
                throw new InvalidJurorCredentialsException("Invalid credentials");
            } else if ((jurorAuthentication.getStatus() != 1)) {
                log.debug(JUROR_ALREADY_RESPONDED);
                throw new JurorAlreadyRespondedException(JUROR_ALREADY_RESPONDED);
            } else if (jurorResponseRepository.findByJurorNumber(jurorAuthentication.getJurorNumber()) != null) {
                log.debug(JUROR_ALREADY_RESPONDED);
                throw new JurorAlreadyRespondedException(JUROR_ALREADY_RESPONDED);
            } else if (!isFutureHearingDate(jurorAuthentication)) {
                log.debug("Court Date {} has passed", jurorAuthentication.getHearingDate());
                throw new CourtDateLapsedException("Not allowed. Court Date has already passed");
            } else if (courtWhitelistRepository.findByLocCode(locCode) == null) {
                log.debug("Court location {} is not whitelisted as allowing responses", locCode);
                throw new InvalidCourtLocationException("Court location is not allowed");
            } else {
                log.info("Juror {} is valid for authentication", jurorAuthentication.getJurorNumber());
                clearFailedLoginAttempts(jurorAuthentication.getJurorNumber());
            }

            // juror is ok to authenticate
            log.debug("Juror {} passed authentication checks", jurorAuthentication.getJurorNumber());
            return PublicAuthenticationResponseDto.builder()
                .jurorNumber(jurorAuthentication.getJurorNumber())
                .firstName(jurorAuthentication.getFirstName())
                .lastName(jurorAuthentication.getLastName())
                .postcode(jurorAuthentication.getPostcode())
                .roles(Collections.singletonList(JUROR_ROLE))
                .build();
        } catch (DataAccessException dae) {
            log.error("Failed to retrieve juror information from persistence: {}", dae.getMessage());
            throw dae;
        }
    }

    /**
     * Update the count of failed login attempts for the juror.  This is invoked as part of an invalid credentials flow.
     *
     * @param jurorAuthentication The juror account associated with the fail
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveFailedLoginAttempts(final Pool jurorAuthentication) {
        final String jurorNumber = jurorAuthentication.getJurorNumber();

        log.debug("Find previous failed login attempt counter for juror {}", jurorNumber);
        final LoginAttempt attempt = loginAttemptRepository.findByUsername(jurorNumber);
        if (attempt != null) {
            // increment failed login counter
            Integer loginAttempts = attempt.getLoginattempts();
            if (loginAttempts == null) {
                loginAttempts = 1;// initialize to this failed attempt
            } else {
                loginAttempts++;
                attempt.setLoginattempts(loginAttempts);
                log.debug("Incrementing juror {} failed login attempts to {}", jurorNumber, loginAttempts);
            }
            loginAttemptRepository.save(attempt);

            if (loginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                log.debug("Locking juror for reaching 3 failed attempts {}", jurorNumber);
                // lock the user
                if (jurorAuthentication.getPoolExtend() == null) {
                    jurorAuthentication.setPoolExtend(PoolExtend.builder()
                        .jurorNumber(jurorAuthentication.getJurorNumber())
                        .isLocked(Boolean.TRUE)
                        .build());
                } else {
                    jurorAuthentication.getPoolExtend().setIsLocked(Boolean.TRUE);
                }
                if (log.isTraceEnabled()) {
                    log.trace("Locking {}", jurorAuthentication);
                }

                poolRepository.save(jurorAuthentication);
                log.warn("Locked juror {}", jurorNumber);

                // reset the counter for failed login attempts now the account is locked.
                clearFailedLoginAttempts(jurorNumber);
            } else {
                log.debug("Juror {} has {}/{} failed login attempts!", jurorNumber, attempt.getLoginattempts(),
                    MAX_FAILED_LOGIN_ATTEMPTS
                );
                loginAttemptRepository.save(attempt);
                log.debug("Juror {} login attempts updated.", jurorNumber);
            }
        } else {
            log.debug("No previous failed attempts for juror {}, creating it.", jurorNumber);
            loginAttemptRepository.save(new LoginAttempt(jurorAuthentication.getJurorNumber(), 1));
        }
    }

    /**
     * Clear the count of failed login attempts for the juror. This is invoked after a successful authentication flow.
     *
     * @param jurorNumber The juror account associated with the success
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void clearFailedLoginAttempts(String jurorNumber) {
        log.debug("Clearing failed login attempt counter for juror {}", jurorNumber);
        final LoginAttempt attempt = loginAttemptRepository.findByUsername(jurorNumber);
        if (attempt != null) {
            loginAttemptRepository.delete(attempt);
            log.debug("Juror {} login attempts cleared.", jurorNumber);
        } else {
            log.debug("No previous failed attempts to clear for juror {}", jurorNumber);
        }
    }

    /**
     * Compare user supplied credentials with database values.
     *
     * @param databaseCredential Database supplied values
     * @param userCredentials    User supplied values for comparison
     * @return Values supplied match the stored values for authentication.
     */
    private boolean isValidCredentials(final Pool databaseCredential,
                                       final PublicAuthenticationRequestDto userCredentials) {

        //normalize postcodes
        log.trace("DB raw    postcode: {}", databaseCredential.getPostcode());
        log.trace("USER raw  postcode: {}", userCredentials.getPostcode());
        final String dbPostcode = databaseCredential.getPostcode()
            .replaceAll(WHITESPACE_MATCHER, "")
            .toLowerCase();
        log.trace("DB norm   postcode: {}", dbPostcode);
        final String userPostcode = userCredentials.getPostcode()
            .replaceAll(WHITESPACE_MATCHER, "")
            .toLowerCase();
        log.trace("USER norm postcode: {}", userPostcode);

        // normalize last names
        log.trace("DB raw    lastName: {}", databaseCredential.getLastName());
        log.trace("USER raw  lastName: {}", userCredentials.getLastName());
        final String dbLastName = databaseCredential.getLastName()
            .toLowerCase();
        log.trace("DB norm   lastName: {}", dbLastName);
        final String userLastName = userCredentials.getLastName()
            .trim()
            .toLowerCase();
        log.trace("USER norm lastName: {}", userLastName);

        // if either do not match then credentials are invalid
        return dbLastName.equalsIgnoreCase(userLastName) && dbPostcode.equalsIgnoreCase(userPostcode);
    }


    private boolean isFutureHearingDate(final Pool authentication) {

        LocalDateTime now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime courtDate = LocalDateTime.ofInstant(
            authentication.getHearingDate().toInstant(),
            ZoneId.systemDefault()
        );
        return courtDate.isAfter(now);
    }

    /**
     * Failed to validate juror credentials.
     * Created by jonny on 24/03/17.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public class InvalidJurorCredentialsException extends RuntimeException {
        public InvalidJurorCredentialsException(String message) {
            super(message);
        }
    }

    /**
     * Juror account is blocked.
     * Created by jonny on 24/03/17.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public class JurorAccountBlockedException extends RuntimeException {
        public JurorAccountBlockedException(String message) {
            super(message);
        }
    }

    /**
     * Juror already responded.
     * Created by jonny on 24/03/17.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class JurorAlreadyRespondedException extends RuntimeException {
        public JurorAlreadyRespondedException() {
        }

        public JurorAlreadyRespondedException(String message) {
            super(message);
        }
    }

    /**
     * Court Date has passed.
     * Created by schohan on 19/03/20.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public class CourtDateLapsedException extends RuntimeException {
        public CourtDateLapsedException(String message) {
            super(message);
        }
    }

    /**
     * Court location is not allowed to respond.
     * Created by jonny on 24/03/17.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public class InvalidCourtLocationException extends RuntimeException {
        public InvalidCourtLocationException(String message) {
            super(message);
        }
    }
}
