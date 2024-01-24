package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.controller.BureauAuthenticationController;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameter;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameterRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Implementation for Authentication service interface for bureau authentication operations.
 */
@Service
@Slf4j
public class BureauAuthenticationServiceImpl implements BureauAuthenticationService {
    /**
     * Maximum failed attempts before an account should be disabled.
     */
    private static final Integer MAX_FAILED_LOGIN_ATTEMPTS = 3;
    private final SystemParameterRepository systemParameterRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Autowired
    public BureauAuthenticationServiceImpl(final SystemParameterRepository systemParameterRepository,
                                           final UserRepository userRepository,
                                           final Clock clock) {
        Assert.notNull(systemParameterRepository, "PasswordSettingRepository cannot be null.");
        Assert.notNull(userRepository, "UserRepository cannot be null.");
        this.systemParameterRepository = systemParameterRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(noRollbackFor = InvalidBureauCredentialsException.class)
    public BureauAuthenticationController.BureauAuthenticationResponseDto authenticateBureauOfficer(
        BureauAuthenticationController.BureauAuthenticationRequestDto authenticationRequest) {
        try {
            User user = userRepository.findByUsername(authenticationRequest.getUserId());
            // validate credentials.
            if (user == null) {
                log.info("Could not find bureau officer using credentials {}", authenticationRequest);
                throw new InvalidBureauCredentialsException("Bad credentials");
            }

            if (!user.getLoginEnabledYn()) {
                log.warn("Bureau account {} locked!", authenticationRequest.getUserId());
                throw new BureauAccountLockedException("Bureau account is locked");
            }

            // hash the password
            final String password = Hashing.sha1().hashString(
                authenticationRequest.getPassword(),
                StandardCharsets.UTF_8
            ).toString().substring(0, 16);
            if (user.getPassword().compareToIgnoreCase(password) != 0) {
                saveFailedLoginAttempts(user);
                throw new InvalidBureauCredentialsException("Invalid credentials");
            }

            // post auth credentials checks
            user = calculatePasswordExpiration(user);
            user = updatePwdLastUsed(user);

            // user is authenticated.  clear the failed attempts.
            user = clearFailedLoginAttempts(user);


            // create the response object
            BureauAuthenticationController.BureauAuthenticationResponseDto responseDto =
                new BureauAuthenticationController.BureauAuthenticationResponseDto(
                    user.getOwner(),
                    user.getUsername(),
                    String.valueOf(user.getLevel()),
                    user.getDaysToExpire(),
                    user.getPasswordWarning(),
                    user != null
                        ?
                        BureauAuthenticationController.UserDto.from(user)
                        :
                        null
                );

            log.debug("Authenticated user {}", responseDto);
            return responseDto;
        } catch (DataAccessException dae) {
            log.error("Failed to retrieve bureau officer information from persistence: {}", dae.getMessage());
            throw dae;
        }
    }

    @Override
    public boolean userIsTeamLeader(BureauJwtAuthentication auth) {
        if (auth.getPrincipal() == null || !(auth.getPrincipal() instanceof BureauJWTPayload)) {
            log.error(
                "User is not authenticated with a {} token, unable to check for team leader status",
                BureauJWTPayload.class
            );
            return false;
        }
        final BureauJWTPayload token = (BureauJWTPayload) auth.getPrincipal();
        return token.getStaff() != null && token.getStaff().getRank() != null && token.getStaff().getRank().equals(1);
    }

    @Override
    public String getUsername(BureauJwtAuthentication auth) {
        return auth.getPrincipal() == null || !(auth.getPrincipal() instanceof BureauJWTPayload)
            ?
            null
            :
            ((BureauJWTPayload) auth.getPrincipal()).getLogin();
    }

    @Override
    public String getOwner(BureauJwtAuthentication auth) {
        return auth.getPrincipal() == null || !(auth.getPrincipal() instanceof BureauJWTPayload)
            ?
            null
            :
            ((BureauJWTPayload) auth.getPrincipal()).getOwner();
    }

    /**
     * Calculate if password is about to expire from the date it was last changed. If password falls into the expiration
     * warning period then number of days until expiration are appended to the response. If the date is past the
     * expiration date then the account gets blocked.
     *
     * @param user bureau user
     * @return bureau user updated with any additional password warnings
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public User calculatePasswordExpiration(final User user) {
        log.debug("Checking account {} credentials for expiry.", user.getUsername());
        Optional<SystemParameter> optpwED = systemParameterRepository.findById(1);
        final SystemParameter passwordExpiryDays = optpwED.isPresent()
            ?
            optpwED.get()
            :
            null;

        Optional<SystemParameter> optpwEWarnD = systemParameterRepository.findById(2);
        final SystemParameter passwordExpiryWarningDays = optpwEWarnD.isPresent()
            ?
            optpwEWarnD.get()
            :
            null;

        if (null == passwordExpiryDays || null == passwordExpiryWarningDays) {
            throw new BureauPasswordExpiredException("Password Expiry system settings missing.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Checking expiry date using expire after {} days, warning after {} days.",
                passwordExpiryDays.getSpValue(), passwordExpiryWarningDays.getSpValue()
            );
        }

        final LocalDate pwdLastChangedDay =
            user.getPasswordChangedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate pwdExpireDate = pwdLastChangedDay.plusDays(Long.parseLong(passwordExpiryDays.getSpValue()));
        final LocalDate warningPeriodStartDate =
            pwdExpireDate.minusDays(Long.parseLong(passwordExpiryWarningDays.getSpValue()));

        if (LocalDate.now().isAfter(pwdExpireDate) || LocalDate.now().isEqual(pwdExpireDate)) {
            // lock the account, the account credentials have expired
            log.warn("Locking bureau user {} - credentials expired.", user.getUsername());
            user.setLoginEnabledYn(Boolean.FALSE);
            userRepository.save(user);
            throw new BureauPasswordExpiredException("Password expired");
        }

        log.debug("Checking bureau user {} ");
        if ((LocalDate.now().isAfter(warningPeriodStartDate) || LocalDate.now().isEqual(warningPeriodStartDate))
            && LocalDate.now().isBefore(pwdExpireDate)) {
            final Long expiryDays = ChronoUnit.DAYS.between(LocalDate.now(), pwdExpireDate);
            log.warn("Account {} credentials expires in {} days.", user.getUsername(), expiryDays);
            user.setPasswordWarning(Boolean.TRUE);
            user.setDaysToExpire(Math.toIntExact(expiryDays));
        }

        log.debug("Account expiry checks complete.");
        return user;
    }

    /**
     * Update the count of failed login attempts for the officer.  This is invoked as part of an invalid credentials
     * flow.
     *
     * @param user The bureau officer account associated with the fail
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public User saveFailedLoginAttempts(User user) {
        final String username = user.getUsername();

        log.debug("Find previous failed login attempt counter for officer {}", username);

        // increment failed login counter
        user.incrementLoginAttempt();
        log.debug("Incrementing officer {} failed login attempts to {}", username, user.getFailedLoginAttempts());

        if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
            log.debug("Locking officer {} for reaching 3 failed attempts", username);
            // lock the user
            user.setLoginEnabledYn(Boolean.FALSE);
            log.warn("Locked officer {}", username);
            // reset the counter for failed login attempts now the account is locked.
            return clearFailedLoginAttempts(user);
        } else {
            log.debug("Bureau officer {} has {}/{} failed login attempts!", username, user.getFailedLoginAttempts(),
                MAX_FAILED_LOGIN_ATTEMPTS
            );
            log.debug("Bureau officer {} login attempts updated.", username);
            return userRepository.save(user);
        }
    }

    /**
     * Clear the count of failed login attempts for the bureau user. This is invoked after a successful authentication
     * flow.
     *
     * @param user The bureau account associated with the success
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public User clearFailedLoginAttempts(final User user) {
        final String username = user.getUsername();
        log.debug("Clearing failed login attempt counter for bureau user {}", username);
        if (user.getFailedLoginAttempts() != 0) {
            user.setFailedLoginAttempts(0);
            log.debug("Officer {} login attempts cleared.", username);
            return userRepository.save(user);
        } else {
            log.debug("No previous failed attempts to clear for officer {}", username);
        }
        return user;
    }

    /**
     * Updates password last usage date to now after password passes basic checks required by MOJ for treating bureau
     * passwords.
     *
     * @param user Current authentication entity undergoing checks
     * @return updated entity
     */
    private User updatePwdLastUsed(final User user) {
        log.debug("Updating password last used date");
        user.setLastLoggedIn(LocalDateTime.now(clock));
        return userRepository.save(user);
    }


    /**
     * Failed to validate bureau officer credentials.
     * Created by jonny on 24/03/17.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public class InvalidBureauCredentialsException extends RuntimeException {
        public InvalidBureauCredentialsException() {
        }

        public InvalidBureauCredentialsException(String message) {
            super(message);
        }
    }

    /**
     * Bureau Officer account is locked.
     * Created by jonny on 24/03/17.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public class BureauAccountLockedException extends RuntimeException {
        public BureauAccountLockedException() {
        }

        public BureauAccountLockedException(String message) {
            super(message);
        }
    }

    /**
     * Bureau Officer password has expired.
     * Created by jonny on 24/03/17.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public class BureauPasswordExpiredException extends RuntimeException {
        public BureauPasswordExpiredException() {
        }

        public BureauPasswordExpiredException(String message) {
            super(message);
        }
    }
}
