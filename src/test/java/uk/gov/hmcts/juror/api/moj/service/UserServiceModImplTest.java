package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.authentication.CreateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.JwtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UpdateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserSearchDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UsernameDto;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class UserServiceModImplTest {

    private static final String BUREAU_OWNER = "400";
    private static final String COURT_OWNER = "415";

    @Mock
    private UserRepository userRepository;
    @Mock
    private CourtLocationRepository courtLocationRepository;
    @Mock
    private JwtService jwtService;
    @Captor
    private ArgumentCaptor<User> userCaptor;
    @Captor
    private ArgumentCaptor<UserSearchDto> userSearchDtoCaptor;
    @Captor
    private ArgumentCaptor<BureauJwtPayload> jwtPayloadCaptor;

    @InjectMocks
    private UserServiceModImpl userService;

    @Nested
    class CreateUser {
        @Test
        void createsBureauUserWithNormalisedEmailBureauCourtAndApprovalLimit() {
            CourtLocation bureauCourt = court(BUREAU_OWNER, BUREAU_OWNER, CourtType.MAIN);
            CreateUserDto createUserDto = CreateUserDto.builder()
                .email("Test.User@EMAIL.GOV.UK ")
                .name("Test User")
                .userType(UserType.BUREAU)
                .approvalLimit(new BigDecimal("31.20"))
                .roles(Set.of(Role.MANAGER))
                .build();

            when(userRepository.existsByEmail("test.user@email.gov.uk")).thenReturn(false);
            when(userRepository.existsById("Test.User")).thenReturn(false);
            when(courtLocationRepository.findById(BUREAU_OWNER)).thenReturn(Optional.of(bureauCourt));

            UsernameDto response = userService.createUser(createUserDto);

            assertThat(response.getUsername()).isEqualTo("Test.User");
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getUsername()).isEqualTo("Test.User");
            assertThat(savedUser.getEmail()).isEqualTo("test.user@email.gov.uk");
            assertThat(savedUser.getName()).isEqualTo("Test User");
            assertThat(savedUser.getUserType()).isEqualTo(UserType.BUREAU);
            assertThat(savedUser.isActive()).isTrue();
            assertThat(savedUser.getApprovalLimit()).isEqualByComparingTo("31.20");
            assertThat(savedUser.getRoles()).containsExactly(Role.MANAGER);
            assertThat(savedUser.getCourts()).containsExactly(bureauCourt);
        }

        @Test
        void createsCourtUserWithoutDefaultBureauCourt() {
            CreateUserDto createUserDto = CreateUserDto.builder()
                .email("court.user@email.gov.uk")
                .name("Court User")
                .userType(UserType.COURT)
                .build();

            when(userRepository.existsByEmail("court.user@email.gov.uk")).thenReturn(false);
            when(userRepository.existsById("court.user")).thenReturn(false);

            userService.createUser(createUserDto);

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getApprovalLimit()).isEqualByComparingTo("0.00");
            assertThat(userCaptor.getValue().getCourts()).isEmpty();
            verifyNoInteractions(courtLocationRepository);
        }

        @Test
        void rejectsCreateWhenEmailIsAlreadyInUse() {
            CreateUserDto createUserDto = CreateUserDto.builder()
                .email("test.user@email.gov.uk")
                .name("Test User")
                .userType(UserType.COURT)
                .build();

            when(userRepository.existsByEmail("test.user@email.gov.uk")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(createUserDto))
                .isInstanceOf(MojException.BusinessRuleViolation.class)
                .hasMessage("Email is already in use");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateUser {
        @Test
        void adminCanUpdateIdentityApprovalLimitStatusAndRoles() {
            User existingUser = User.builder()
                .username("court.user")
                .email("old@email.gov.uk")
                .name("Old Name")
                .active(true)
                .approvalLimit(new BigDecimal("10.00"))
                .roles(Set.of())
                .userType(UserType.COURT)
                .build();
            UpdateUserDto updateUserDto = UpdateUserDto.builder()
                .email(" New.Email@EMAIL.GOV.UK ")
                .name("New Name")
                .isActive(false)
                .approvalLimit(new BigDecimal("25.50"))
                .roles(Set.of(Role.MANAGER))
                .build();

            when(userRepository.findById("court.user")).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail("new.email@email.gov.uk")).thenReturn(false);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::isAdministration).thenReturn(true);
                securityUtil.when(SecurityUtil::canEditApprovalLimit).thenReturn(true);

                userService.updateUser("court.user", updateUserDto);
            }

            assertThat(existingUser.getEmail()).isEqualTo("new.email@email.gov.uk");
            assertThat(existingUser.getName()).isEqualTo("New Name");
            assertThat(existingUser.isActive()).isFalse();
            assertThat(existingUser.getApprovalLimit()).isEqualByComparingTo("25.50");
            assertThat(existingUser.getRoles()).containsExactly(Role.MANAGER);
            verify(userRepository).save(existingUser);
        }

        @Test
        void nonAdminCannotUpdateUserOutsideActiveCourt() {
            User existingUser = User.builder()
                .username("other.court.user")
                .email("other@email.gov.uk")
                .name("Other Court User")
                .active(true)
                .userType(UserType.COURT)
                .courts(Set.of(court("462", "462", CourtType.MAIN)))
                .build();
            UpdateUserDto updateUserDto = UpdateUserDto.builder()
                .email("same@email.gov.uk")
                .name("Same Name")
                .isActive(true)
                .roles(Set.of())
                .build();

            when(userRepository.findById("other.court.user")).thenReturn(Optional.of(existingUser));

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::isAdministration).thenReturn(false);
                securityUtil.when(SecurityUtil::getActiveOwner).thenReturn(COURT_OWNER);

                assertThatThrownBy(() -> userService.updateUser("other.court.user", updateUserDto))
                    .isInstanceOf(MojException.Forbidden.class)
                    .hasMessage("User not part of court");
            }

            verify(userRepository, never()).save(any());
        }

        @Test
        void nonAdminCannotUpdateApprovalLimit() {
            User existingUser = User.builder()
                .username("court.user")
                .email("court.user@email.gov.uk")
                .name("Court User")
                .active(true)
                .approvalLimit(new BigDecimal("10.00"))
                .userType(UserType.COURT)
                .courts(Set.of(court(COURT_OWNER, COURT_OWNER, CourtType.MAIN)))
                .build();
            UpdateUserDto updateUserDto = UpdateUserDto.builder()
                .email("ignored@email.gov.uk")
                .name("Ignored")
                .isActive(false)
                .approvalLimit(new BigDecimal("99.99"))
                .roles(Set.of(Role.SENIOR_JUROR_OFFICER))
                .build();

            when(userRepository.findById("court.user")).thenReturn(Optional.of(existingUser));

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::isAdministration).thenReturn(false);
                securityUtil.when(SecurityUtil::getActiveOwner).thenReturn(COURT_OWNER);
                securityUtil.when(SecurityUtil::canEditApprovalLimit).thenReturn(false);

                userService.updateUser("court.user", updateUserDto);
            }

            assertThat(existingUser.getEmail()).isEqualTo("court.user@email.gov.uk");
            assertThat(existingUser.getName()).isEqualTo("Court User");
            assertThat(existingUser.getApprovalLimit()).isEqualByComparingTo("10.00");
            assertThat(existingUser.isActive()).isFalse();
            assertThat(existingUser.getRoles()).containsExactly(Role.SENIOR_JUROR_OFFICER);
            verify(userRepository).save(existingUser);
        }
    }

    @Nested
    class Authentication {
        @Test
        void createJwtUpdatesLastLoggedInAndBuildsJwtPayload() {
            CourtLocation satelliteCourt = court("462", COURT_OWNER, CourtType.SATELLITE);
            CourtLocation mainCourt = court(COURT_OWNER, COURT_OWNER, CourtType.MAIN);
            User user = User.builder()
                .username("court.user")
                .email("court.user@email.gov.uk")
                .name("Court User")
                .active(true)
                .userType(UserType.COURT)
                .roles(Set.of(Role.MANAGER))
                .courts(Set.of(mainCourt))
                .build();

            when(userRepository.findByEmailIgnoreCase("court.user@email.gov.uk")).thenReturn(Optional.of(user));
            when(courtLocationRepository.findById("462")).thenReturn(Optional.of(satelliteCourt));
            when(courtLocationRepository.findByOwner(COURT_OWNER)).thenReturn(List.of(satelliteCourt, mainCourt));
            when(jwtService.generateBureauJwtToken(eq("court.user"), any(BureauJwtPayload.class))).thenReturn("jwt");

            LocalDateTime before = LocalDateTime.now();
            JwtDto response = userService.createJwt("court.user@email.gov.uk", "462");
            LocalDateTime after = LocalDateTime.now();

            assertThat(response.getJwt()).isEqualTo("jwt");
            assertThat(user.getLastLoggedIn()).isBetween(before, after);
            verify(userRepository).save(user);
            verify(jwtService).generateBureauJwtToken(eq("court.user"), jwtPayloadCaptor.capture());
            BureauJwtPayload payload = jwtPayloadCaptor.getValue();
            assertThat(payload.getOwner()).isEqualTo(COURT_OWNER);
            assertThat(payload.getLocCode()).isEqualTo("462");
            assertThat(payload.getLogin()).isEqualTo("court.user");
            assertThat(payload.getUserType()).isEqualTo(UserType.COURT);
            assertThat(payload.getActiveUserType()).isEqualTo(UserType.COURT);
            assertThat(payload.getRoles()).containsExactly(Role.MANAGER);
            assertThat(payload.getStaff().getCourts()).containsExactly(COURT_OWNER, "462");
        }

        @Test
        void createJwtRejectsInactiveUser() {
            User user = User.builder()
                .username("inactive.user")
                .email("inactive.user@email.gov.uk")
                .active(false)
                .userType(UserType.COURT)
                .build();

            when(userRepository.findByEmailIgnoreCase("inactive.user@email.gov.uk")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.createJwt("inactive.user@email.gov.uk", COURT_OWNER))
                .isInstanceOf(MojException.Forbidden.class)
                .hasMessage("User is not active");
            verify(userRepository, never()).save(any());
            verifyNoInteractions(jwtService);
        }

        @Test
        void getCourtsReturnsSyntheticAdminCourtForAdministrator() {
            User user = User.builder()
                .username("admin.user")
                .email("admin.user@email.gov.uk")
                .userType(UserType.ADMINISTRATOR)
                .build();

            when(userRepository.findByEmailIgnoreCase("admin.user@email.gov.uk")).thenReturn(Optional.of(user));

            assertThat(userService.getCourts(" admin.user@email.gov.uk "))
                .singleElement()
                .satisfies(courtDto -> {
                    assertThat(courtDto.getLocCode()).isEqualTo("ADMIN");
                    assertThat(courtDto.getName()).isEqualTo("ADMIN");
                    assertThat(courtDto.getCourtType()).isNull();
                });
            verifyNoInteractions(courtLocationRepository);
        }
    }

    @Nested
    class SearchAndAccess {
        @Test
        void getUsersScopesCourtUserSearchToActiveOwner() {
            UserSearchDto userSearchDto = UserSearchDto.builder()
                .pageNumber(1)
                .pageLimit(25)
                .userType(UserType.BUREAU)
                .court(BUREAU_OWNER)
                .build();
            PaginatedList<UserDetailsDto> expectedResponse = new PaginatedList<>();

            when(userRepository.messageSearch(userSearchDto)).thenReturn(expectedResponse);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getUserType).thenReturn(UserType.COURT);
                securityUtil.when(SecurityUtil::getActiveOwner).thenReturn(COURT_OWNER);

                assertThat(userService.getUsers(userSearchDto)).isSameAs(expectedResponse);
            }

            verify(userRepository).messageSearch(userSearchDtoCaptor.capture());
            assertThat(userSearchDtoCaptor.getValue().getUserType()).isEqualTo(UserType.COURT);
            assertThat(userSearchDtoCaptor.getValue().getCourt()).isEqualTo(COURT_OWNER);
        }

        @Test
        void getUserRejectsNonAdminAccessToUserOutsideActiveCourt() {
            User existingUser = User.builder()
                .username("other.court.user")
                .email("other@email.gov.uk")
                .name("Other Court User")
                .active(true)
                .userType(UserType.COURT)
                .courts(Set.of(court("462", "462", CourtType.MAIN)))
                .build();

            when(userRepository.findById("other.court.user")).thenReturn(Optional.of(existingUser));

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::isAdministration).thenReturn(false);
                securityUtil.when(SecurityUtil::getActiveOwner).thenReturn(COURT_OWNER);

                assertThatThrownBy(() -> userService.getUser("other.court.user"))
                    .isInstanceOf(MojException.Forbidden.class)
                    .hasMessage("User not part of court");
            }
        }
    }

    @Nested
    class CourtsAndType {
        @Test
        void addCourtAddsResolvedCourtsAndSavesUser() {
            User user = User.builder()
                .username("court.user")
                .userType(UserType.COURT)
                .courts(mutableSet(court(COURT_OWNER, COURT_OWNER, CourtType.MAIN)))
                .build();
            CourtLocation newCourt = court("462", "462", CourtType.MAIN);

            when(userRepository.findById("court.user")).thenReturn(Optional.of(user));
            when(courtLocationRepository.findById("462")).thenReturn(Optional.of(newCourt));

            userService.addCourt("court.user", List.of("462"));

            assertThat(user.getCourts()).contains(newCourt);
            verify(userRepository).save(user);
        }

        @Test
        void removeCourtRemovesResolvedCourtsAndSavesUser() {
            CourtLocation courtToRemove = court("462", "462", CourtType.MAIN);
            User user = User.builder()
                .username("court.user")
                .userType(UserType.COURT)
                .courts(mutableSet(court(COURT_OWNER, COURT_OWNER, CourtType.MAIN), courtToRemove))
                .build();

            when(userRepository.findById("court.user")).thenReturn(Optional.of(user));
            when(courtLocationRepository.findById("462")).thenReturn(Optional.of(courtToRemove));

            userService.removeCourt("court.user", List.of("462"));

            assertThat(user.getCourts()).doesNotContain(courtToRemove);
            verify(userRepository).save(user);
        }

        @Test
        void changeUserTypeToBureauClearsExistingCourtsAndRolesAndAddsBureauCourt() {
            CourtLocation bureauCourt = court(BUREAU_OWNER, BUREAU_OWNER, CourtType.MAIN);
            User user = User.builder()
                .username("court.user")
                .userType(UserType.COURT)
                .roles(mutableSet(Role.MANAGER))
                .courts(mutableSet(court(COURT_OWNER, COURT_OWNER, CourtType.MAIN)))
                .build();

            when(userRepository.findById("court.user")).thenReturn(Optional.of(user));
            when(courtLocationRepository.findById(BUREAU_OWNER)).thenReturn(Optional.of(bureauCourt));

            userService.changeUserType("court.user", UserType.BUREAU);

            assertThat(user.getUserType()).isEqualTo(UserType.BUREAU);
            assertThat(user.getRoles()).isEmpty();
            assertThat(user.getCourts()).containsExactly(bureauCourt);
        }

        @Test
        void changeUserTypeNoOpsWhenTypeIsUnchanged() {
            User user = User.builder()
                .username("bureau.user")
                .userType(UserType.BUREAU)
                .roles(Set.of(Role.MANAGER))
                .courts(Set.of(court(BUREAU_OWNER, BUREAU_OWNER, CourtType.MAIN)))
                .build();

            when(userRepository.findById("bureau.user")).thenReturn(Optional.of(user));

            assertThatCode(() -> userService.changeUserType("bureau.user", UserType.BUREAU))
                .doesNotThrowAnyException();
            assertThat(user.getRoles()).containsExactly(Role.MANAGER);
            assertThat(user.getCourts()).hasSize(1);
            verifyNoInteractions(courtLocationRepository);
        }
    }

    @Nested
    class Helpers {
        @Test
        void createUsernameTruncatesLocalPartToThirtyCharactersWhenUnique() {
            String localPart = "abcdefghijklmnopqrstuvwxyz1234567890";
            String expectedUsername = localPart.substring(0, 30);
            when(userRepository.existsById(expectedUsername)).thenReturn(false);

            assertThat(userService.createUsername(localPart + "@email.gov.uk")).isEqualTo(expectedUsername);

            verify(userRepository).existsById(expectedUsername);
        }

        @Test
        void createUsernameAppendsOneForSingleCollision() {
            when(userRepository.existsById("existing.user")).thenReturn(true);
            when(userRepository.existsById("existing.user1")).thenReturn(false);

            assertThat(userService.createUsername("existing.user@email.gov.uk")).isEqualTo("existing.user1");

            verify(userRepository, times(1)).existsById("existing.user");
            verify(userRepository, times(1)).existsById("existing.user1");
        }

        @Test
        void createUsernameIncrementsSuffixForMultipleCollisions() {
            when(userRepository.existsById("existing.user")).thenReturn(true);
            when(userRepository.existsById("existing.user1")).thenReturn(true);
            when(userRepository.existsById("existing.user2")).thenReturn(false);

            assertThat(userService.createUsername("existing.user@email.gov.uk")).isEqualTo("existing.user2");

            verify(userRepository).existsById("existing.user");
            verify(userRepository).existsById("existing.user1");
            verify(userRepository).existsById("existing.user2");
        }

        @Test
        void createUsernameShortensTruncatedUsernameToMakeRoomForSuffix() {
            String localPart = "abcdefghijklmnopqrstuvwxyz1234567890";
            String truncatedUsername = localPart.substring(0, 30);
            String expectedUsername = localPart.substring(0, 29) + "1";
            when(userRepository.existsById(truncatedUsername)).thenReturn(true);
            when(userRepository.existsById(expectedUsername)).thenReturn(false);

            assertThat(userService.createUsername(localPart + "@email.gov.uk"))
                .isEqualTo(expectedUsername)
                .hasSize(30);

            verify(userRepository).existsById(truncatedUsername);
            verify(userRepository).existsById(expectedUsername);
        }
    }

    private CourtLocation court(String locCode, String owner, CourtType courtType) {
        CourtLocation courtLocation = CourtLocation.builder()
            .locCode(locCode)
            .owner(owner)
            .name("Court " + locCode)
            .build();
        assertThat(courtLocation.getType()).isEqualTo(courtType);
        return courtLocation;
    }

    @SafeVarargs
    private static <T> Set<T> mutableSet(T... values) {
        return new HashSet<>(List.of(values));
    }
}
