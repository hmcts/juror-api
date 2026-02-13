package uk.gov.hmcts.juror.api.jurorer.service;

import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;

import java.util.List;


public interface LocalAuthorityService {

    List<LocalAuthority> getAllLocalAuthorities(boolean activeOnly);

    LocalAuthority getLocalAuthorityByCode(String localAuthorityCode);

}
