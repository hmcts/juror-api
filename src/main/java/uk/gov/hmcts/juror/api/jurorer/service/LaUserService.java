package uk.gov.hmcts.juror.api.jurorer.service;

import uk.gov.hmcts.juror.api.jurorer.controller.dto.ExportLaEmailAddressResponseDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaJwtDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaUserDetailsDto;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;

import java.util.List;
import java.util.Optional;


public interface LaUserService {

    LaJwtDto createJwt(String email);

    LaUser findUserByUsername(String username);

    LaUserDetailsDto getLaUserDetails(String laCode);

    List<LaUser> findUsersByLaCode(String laCode);

    void saveLaUser(LaUser laUser);

    Optional<LaUser> findLastLoggedInUserByLaCode(String laCode);

    /**
     * Get all email addresses grouped by Local Authority.
     *
     * @param activeOnly If true, only includes active users. If false, includes all users.
     * @return Export response with LA email addresses filtered by active status
     */
    ExportLaEmailAddressResponseDto getAllLaEmailAddresses(boolean activeOnly);

}
