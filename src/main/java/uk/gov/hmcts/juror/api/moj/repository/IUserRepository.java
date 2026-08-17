package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserSearchDto;

@FunctionalInterface
public interface IUserRepository {
    PaginatedList<UserDetailsDto> messageSearch(UserSearchDto search);
}
