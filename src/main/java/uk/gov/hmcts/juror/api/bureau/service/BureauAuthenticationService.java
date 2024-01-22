package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.BureauAuthenticationController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

/**
 * Authentication service interface for bureau authentication operations.
 */
public interface BureauAuthenticationService {

    /**
     * Create a authentication response based on provided bureau officer credentials.
     *
     * @param authenticationRequest Bureau officer credentials.
     * @return Bureau authentication information.
     */
    BureauAuthenticationController.BureauAuthenticationResponseDto authenticateBureauOfficer(
        BureauAuthenticationController.BureauAuthenticationRequestDto authenticationRequest);

    /**
     * Checks whether the authenticated user is a team leader.
     *
     * @param auth the authentication object to check
     * @return whether the authenticated user is a team leader
     */
    boolean userIsTeamLeader(BureauJwtAuthentication auth);

    /**
     * Gets the username of the logged-in user.
     *
     * @param auth authentication object
     * @return username of the logged-in user
     */
    String getUsername(BureauJwtAuthentication auth);

    /**
     * Gets the owner of the logged-in user.
     *
     * @param auth authentication object
     * @return username of the logged-in user
     */
    String getOwner(BureauJwtAuthentication auth);
}
