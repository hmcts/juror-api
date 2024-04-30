package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.BureauAuthenticationController;

/**
 * Authentication service interface for bureau authentication operations.
 */
public interface BureauAuthenticationService {

    /**BureauAuthenticationService
     * Create a authentication response based on provided bureau officer credentials.
     *
     * @param authenticationRequest Bureau officer credentials.
     * @return Bureau authentication information.
     */
    @Deprecated(forRemoval = true)
    BureauAuthenticationController.BureauAuthenticationResponseDto authenticateBureauOfficer(
        BureauAuthenticationController.BureauAuthenticationRequestDto authenticationRequest);

}
