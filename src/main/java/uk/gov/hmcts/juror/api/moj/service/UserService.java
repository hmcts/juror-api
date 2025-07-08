package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.authentication.CourtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.CreateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.JwtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UpdateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserSearchDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UsernameDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UsernameDto createUser(CreateUserDto createUserDto);

    List<CourtDto> getCourts(String email);

    JwtDto createJwt(String email, String locCode);

    void updateUser(String username, UpdateUserDto updateUserDto);

    void addCourt(String username, List<String> courts);

    void removeCourt(String username, List<String> courts);

    PaginatedList<UserDetailsDto> getUsers(UserSearchDto userSearchDto);

    UserDetailsDto getUser(String username);

    User findUserByUsername(String username);

    Optional<User> findUserByUsernameOpt(String username);

    void changeUserType(String username, UserType userType);
}
