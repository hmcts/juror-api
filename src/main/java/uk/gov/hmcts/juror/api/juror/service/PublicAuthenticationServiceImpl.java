package uk.gov.hmcts.juror.api.juror.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.juror.controller.PublicAuthenticationController.PublicAuthenticationRequestDto;
import uk.gov.hmcts.juror.api.juror.controller.PublicAuthenticationController.PublicAuthenticationResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.service.JurorServiceModImpl;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.WHITESPACE_MATCHER;

/**
 * Implementation for Authentication service interface for public authentication operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PublicAuthenticationServiceImpl implements PublicAuthenticationService {
    private static final String JUROR_ROLE = "juror";
    private static final String JUROR_ALREADY_RESPONDED = "Juror already responded";
    private static final Integer MAX_FAILED_LOGIN_ATTEMPTS = 3;

    private static final long LOCK_TIME_DURATION = 30 * 60 * 1000; // 1 minutes
    private final JurorPoolRepository jurorPoolRepository;
    private final JurorRepository jurorRepository;
    private final JurorServiceModImpl jurorServiceModImpl;
    private final JurorResponseServiceImpl jurorResponseServiceImpl;


    /**
     * Authenticate a juror using the supplied credentials.
     *
     * @throws InvalidJurorCredentialsException Credentials do not match persistence
     */
    @Override
    @Transactional(noRollbackFor = InvalidJurorCredentialsException.class)
    public PublicAuthenticationResponseDto authenticationJuror(final PublicAuthenticationRequestDto credentials) {
        log.debug("Authenticating juror with {}", credentials);

        try {
            if (jurorResponseServiceImpl.getCommonJurorResponseOptional(credentials.getJurorNumber()).isPresent()) {
                log.debug(JUROR_ALREADY_RESPONDED);
                throw new JurorAlreadyRespondedException(JUROR_ALREADY_RESPONDED);
            }

            Juror juror = jurorServiceModImpl.getJurorOptionalFromJurorNumber(credentials.getJurorNumber())
                .orElseThrow(() -> {
                    log.info("Could not find juror {} using credentials supplied", credentials.getJurorNumber());
                    log.debug("Juror {} using credentials {}", credentials.getJurorNumber(), credentials);
                    log.debug("Juror may not be active");
                    return new InvalidJurorCredentialsException("Bad credentials");
                });

            if (juror.isLocked()) {
                log.info("Checking account can be unlocked for juror {}",credentials.getJurorNumber());
                if (unlockWhenTimePassed(juror)) {
                    log.info("Juror account unlocked for {}", credentials.getJurorNumber());
                } else {
                    log.debug("Account locked: {}", credentials.getJurorNumber());
                    throw new JurorAccountBlockedException("Juror account is locked");
                }
            } else if (!isValidCredentials(juror, credentials)) {
                log.debug("Credentials do not match");
                saveFailedLoginAttempts(juror);
                throw new InvalidJurorCredentialsException("Invalid credentials");
            }

            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndStatusStatusAndIsActive(
                    credentials.getJurorNumber(), IJurorStatus.SUMMONED, true)
                .orElseThrow(() -> {
                    log.debug(JUROR_ALREADY_RESPONDED);
                    return new JurorAlreadyRespondedException(JUROR_ALREADY_RESPONDED);
                });

            if (!isFutureHearingDate(jurorPool)) {
                log.debug("Court Date {} has passed", jurorPool.getNextDate());
                throw new CourtDateLapsedException("Not allowed. Court Date has already passed");
            } else {
                log.info("Juror {} is valid for authentication", credentials.getJurorNumber());
                clearFailedLoginAttempts(juror);
            }

            // juror is ok to authenticate
            log.debug("Juror {} passed authentication checks", juror.getJurorNumber());
            return PublicAuthenticationResponseDto.builder()
                .jurorNumber(juror.getJurorNumber())
                .firstName(juror.getFirstName())
                .lastName(juror.getLastName())
                .postcode(juror.getPostcode())
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
     * @param juror The juror account associated with the fail
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveFailedLoginAttempts(final Juror juror) {
        // increment failed login counter
        int loginAttempts = juror.getLoginAttempts();
        loginAttempts++;
        juror.setLoginAttempts(loginAttempts);
        log.debug("Incrementing juror {} failed login attempts to {}", juror.getJurorNumber(), loginAttempts);
        jurorRepository.save(juror);

        if (loginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            log.debug("Locking juror for reaching 3 failed attempts {}", juror.getJurorNumber());
            juror.setLocked(true);
            juror.setLockTime(LocalDateTime.now());

            if (log.isTraceEnabled()) {
                log.trace("Locking {}", juror.getJurorNumber());
            }
            jurorRepository.save(juror);
            log.warn("Locked juror {}", juror.getJurorNumber());

            // reset the counter for failed login attempts now the account is locked.
            clearFailedLoginAttempts(juror);
        } else {
            log.debug("Juror {} has {}/{} failed login attempts!", juror.getJurorNumber(), juror.getLoginAttempts(),
                MAX_FAILED_LOGIN_ATTEMPTS
            );
            jurorRepository.save(juror);
            log.debug("Juror {} login attempts updated.", juror.getJurorNumber());
        }
    }

    /**
     * Clear the count of failed login attempts for the juror. This is invoked after a successful authentication flow.
     *
     * @param juror The juror account associated with the success
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void clearFailedLoginAttempts(Juror juror) {
        log.debug("Clearing failed login attempt counter for juror {}", juror.getJurorNumber());
        juror.setLoginAttempts(0);
        jurorRepository.save(juror);
        log.debug("Juror {} login attempts cleared.", juror.getJurorNumber());
    }

    public boolean unlockWhenTimePassed(Juror juror) {
        if (juror.isLocked()) {
            long lockTime = juror.getLockTime().toInstant(ZoneOffset.UTC).toEpochMilli();
            long currentTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
            if (currentTime - lockTime >= LOCK_TIME_DURATION) {
                juror.setLocked(false);
                juror.setLockTime(null);
                jurorRepository.save(juror);
                return true;
            }
        }
        return false;
    }

    /**
     * Compare user supplied credentials with database values.
     *
     * @param databaseCredential Database supplied values
     * @param userCredentials    User supplied values for comparison
     * @return Values supplied match the stored values for authentication.
     */
    private boolean isValidCredentials(final Juror databaseCredential,
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


    private boolean isFutureHearingDate(final JurorPool authentication) {
        return authentication.getNextDate().isAfter(LocalDate.now());
    }

    /**
     * Failed to validate juror credentials.
     * Created by jonny on 24/03/17.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class InvalidJurorCredentialsException extends RuntimeException {
        public InvalidJurorCredentialsException(String message) {
            super(message);
        }
    }

    /**
     * Juror account is blocked.
     * Created by jonny on 24/03/17.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class JurorAccountBlockedException extends RuntimeException {
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
    public static class CourtDateLapsedException extends RuntimeException {
        public CourtDateLapsedException(String message) {
            super(message);
        }
    }
}
