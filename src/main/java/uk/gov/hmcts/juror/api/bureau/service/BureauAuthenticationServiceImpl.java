package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.controller.BureauAuthenticationController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;

/**
 * Implementation for Authentication service interface for bureau authentication operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BureauAuthenticationServiceImpl implements BureauAuthenticationService {
    /**
     * Maximum failed attempts before an account should be disabled.
     */
    private final UserRepository userRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final EntityManager entityManager;


    @Override
    @Transactional(noRollbackFor = InvalidBureauCredentialsException.class)
    @Deprecated(forRemoval = true)
    public BureauAuthenticationController.BureauAuthenticationResponseDto authenticateBureauOfficer(
        BureauAuthenticationController.BureauAuthenticationRequestDto authenticationRequest) {
        try {
            User user = userRepository.findByUsername(authenticationRequest.getUserId());
            // validate credentials.
            if (user == null) {
                log.info("Could not find bureau officer using credentials {}", authenticationRequest);
                throw new InvalidBureauCredentialsException("Bad credentials");
            }


            // create the response object
            BureauAuthenticationController.BureauAuthenticationResponseDto responseDto =
                new BureauAuthenticationController.BureauAuthenticationResponseDto(
                    user.getOwner(),
                    user.getUsername(),
                    String.valueOf(user.getLevel()),
                    999,
                    false,
                    user.getUserType(),
                    user.getRoles(),
                    BureauAuthenticationController.UserDto.from(courtLocationRepository, entityManager, user)
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
     * Failed to validate bureau officer credentials.
     * Created by jonny on 24/03/17.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public class InvalidBureauCredentialsException extends RuntimeException {

        public InvalidBureauCredentialsException(String message) {
            super(message);
        }
    }
}
