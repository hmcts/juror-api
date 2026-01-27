package uk.gov.hmcts.juror.api.jurorer.service;

import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaJwtDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaUserDetailsDto;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;


public interface LaUserService {

    LaJwtDto createJwt(String email);

    LaUserDetailsDto getUser(String username);

    LaUser findUserByUsername(String username);

}
