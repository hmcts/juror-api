package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.authentication.CourtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.CreateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.JwtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UpdateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserCourtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserSearchDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UsernameDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserServiceModImpl implements UserService {


    private final UserRepository userRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UsernameDto createUser(CreateUserDto createUserDto) {
        if (doesUserExistWithEmail(createUserDto.getEmail())) {
            throw new MojException.BusinessRuleViolation("Email is already in use",
                MojException.BusinessRuleViolation.ErrorCode.EMAIL_IN_USE);
        }

        User user = User.builder()
            .userType(createUserDto.getUserType())
            .email(createUserDto.getEmail())
            .name(createUserDto.getName())
            .roles(createUserDto.getRoles())
            .active(true)
            .username(createUsername(createUserDto.getEmail()))
            .approvalLimit(BigDecimalUtils.getOrZero(createUserDto.getApprovalLimit()))
            .build();

        if (UserType.ADMINISTRATOR.equals(createUserDto.getUserType()) || UserType.BUREAU.equals(
            createUserDto.getUserType())) {
            user.addCourt(getCourtLocation(SecurityUtil.BUREAU_OWNER));
        }
        userRepository.save(user);
        return new UsernameDto(user.getUsername());
    }

    @Override
    @Transactional
    public void updateUser(String username, UpdateUserDto updateUserDto) {
        User user = findUserByUsername(username);
        if (!SecurityUtil.isAdministration()
            && !user.hasCourtByOwner(SecurityUtil.getActiveOwner())) {
            throw new MojException.Forbidden("User not part of court", null);
        }
        if (SecurityUtil.isAdministration()) {
            user.setEmail(updateUserDto.getEmail());
            user.setName(updateUserDto.getName());
            user.setApprovalLimit(BigDecimalUtils.getOrZero(updateUserDto.getApprovalLimit()));
        }
        user.setActive(updateUserDto.getIsActive());
        user.setRoles(updateUserDto.getRoles());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtDto> getCourts(String email) {
        User user = findUserByEmail(email);

        if (UserType.ADMINISTRATOR.equals(user.getUserType())) {
            return List.of(
                CourtDto.builder()
                    .name("ADMIN")
                    .locCode("ADMIN")
                    .courtType(null)
                    .build()
            );
        }

        return user.getCourts()
            .stream()
            .map(courtLocation -> getCourtsByOwner(courtLocation.getOwner()))
            .flatMap(Collection::stream)
            .map(CourtDto::new)
            .sorted(Comparator.comparing(CourtDto::getLocCode))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JwtDto createJwt(String email, String locCode) {
        User user = findUserByEmail(email);
        if (!user.isActive()) {
            throw new MojException.Forbidden("User is not active", null);
        }
        if ("ADMIN".equals(locCode) && !UserType.ADMINISTRATOR.equals(user.getUserType())) {
            throw new MojException.Forbidden("User must be an admin", null);
        }

        final UserType activeUserType;
        if (UserType.ADMINISTRATOR.equals(user.getUserType())) {
            if ("ADMIN".equals(locCode)) {
                activeUserType = UserType.ADMINISTRATOR;
                locCode = "400";
            } else {
                activeUserType = (SecurityUtil.BUREAU_OWNER.equals(locCode) ? UserType.BUREAU : UserType.COURT);
            }
        } else {
            activeUserType = user.getUserType();
        }

        CourtLocation loggedInCourt = getCourtLocation(locCode);
        List<CourtLocation> courtLocations = getCourtsByOwner(loggedInCourt.getOwner());
        if (UserType.ADMINISTRATOR.equals(user.getUserType()) || user.hasCourtByOwner(loggedInCourt.getOwner())) {
            return new JwtDto(
                jwtService.generateBureauJwtToken(
                    user.getUsername(),
                    new BureauJwtPayload(user, activeUserType, locCode, courtLocations)
                ));
        }
        throw new MojException.Forbidden("User not part of court", null);
    }


    @Override
    @Transactional
    public void addCourt(String username, List<String> courts) {
        User user = findUserByUsername(username);
        courts.forEach(string -> user.addCourt(getCourtLocation(string)));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeCourt(String username, List<String> courts) {
        User user = findUserByUsername(username);
        courts.forEach(string -> user.removeCourt(getCourtLocation(string)));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedList<UserDetailsDto> getUsers(UserSearchDto userSearchDto) {
        UserType userType = SecurityUtil.getUserType();
        if (userType.equals(UserType.COURT)) {
            userSearchDto.setUserType(UserType.COURT);
            userSearchDto.setCourt(SecurityUtil.getActiveOwner());
        }
        if (userType.equals(UserType.BUREAU)) {
            userSearchDto.setUserType(UserType.BUREAU);
            userSearchDto.setCourt(SecurityUtil.BUREAU_OWNER);
        }
        return userRepository.messageSearch(userSearchDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailsDto getUser(String username) {
        User user = findUserByUsername(username);
        if (!SecurityUtil.isAdministration()
            && !user.hasCourtByOwner(SecurityUtil.getActiveOwner())) {
            throw new MojException.Forbidden("User not part of court", null);
        }
        return new UserDetailsDto(user, getUserCourts(user));
    }


    @Override
    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        return userRepository.findById(username).orElseThrow(
            () -> new MojException.NotFound("User not found", null)
        );
    }

    @Override
    @Transactional
    public void changeUserType(String username, UserType userType) {
        User user = findUserByUsername(username);
        if (userType.equals(user.getUserType())) {
            return;
        }
        user.setUserType(userType);
        user.clearCourts();
        user.clearRoles();

        if (UserType.ADMINISTRATOR.equals(userType) || UserType.BUREAU.equals(userType)) {
            user.addCourt(getCourtLocation(SecurityUtil.BUREAU_OWNER));
        }
    }

    @Transactional(readOnly = true)
    User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
            () -> new MojException.NotFound("User not found", null)
        );
    }

    @Transactional(readOnly = true)
    CourtLocation getCourtLocation(String locCode) {
        return courtLocationRepository.findById(locCode)
            .orElseThrow(() -> new MojException.NotFound("Court not found", null));
    }

    @Transactional(readOnly = true)
    List<UserCourtDto> getUserCourts(User user) {
        return user.getCourts().stream()
            .map(courtLocation -> new UserCourtDto(getCourtsByOwner(courtLocation.getOwner())))
            .toList();
    }

    @Transactional(readOnly = true)
    List<CourtLocation> getCourtsByOwner(String owner) {
        return courtLocationRepository.findByOwner(owner);
    }


    String createUsername(String email) {
        String username = email.split("@")[0];
        username = username.substring(0, Math.min(username.length(), 18));
        // limit to 18 characters (DB constraint + 2 digits for numerics)
        int i = 1;
        String usernameTemp = username;
        while (userRepository.existsById(usernameTemp)) {
            usernameTemp = username + i;
        }
        return usernameTemp;
    }

    boolean doesUserExistWithEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
