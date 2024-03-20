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
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static java.time.ZoneId.systemDefault;
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
    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;


    private final JurorPoolRepository jurorPoolRepository;

    private final JurorRepository jurorRepository;


    @Autowired
    public PublicAuthenticationServiceImpl(final JurorDigitalResponseRepositoryMod jurorResponseRepository,
                                           final JurorPoolRepository jurorPoolRepository,
                                           final JurorRepository jurorRepository) {

        Assert.notNull(jurorResponseRepository, "JurorDigitalResponseRepositoryMod cannot be null");
        Assert.notNull(jurorPoolRepository, "JurorPoolRepository cannot be null");
        Assert.notNull(jurorRepository, "JurorRepository cannot be null");
        this.jurorResponseRepository = jurorResponseRepository;
        this.jurorPoolRepository = jurorPoolRepository;
        this.jurorRepository = jurorRepository;
    }

    /**
     * @throws InvalidJurorCredentialsException Credentials do not match persistence
     */
    @Override
    @Transactional(noRollbackFor = InvalidJurorCredentialsException.class)
    public PublicAuthenticationResponseDto authenticationJuror(final PublicAuthenticationRequestDto credentials) {
        log.debug("Authenticating juror with {}", credentials);

        try {

            Optional<JurorPool> optJurorPool =
                jurorPoolRepository.findOne(QJurorPool.jurorPool.juror.jurorNumber.eq(credentials.getJurorNumber()));


            final JurorPool jurorAuthentication = optJurorPool.orElse(null);

            // validate credentials.
            if (jurorAuthentication == null) {
                log.info("Could not find juror using credentials {}", credentials);
                log.debug("Juror may not be active");
                throw new InvalidJurorCredentialsException("Bad credentials");
            }
            //auth checks
            final String locCode =
                jurorAuthentication.getCourt() != null ? jurorAuthentication.getCourt().getLocCode() : null;
            if (jurorAuthentication.getJuror().isLocked()) {
                log.debug("Account locked: {}", jurorAuthentication.getJurorNumber());
                throw new JurorAccountBlockedException("Juror account is locked");
            } else if (!isValidCredentials(jurorAuthentication, credentials)) {
                log.debug("Credentials do not match");
                saveFailedLoginAttempts(jurorAuthentication);
                throw new InvalidJurorCredentialsException("Invalid credentials");
            } else if ((jurorAuthentication.getStatus().getStatus() != 1)) {
                log.debug(JUROR_ALREADY_RESPONDED);
                throw new JurorAlreadyRespondedException(JUROR_ALREADY_RESPONDED);
            } else if (jurorResponseRepository.findByJurorNumber(jurorAuthentication.getJurorNumber()) != null) {
                log.debug(JUROR_ALREADY_RESPONDED);
                throw new JurorAlreadyRespondedException(JUROR_ALREADY_RESPONDED);
            } else if (!isFutureHearingDate(jurorAuthentication)) {
                log.debug("Court Date {} has passed", jurorAuthentication.getNextDate());
                throw new CourtDateLapsedException("Not allowed. Court Date has already passed");
            } else {
                log.info("Juror {} is valid for authentication", jurorAuthentication.getJurorNumber());
                clearFailedLoginAttempts(jurorAuthentication.getJurorNumber());
            }

            // juror is ok to authenticate
            log.debug("Juror {} passed authentication checks", jurorAuthentication.getJurorNumber());
            return PublicAuthenticationResponseDto.builder()
                .jurorNumber(jurorAuthentication.getJurorNumber())
                .firstName(jurorAuthentication.getJuror().getFirstName())
                .lastName(jurorAuthentication.getJuror().getLastName())
                .postcode(jurorAuthentication.getJuror().getPostcode())
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
    public void saveFailedLoginAttempts(final JurorPool jurorAuthentication) {
        final String jurorNumber = jurorAuthentication.getJurorNumber();

        log.debug("Find previous failed login attempt counter for juror {}", jurorNumber);
        final Juror attempt = jurorRepository.findByJurorNumber(jurorNumber);
        if (attempt != null) {
            // increment failed login counter
            Integer loginAttempts = attempt.getLoginAttempts();
            if (loginAttempts == null) {
                loginAttempts = 1;// initialize to this failed attempt
            } else {
                loginAttempts++;
                attempt.setLoginAttempts(loginAttempts);
                log.debug("Incrementing juror {} failed login attempts to {}", jurorNumber, loginAttempts);
            }
            jurorRepository.save(attempt);

            if (loginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                log.debug("Locking juror for reaching 3 failed attempts {}", jurorNumber);
                jurorAuthentication.getJuror().setLocked(true);

                if (log.isTraceEnabled()) {
                    log.trace("Locking {}", jurorAuthentication);
                }

                jurorPoolRepository.save(jurorAuthentication);
                log.warn("Locked juror {}", jurorNumber);

                // reset the counter for failed login attempts now the account is locked.
                clearFailedLoginAttempts(jurorNumber);
            } else {
                log.debug("Juror {} has {}/{} failed login attempts!", jurorNumber, attempt.getLoginAttempts(),
                    MAX_FAILED_LOGIN_ATTEMPTS
                );
                jurorRepository.save(attempt);
                log.debug("Juror {} login attempts updated.", jurorNumber);
            }
        } else {
            log.debug("No previous failed attempts for juror {}, creating it.", jurorNumber);
            // loginAttemptRepository.save(new LoginAttempt(jurorAuthentication.getJurorNumber(), 1));
            //  jurorRepository.save(new Juror(jurorAuthentication.getJurorNumber(),String ));
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
        final Juror attempt = jurorRepository.findByJurorNumber(jurorNumber);
        if (attempt != null) {
            attempt.setLoginAttempts(0);
            jurorRepository.save(attempt);
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
    private boolean isValidCredentials(final JurorPool databaseCredential,
                                       final PublicAuthenticationRequestDto userCredentials) {

        //normalize postcodes
        log.trace("DB raw    postcode: {}", databaseCredential.getJuror().getPostcode());
        log.trace("USER raw  postcode: {}", userCredentials.getPostcode());
        final String dbPostcode = databaseCredential.getJuror().getPostcode()
            .replaceAll(WHITESPACE_MATCHER, "")
            .toLowerCase();
        log.trace("DB norm   postcode: {}", dbPostcode);
        final String userPostcode = userCredentials.getPostcode()
            .replaceAll(WHITESPACE_MATCHER, "")
            .toLowerCase();
        log.trace("USER norm postcode: {}", userPostcode);

        // normalize last names
        log.trace("DB raw    lastName: {}", databaseCredential.getJuror().getLastName());
        log.trace("USER raw  lastName: {}", userCredentials.getLastName());
        final String dbLastName = databaseCredential.getJuror().getLastName()
            .toLowerCase();
        log.trace("DB norm   lastName: {}", dbLastName);
        final String userLastName = userCredentials.getLastName()
            .trim()
            .toLowerCase();
        log.trace("USER norm lastName: {}", userLastName);

        // if either do not match then credentials are invalid
        return dbLastName.equalsIgnoreCase(userLastName) && dbPostcode.equalsIgnoreCase(userPostcode);
    }


    private boolean isFutureHearingDate(final JurorPool authentication) {

        LocalDateTime now = Instant.now().atZone(systemDefault()).toLocalDateTime();
        LocalDate hearingDate = authentication.getNextDate();
        Instant hearingDateInstant = hearingDate.atStartOfDay(systemDefault()).toInstant();
        LocalDateTime courtDate = LocalDateTime.ofInstant((hearingDateInstant), ZoneId.systemDefault());
        //    LocalDateTime courtDate = LocalDateTime.ofInstant(authentication.getJuror().getHearingDate().toInstant
        //    (),ZoneId.systemDefault());
        //  LocalDateTime courtDate = LocalDateTime.ofInstant(authentication.getNextDate().atStartOfDay(ZoneId
        //  .systemDefault()).toInstant(),ZoneId.systemDefault());
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
