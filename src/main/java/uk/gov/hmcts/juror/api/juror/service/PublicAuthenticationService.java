package uk.gov.hmcts.juror.api.juror.service;

import uk.gov.hmcts.juror.api.juror.controller.PublicAuthenticationController.PublicAuthenticationRequestDto;
import uk.gov.hmcts.juror.api.juror.controller.PublicAuthenticationController.PublicAuthenticationResponseDto;

/**
 * Authentication service interface for public authentication operations.
 */
public interface PublicAuthenticationService {
    /**
     * Create a authentication response based on provided juror credentials.
     *
     * @param authenticationRequest Juror credentials.
     * @return Juror authentication information.
     */
    PublicAuthenticationResponseDto authenticationJuror(PublicAuthenticationRequestDto authenticationRequest);
}
