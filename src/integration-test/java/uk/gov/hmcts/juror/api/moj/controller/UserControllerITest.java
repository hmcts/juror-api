package uk.gov.hmcts.juror.api.moj.controller;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.juror.api.AbstractControllerIntegrationTest;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.authentication.CourtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.CreateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UpdateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserCourtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserSearchDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UsernameDto;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + UserControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Sql(value = {"/db/administration/teardownUsers.sql",
    "/db/administration/createUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/db/administration/teardownUsers.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class UserControllerITest extends AbstractIntegrationTest {

    public static final String BASE_URL = "/api/v1/moj/users";
    private static final String EMAIL_SUFFIX = "@email.gov.uk";
    private static final String SYSTEM_USER = "test_system";

    private final TestRestTemplate template;
    private HttpHeaders httpHeaders;

    private final UserRepository userRepository;

    private final PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createHmacJwt());
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    private void assertUserHasCourts(String username, String... courtLocCodes) {
        transactionTemplate.execute(status -> {
            User user = getUserFromUsername(username);
            assertThat(user.getCourts()).hasSize(courtLocCodes.length);
            assertThat(user.getCourts()
                .stream().map(CourtLocation::getLocCode).toList())
                .containsExactlyInAnyOrder(courtLocCodes);
            return null;
        });
    }

    private void assertUserIsUpdatedBy(String username, String updateBy) {
        transactionTemplate.execute(status -> {
            User user = getUserFromUsername(username);
            assertThat(user.getUpdatedBy()).isEqualTo(updateBy);
            return null;
        });
    }

    private User getUserFromUsername(String username) {
        return transactionTemplate.execute(status -> {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                fail("Could not find user");
            }
            return user;
        });
    }

    @Nested
    @DisplayName("POST (GET) " + ViewAllUsers.URL)
    class ViewAllUsers extends AbstractControllerIntegrationTest<UserSearchDto, PaginatedList<UserDetailsDto>> {
        private static final String URL = BASE_URL;

        protected ViewAllUsers() {
            super(HttpMethod.POST, template, HttpStatus.OK);
        }

        @Override
        protected String getValidUrl() {
            return URL;
        }

        @Override
        protected String getValidJwt() {
            return createJwt("test_admin_standard", "400",
                UserType.ADMINISTRATOR, Set.of(), "400");
        }

        @Override
        protected UserSearchDto getValidPayload() {
            return UserSearchDto.builder()
                .pageNumber(1)
                .pageLimit(3)
                .build();
        }

        @DisplayName("Positive")
        @Nested
        class Positive {
            @Test
            void adminTypical() {
                testBuilder()
                    .jwt(createJwtAdministrator("test_admin_standard"))
                    .triggerValid()
                    .assertEquals(
                        PaginatedList.builder()
                            .currentPage(1L)
                            .totalPages(5L)
                            .totalItems(14L)
                            .data(List.of(UserDetailsDto.builder()
                                    .username("test_admin_inactive")
                                    .email("test_admin_inactive@email.gov.uk")
                                    .name("Admin Inactive")
                                    .isActive(false)
                                    .lastSignIn(null)
                                    .userType(UserType.ADMINISTRATOR)
                                    .roles(Set.of())
                                    .courts(List.of(UserCourtDto.builder()
                                        .primaryCourt(CourtDto.builder()
                                            .name("Jury Central Summoning Bureau")
                                            .locCode("400")
                                            .courtType(CourtType.MAIN).build())
                                        .satelliteCourts(List.of()).build()))
                                    .build(),
                                UserDetailsDto.builder()
                                    .username("test_admin_standard")
                                    .email("test_admin_standard@email.gov.uk")
                                    .name("Admin Standard")
                                    .isActive(true)
                                    .lastSignIn(null)
                                    .userType(UserType.ADMINISTRATOR)
                                    .roles(Set.of())
                                    .courts(List.of(UserCourtDto.builder()
                                        .primaryCourt(CourtDto.builder()
                                            .name("Jury Central Summoning Bureau")
                                            .locCode("400")
                                            .courtType(CourtType.MAIN).build())
                                        .satelliteCourts(List.of()).build()))
                                    .build(),
                                UserDetailsDto.builder()
                                    .username("test_bureau_inactive")
                                    .email("test_bureau_inactive@email.gov.uk")
                                    .name("Bureau Inactive")
                                    .isActive(false)
                                    .lastSignIn(null)
                                    .userType(UserType.BUREAU)
                                    .roles(Set.of())
                                    .courts(List.of(UserCourtDto.builder()
                                        .primaryCourt(CourtDto.builder()
                                            .name("Jury Central Summoning Bureau")
                                            .locCode("400")
                                            .courtType(CourtType.MAIN).build())
                                        .satelliteCourts(List.of()).build()))
                                    .build()))
                            .build()
                    );
            }

            @Test
            void courtManagerTypical() {
                testBuilder()
                    .jwt(createJwt("test_court_manager", Set.of(Role.MANAGER), "415", "415"))
                    .triggerValid()
                    .assertEquals(
                        PaginatedList.builder()
                            .currentPage(1L)
                            .totalPages(2L)
                            .totalItems(6L)
                            .data(List.of(UserDetailsDto.builder()
                                    .username("test_court_inactive")
                                    .email("test_court_inactive@email.gov.uk")
                                    .name("Court Inactive")
                                    .isActive(false)
                                    .lastSignIn(null)
                                    .userType(UserType.COURT)
                                    .roles(Set.of())
                                    .courts(List.of(UserCourtDto.builder()
                                        .primaryCourt(CourtDto.builder()
                                            .name("CHESTER")
                                            .locCode("415")
                                            .courtType(CourtType.MAIN).build())
                                        .satelliteCourts(List.of(CourtDto.builder()
                                                .name("WARRINGTON")
                                                .locCode("462")
                                                .courtType(CourtType.SATELLITE).build(),
                                            CourtDto.builder()
                                                .name("KNUTSFORD")
                                                .locCode("767")
                                                .courtType(CourtType.SATELLITE).build())).build()))
                                    .build(),
                                UserDetailsDto.builder()
                                    .username("test_court_manager")
                                    .email("test_court_manager@email.gov.uk")
                                    .name("Court Manager")
                                    .isActive(true)
                                    .lastSignIn(null)
                                    .userType(UserType.COURT)
                                    .roles(Set.of(Role.MANAGER))
                                    .courts(List.of(UserCourtDto.builder()
                                        .primaryCourt(CourtDto.builder()
                                            .name("CHESTER")
                                            .locCode("415")
                                            .courtType(CourtType.MAIN).build())
                                        .satelliteCourts(List.of(CourtDto.builder()
                                                .name("WARRINGTON")
                                                .locCode("462")
                                                .courtType(CourtType.SATELLITE).build(),
                                            CourtDto.builder()
                                                .name("KNUTSFORD")
                                                .locCode("767")
                                                .courtType(CourtType.SATELLITE).build())).build()))
                                    .build(),
                                UserDetailsDto.builder()
                                    .username("test_court_multi")
                                    .email("test_court_multi@email.gov.uk")
                                    .name("Court Multiple Linked")
                                    .isActive(true)
                                    .lastSignIn(null)
                                    .userType(UserType.COURT)
                                    .roles(Set.of())
                                    .courts(List.of(UserCourtDto.builder()
                                            .primaryCourt(CourtDto.builder()
                                                .name("CHESTER")
                                                .locCode("415")
                                                .courtType(CourtType.MAIN).build())
                                            .satelliteCourts(List.of(CourtDto.builder()
                                                    .name("WARRINGTON")
                                                    .locCode("462")
                                                    .courtType(CourtType.SATELLITE).build(),
                                                CourtDto.builder()
                                                    .name("KNUTSFORD")
                                                    .locCode("767")
                                                    .courtType(CourtType.SATELLITE).build())).build(),
                                        UserCourtDto.builder()
                                            .primaryCourt(CourtDto.builder()
                                                .name("WOLVERHAMPTON")
                                                .locCode("421")
                                                .courtType(CourtType.MAIN).build())
                                            .satelliteCourts(List.of(CourtDto.builder()
                                                .name("KIDDERMINSTER")
                                                .locCode("798")
                                                .courtType(CourtType.SATELLITE).build())).build()))
                                    .build()))
                            .build()
                    );
            }

            @Test
            void bureauManagerTypical() {
                testBuilder()
                    .jwt(createJwt("test_bureau_standard", "400",
                        UserType.BUREAU, Set.of(Role.MANAGER), "400"))
                    .triggerValid()
                    .assertEquals(PaginatedList.builder()
                        .currentPage(1L)
                        .totalPages(1L)
                        .totalItems(3L)
                        .data(List.of(UserDetailsDto.builder()
                                .username("test_bureau_inactive")
                                .email("test_bureau_inactive@email.gov.uk")
                                .name("Bureau Inactive")
                                .isActive(false)
                                .lastSignIn(null)
                                .userType(UserType.BUREAU)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_bureau_standard")
                                .email("test_bureau_standard@email.gov.uk")
                                .name("Bureau Standard")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.BUREAU)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_bureau_lead")
                                .email("test_bureau_lead@email.gov.uk")
                                .name("Bureau Team Lead")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.BUREAU)
                                .roles(Set.of(Role.MANAGER))
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build()))
                        .build());
            }

            @Test
            void searchByUserName() {
                UserSearchDto payload = getValidPayload();
                payload.setUserName("Inactive");
                testBuilder()
                    .payload(payload)
                    .jwt(createJwtAdministrator("test_admin_standard"))
                    .triggerValid()
                    .assertEquals(PaginatedList.builder()
                        .currentPage(1L)
                        .totalPages(1L)
                        .totalItems(3L)
                        .data(List.of(UserDetailsDto.builder()
                                .username("test_admin_inactive")
                                .email("test_admin_inactive@email.gov.uk")
                                .name("Admin Inactive")
                                .isActive(false)
                                .lastSignIn(null)
                                .userType(UserType.ADMINISTRATOR)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_bureau_inactive")
                                .email("test_bureau_inactive@email.gov.uk")
                                .name("Bureau Inactive")
                                .isActive(false)
                                .lastSignIn(null)
                                .userType(UserType.BUREAU)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_court_inactive")
                                .email("test_court_inactive@email.gov.uk")
                                .name("Court Inactive")
                                .isActive(false)
                                .lastSignIn(null)
                                .userType(UserType.COURT)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("CHESTER")
                                        .locCode("415")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of(CourtDto.builder()
                                            .name("WARRINGTON")
                                            .locCode("462")
                                            .courtType(CourtType.SATELLITE).build(),
                                        CourtDto.builder()
                                            .name("KNUTSFORD")
                                            .locCode("767")
                                            .courtType(CourtType.SATELLITE).build())).build()))
                                .build()))
                        .build());
            }

            @Test
            void searchByCourt() {
                UserSearchDto payload = getValidPayload();
                payload.setCourt("400");
                testBuilder()
                    .payload(payload)
                    .jwt(createJwtAdministrator("test_admin_standard"))
                    .triggerValid()
                    .assertEquals(PaginatedList.builder()
                        .currentPage(1L)
                        .totalPages(2L)
                        .totalItems(5L)
                        .data(List.of(UserDetailsDto.builder()
                                .username("test_admin_inactive")
                                .email("test_admin_inactive@email.gov.uk")
                                .name("Admin Inactive")
                                .isActive(false)
                                .lastSignIn(null)
                                .userType(UserType.ADMINISTRATOR)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_admin_standard")
                                .email("test_admin_standard@email.gov.uk")
                                .name("Admin Standard")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.ADMINISTRATOR)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_bureau_inactive")
                                .email("test_bureau_inactive@email.gov.uk")
                                .name("Bureau Inactive")
                                .isActive(false)
                                .lastSignIn(null)
                                .userType(UserType.BUREAU)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build()))
                        .build());
            }

            @Test
            void activeOnly() {
                UserSearchDto payload = getValidPayload();
                payload.setOnlyActive(true);
                testBuilder()
                    .payload(payload)
                    .jwt(createJwtAdministrator("test_admin_standard"))
                    .triggerValid()
                    .assertEquals(PaginatedList.builder()
                        .currentPage(1L)
                        .totalPages(3L)
                        .totalItems(9L)
                        .data(List.of(UserDetailsDto.builder()
                                .username("test_admin_standard")
                                .email("test_admin_standard@email.gov.uk")
                                .name("Admin Standard")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.ADMINISTRATOR)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_bureau_standard")
                                .email("test_bureau_standard@email.gov.uk")
                                .name("Bureau Standard")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.BUREAU)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_bureau_lead")
                                .email("test_bureau_lead@email.gov.uk")
                                .name("Bureau Team Lead")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.BUREAU)
                                .roles(Set.of(Role.MANAGER))
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build()))
                        .build());
            }

            @Test
            void customSort() {
                UserSearchDto payload = getValidPayload();
                payload.setSortMethod(SortMethod.DESC);
                payload.setSortField(UserSearchDto.SortField.USER_TYPE);
                payload.setPageLimit(5);
                payload.setPageNumber(2);
                testBuilder()
                    .payload(payload)
                    .jwt(createJwtAdministrator("test_admin_standard"))
                    .triggerValid()
                    .assertEquals(PaginatedList.builder()
                        .currentPage(2L)
                        .totalPages(3L)
                        .totalItems(14L)
                        .data(List.of(UserDetailsDto.builder()
                                .username("test_court_primary")
                                .email("test_court_primary@email.gov.uk")
                                .name("Court Primary Only")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.COURT)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("BRISTOL")
                                        .locCode("408")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_court_sjo")
                                .email("test_court_sjo@email.gov.uk")
                                .name("Court SJO")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.COURT)
                                .roles(Set.of(Role.SENIOR_JUROR_OFFICER))
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("CHESTER")
                                        .locCode("415")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of(CourtDto.builder()
                                            .name("WARRINGTON")
                                            .locCode("462")
                                            .courtType(CourtType.SATELLITE).build(),
                                        CourtDto.builder()
                                            .name("KNUTSFORD")
                                            .locCode("767")
                                            .courtType(CourtType.SATELLITE).build())).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_court_sjo_mangr")
                                .email("test_court_sjo_mangr@email.gov.uk")
                                .name("Court SJO & Manager")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.COURT)
                                .roles(Set.of(Role.MANAGER,
                                    Role.SENIOR_JUROR_OFFICER))
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("CHESTER")
                                        .locCode("415")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of(CourtDto.builder()
                                            .name("WARRINGTON")
                                            .locCode("462")
                                            .courtType(CourtType.SATELLITE).build(),
                                        CourtDto.builder()
                                            .name("KNUTSFORD")
                                            .locCode("767")
                                            .courtType(CourtType.SATELLITE).build())).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_court_standard")
                                .email("test_court_standard@email.gov.uk")
                                .name("Court Standard")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.COURT)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("CHESTER")
                                        .locCode("415")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of(CourtDto.builder()
                                            .name("WARRINGTON")
                                            .locCode("462")
                                            .courtType(CourtType.SATELLITE).build(),
                                        CourtDto.builder()
                                            .name("KNUTSFORD")
                                            .locCode("767")
                                            .courtType(CourtType.SATELLITE).build())).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_bureau_inactive")
                                .email("test_bureau_inactive@email.gov.uk")
                                .name("Bureau Inactive")
                                .isActive(false)
                                .lastSignIn(null)
                                .userType(UserType.BUREAU)
                                .roles(Set.of())
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build()))
                        .build());
            }

            @Test
            void managerSort() {
                UserSearchDto payload = getValidPayload();
                payload.setSortMethod(SortMethod.DESC);
                payload.setSortField(UserSearchDto.SortField.MANAGER);
                payload.setPageLimit(3);
                payload.setPageNumber(1);
                testBuilder()
                    .payload(payload)
                    .jwt(createJwtAdministrator("test_admin_standard"))
                    .triggerValid()
                    .assertEquals(PaginatedList.builder()
                        .currentPage(1L)
                        .totalPages(5L)
                        .totalItems(14L)
                        .data(List.of(
                            UserDetailsDto.builder()
                                .username("test_bureau_lead")
                                .email("test_bureau_lead@email.gov.uk")
                                .name("Bureau Team Lead")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.BUREAU)
                                .roles(Set.of(Role.MANAGER))
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("Jury Central Summoning Bureau")
                                        .locCode("400")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of()).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_court_manager")
                                .email("test_court_manager@email.gov.uk")
                                .name("Court Manager")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.COURT)
                                .roles(Set.of(Role.MANAGER))
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("CHESTER")
                                        .locCode("415")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of(CourtDto.builder()
                                            .name("WARRINGTON")
                                            .locCode("462")
                                            .courtType(CourtType.SATELLITE).build(),
                                        CourtDto.builder()
                                            .name("KNUTSFORD")
                                            .locCode("767")
                                            .courtType(CourtType.SATELLITE).build())).build()))
                                .build(),
                            UserDetailsDto.builder()
                                .username("test_court_sjo_mangr")
                                .email("test_court_sjo_mangr@email.gov.uk")
                                .name("Court SJO & Manager")
                                .isActive(true)
                                .lastSignIn(null)
                                .userType(UserType.COURT)
                                .roles(Set.of(Role.MANAGER, Role.SENIOR_JUROR_OFFICER))
                                .courts(List.of(UserCourtDto.builder()
                                    .primaryCourt(CourtDto.builder()
                                        .name("CHESTER")
                                        .locCode("415")
                                        .courtType(CourtType.MAIN).build())
                                    .satelliteCourts(List.of(CourtDto.builder()
                                            .name("WARRINGTON")
                                            .locCode("462")
                                            .courtType(CourtType.SATELLITE).build(),
                                        CourtDto.builder()
                                            .name("KNUTSFORD")
                                            .locCode("767")
                                            .courtType(CourtType.SATELLITE).build())).build()))
                                .build()))
                        .build());
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {
            @Test
            void isCourtUser() {
                testBuilder()
                    .jwt(createJwt(COURT_USER, "415", UserType.COURT,
                        Set.of(), "415"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void isBureauUser() {
                testBuilder()
                    .jwt(createJwt(BUREAU_USER, "400", UserType.BUREAU,
                        Set.of(), "400"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void invalidPayload() {
                UserSearchDto payload = getValidPayload();
                payload.setPageNumber(0);
                testBuilder()
                    .payload(payload)
                    .triggerInvalid()
                    .assertInvalidPayload(
                        new RestResponseEntityExceptionHandler.FieldError(
                            "pageNumber", "must be greater than or equal to 1")
                    );
            }
        }
    }

    @Nested
    @DisplayName("POST " + CreateUser.URL)
    class CreateUser extends AbstractControllerIntegrationTest<CreateUserDto, UsernameDto> {
        private static final String URL = BASE_URL + "/create";

        protected CreateUser() {
            super(HttpMethod.POST, template, HttpStatus.ACCEPTED);
        }

        @Override
        protected String getValidUrl() {
            return URL;
        }

        @Override
        protected String getValidJwt() {
            return createJwt("test_admin_standard", "400",
                UserType.ADMINISTRATOR, Set.of(), "400");
        }

        @Override
        protected CreateUserDto getValidPayload() {
            return CreateUserDto.builder()
                .userType(UserType.COURT)
                .email("test_new_user" + EMAIL_SUFFIX)
                .name("New User")
                .build();
        }

        @DisplayName("Positive")
        @Nested
        class Positive {

            void assertUserCreated(
                CreateUserDto userDto,
                String expectedUsername,
                String createdBy,
                String... courtLocCodes
            ) {
                transactionTemplate.execute(status -> {
                    User user = getUserFromUsername(expectedUsername);
                    assertThat(user.getUserType()).isEqualTo(userDto.getUserType());
                    assertThat(user.getEmail()).isEqualTo(userDto.getEmail());
                    assertThat(user.getName()).isEqualTo(userDto.getName());
                    assertThat(user.getRoles()).isEqualTo(Optional.ofNullable(userDto.getRoles()).orElse(Set.of()));
                    assertThat(user.isActive()).isTrue();
                    assertThat(user.getUsername()).isEqualTo(expectedUsername);
                    assertThat(user.getCreatedBy()).isEqualTo(createdBy);
                    assertThat(user.getUpdatedBy()).isEqualTo(createdBy);
                    assertThat(user.getApprovalLimit()).isEqualTo(
                        Optional.ofNullable(userDto.getApprovalLimit()).orElse(new BigDecimal("0.00")));
                    assertThat(user.getCourts()).hasSize(courtLocCodes.length);
                    assertThat(user.getCourts()
                        .stream().map(CourtLocation::getLocCode).toList())
                        .containsExactlyInAnyOrder(courtLocCodes);
                    return null;
                });
            }

            @Test
            void typicalCourt() {
                CreateUserDto userDto = getValidPayload();
                userDto.setUserType(UserType.COURT);
                testBuilder()
                    .payload(userDto)
                    .triggerValid()
                    .assertEquals(new UsernameDto("test_new_user"));
                assertUserCreated(userDto, "test_new_user", "test_admin_standard");
            }

            @Test
            void typicalBureau() {
                CreateUserDto userDto = getValidPayload();
                userDto.setUserType(UserType.BUREAU);
                testBuilder()
                    .payload(userDto)
                    .triggerValid()
                    .assertEquals(new UsernameDto("test_new_user"));
                assertUserCreated(userDto, "test_new_user", "test_admin_standard", "400");
            }

            @Test
            void typicalAdmin() {
                CreateUserDto userDto = getValidPayload();
                userDto.setUserType(UserType.ADMINISTRATOR);
                testBuilder()
                    .payload(userDto)
                    .triggerValid()
                    .assertEquals(new UsernameDto("test_new_user"));
                assertUserCreated(userDto, "test_new_user", "test_admin_standard", "400");
            }

            @Test
            void typicalDefaultUsernameExists() {
                CreateUserDto userDto = getValidPayload();
                userDto.setEmail("test_court_sjo" + EMAIL_SUFFIX + "1");
                userDto.setRoles(Set.of(Role.MANAGER));
                userDto.setApprovalLimit(new BigDecimal("31.20"));
                testBuilder()
                    .payload(userDto)
                    .triggerValid()
                    .assertEquals(new UsernameDto("test_court_sjo1"));
                assertUserCreated(userDto, "test_court_sjo1", "test_admin_standard");
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            @Test
            void isCourtUser() {
                testBuilder()
                    .jwt(createJwt(COURT_USER, "415", UserType.COURT,
                        Set.of(), "415"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void isBureauUser() {
                testBuilder()
                    .jwt(createJwt(BUREAU_USER, "400", UserType.BUREAU,
                        Set.of(), "400"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void invalidPayload() {
                CreateUserDto payload = getValidPayload();
                payload.setEmail(null);
                testBuilder()
                    .payload(payload)
                    .triggerInvalid()
                    .assertInvalidPayload(
                        new RestResponseEntityExceptionHandler.FieldError(
                            "email", "must not be blank")
                    );
            }

            @Test
            void emailAlreadyInUse() {
                CreateUserDto payload = getValidPayload();
                payload.setEmail("test_court_sjo" + EMAIL_SUFFIX);
                testBuilder()
                    .payload(payload)
                    .triggerInvalid()
                    .assertBusinessRuleViolation(
                        MojException.BusinessRuleViolation.ErrorCode.EMAIL_IN_USE,
                        "Email is already in use"
                    );
            }
        }
    }

    @Nested
    @DisplayName("GET " + GetUser.URL)
    class GetUser extends AbstractControllerIntegrationTest<Void, UserDetailsDto> {
        private static final String URL = BASE_URL + "/{username}";

        protected GetUser() {
            super(HttpMethod.GET, template, HttpStatus.OK);
        }


        private String toUrl(String username) {
            return URL.replace("{username}", username);
        }

        @Override
        protected String getValidUrl() {
            return toUrl("test_court_standard");
        }

        @Override
        protected String getValidJwt() {
            return createJwt("test_admin_standard", "400",
                UserType.ADMINISTRATOR, Set.of(), "400");
        }

        @Override
        protected Void getValidPayload() {
            return null;
        }

        @DisplayName("Positive")
        @Nested
        class Positive {
            @Test
            void courtManager() {
                testBuilder()
                    .jwt(createJwt("test_court_manager", Set.of(Role.MANAGER), "415", "415"))
                    .url(toUrl("test_court_standard"))
                    .triggerValid()
                    .assertEquals(UserDetailsDto.builder()
                        .username("test_court_standard")
                        .email("test_court_standard@email.gov.uk")
                        .name("Court Standard")
                        .isActive(true)
                        .lastSignIn(null)
                        .userType(UserType.COURT)
                        .roles(Set.of())
                        .courts(List.of(UserCourtDto.builder()
                            .primaryCourt(CourtDto.builder()
                                .name("CHESTER")
                                .locCode("415")
                                .courtType(CourtType.MAIN).build())
                            .satelliteCourts(List.of(CourtDto.builder()
                                    .name("WARRINGTON")
                                    .locCode("462")
                                    .courtType(CourtType.SATELLITE).build(),
                                CourtDto.builder()
                                    .name("KNUTSFORD")
                                    .locCode("767")
                                    .courtType(CourtType.SATELLITE).build())).build()))
                        .build());
            }

            @Test
            void bureauManager() {
                testBuilder()
                    .jwt(createJwtBureau("test_bureau_lead", Set.of(Role.MANAGER)))
                    .url(toUrl("test_bureau_standard"))
                    .triggerValid()
                    .assertEquals(UserDetailsDto.builder()
                        .username("test_bureau_standard")
                        .email("test_bureau_standard@email.gov.uk")
                        .name("Bureau Standard")
                        .isActive(true)
                        .lastSignIn(null)
                        .userType(UserType.BUREAU)
                        .roles(Set.of())
                        .courts(List.of(UserCourtDto.builder()
                            .primaryCourt(CourtDto.builder()
                                .name("Jury Central Summoning Bureau")
                                .locCode("400")
                                .courtType(CourtType.MAIN).build())
                            .satelliteCourts(List.of()).build()))
                        .build());
            }

            @Test
            void adminUser() {
                testBuilder()
                    .jwt(createJwtAdministrator("test_admin_standard"))
                    .url(toUrl("test_admin_inactive"))
                    .triggerValid()
                    .assertEquals(UserDetailsDto.builder()
                        .username("test_admin_inactive")
                        .email("test_admin_inactive@email.gov.uk")
                        .name("Admin Inactive")
                        .isActive(false)
                        .lastSignIn(null)
                        .userType(UserType.ADMINISTRATOR)
                        .roles(Set.of())
                        .courts(List.of(UserCourtDto.builder()
                            .primaryCourt(CourtDto.builder()
                                .name("Jury Central Summoning Bureau")
                                .locCode("400")
                                .courtType(CourtType.MAIN).build())
                            .satelliteCourts(List.of()).build()))
                        .build());
            }

            @Test
            void userDoesNotHaveCourtAsAdminUser() {
                testBuilder()
                    .jwt(createJwtAdministrator("test_admin_standard"))
                    .url(toUrl("test_court_standard"))
                    .triggerValid()
                    .assertEquals(UserDetailsDto.builder()
                        .username("test_court_standard")
                        .email("test_court_standard@email.gov.uk")
                        .name("Court Standard")
                        .isActive(true)
                        .lastSignIn(null)
                        .userType(UserType.COURT)
                        .roles(Set.of())
                        .courts(List.of(UserCourtDto.builder()
                            .primaryCourt(CourtDto.builder()
                                .name("CHESTER")
                                .locCode("415")
                                .courtType(CourtType.MAIN).build())
                            .satelliteCourts(List.of(CourtDto.builder()
                                    .name("WARRINGTON")
                                    .locCode("462")
                                    .courtType(CourtType.SATELLITE).build(),
                                CourtDto.builder()
                                    .name("KNUTSFORD")
                                    .locCode("767")
                                    .courtType(CourtType.SATELLITE).build())).build()))
                        .build());
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            @Test
            void userDoesNotHaveCourtAsCourtUser() {
                testBuilder()
                    .jwt(createJwt("test_court_manager", Set.of(Role.MANAGER), "415", "415"))
                    .url(toUrl("test_court_primary"))
                    .triggerInvalid()
                    .assertMojForbiddenResponse("User not part of court");
            }

            @Test
            void userDoesNotHaveCourtAsBureauUser() {
                testBuilder()
                    .jwt(createJwtBureau("test_bureau_lead", Set.of(Role.MANAGER)))
                    .url(toUrl("test_court_standard"))
                    .triggerInvalid()
                    .assertMojForbiddenResponse("User not part of court");
            }

            @Test
            void isCourtUserButNotManager() {
                testBuilder()
                    .jwt(createJwt("test_court_manager", "415",
                        UserType.COURT, Set.of(), "415"))
                    .url(toUrl("test_court_standard"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void isBureauUserButNotManager() {
                testBuilder()
                    .jwt(createJwt("test_bureau_lead", "400",
                        UserType.BUREAU, Set.of(), "400"))
                    .url(toUrl("test_bureau_standard"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void userNotFound() {
                testBuilder()
                    .url(toUrl("not_found"))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }
        }
    }

    @Nested
    @DisplayName("PUT " + UpdateUser.URL)
    class UpdateUser extends AbstractControllerIntegrationTest<UpdateUserDto, Void> {
        private static final String URL = BASE_URL + "/{username}";

        protected UpdateUser() {
            super(HttpMethod.PUT, template, HttpStatus.ACCEPTED);
        }

        private String toUrl(String username) {
            return URL.replace("{username}", username);
        }

        @Override
        protected String getValidUrl() {
            return toUrl("test_court_standard");
        }

        @Override
        protected String getValidJwt() {
            return createJwt("test_admin_standard", "400",
                UserType.ADMINISTRATOR, Set.of(), "400");
        }

        @Override
        protected UpdateUserDto getValidPayload() {
            return UpdateUserDto.builder()
                .email("test_new_email" + EMAIL_SUFFIX)
                .name("New Name")
                .isActive(true)
                .roles(Set.of(Role.MANAGER))
                .approvalLimit(new BigDecimal("31.20"))
                .build();
        }

        @DisplayName("Positive")
        @Nested
        class Positive {

            void assertUserUpdated(
                User oldUser,
                UpdateUserDto userDto,
                boolean isAdmin,
                String username,
                String updatedBy
            ) {
                transactionTemplate.execute(status -> {
                    User user = getUserFromUsername(username);
                    if (isAdmin) {
                        assertThat(user.getEmail()).isEqualTo(userDto.getEmail());
                        assertThat(user.getName()).isEqualTo(userDto.getName());
                        assertThat(user.isActive()).isEqualTo(userDto.getIsActive());
                        assertThat(user.getApprovalLimit()).isEqualTo(
                            Optional.ofNullable(userDto.getApprovalLimit()).orElse(new BigDecimal("0.00")));
                    } else {
                        assertThat(user.getEmail()).isEqualTo(oldUser.getEmail());
                        assertThat(user.getName()).isEqualTo(oldUser.getName());
                        assertThat(user.isActive()).isEqualTo(oldUser.isActive());
                        assertThat(user.getApprovalLimit()).isEqualTo(oldUser.getApprovalLimit());
                    }
                    assertThat(user.getRoles()).isEqualTo(Optional.ofNullable(userDto.getRoles()).orElse(Set.of()));
                    assertThat(user.getUpdatedBy()).isEqualTo(updatedBy);
                    return null;
                });
            }

            @Test
            void courtManager() {
                final String username = "test_court_standard";
                UpdateUserDto payload = getValidPayload();
                User userBeforeUpdate = getUserFromUsername(username);
                testBuilder()
                    .url(toUrl(username))
                    .jwt(createJwt("test_court_manager", Set.of(Role.MANAGER), "415", "415"))
                    .payload(payload)
                    .triggerValid()
                    .assertValidNoBody();
                assertUserUpdated(userBeforeUpdate, payload, false, username, "test_court_manager");
            }

            @Test
            void bureauManager() {
                final String username = "test_bureau_standard";
                UpdateUserDto payload = getValidPayload();
                User userBeforeUpdate = getUserFromUsername(username);
                testBuilder()
                    .url(toUrl(username))
                    .jwt(createJwtBureau("test_bureau_lead", Set.of(Role.MANAGER)))
                    .payload(payload)
                    .triggerValid()
                    .assertValidNoBody();
                assertUserUpdated(userBeforeUpdate, payload, false, username, "test_bureau_lead");
            }

            @Test
            void adminUser() {
                final String username = "test_admin_inactive";
                UpdateUserDto payload = getValidPayload();
                User userBeforeUpdate = getUserFromUsername(username);
                testBuilder()
                    .url(toUrl(username))
                    .jwt(createJwtAdministrator("test_admin_standard"))
                    .payload(payload)
                    .triggerValid()
                    .assertValidNoBody();
                assertUserUpdated(userBeforeUpdate, payload, true, username, "test_admin_standard");
            }

            @Test
            void userDoesNotHaveCourtAsAdminUser() {
                final String username = "test_court_manager";
                UpdateUserDto payload = getValidPayload();
                User userBeforeUpdate = getUserFromUsername(username);
                testBuilder()
                    .url(toUrl(username))
                    .jwt(createJwtAdministrator("test_admin_standard"))
                    .payload(payload)
                    .triggerValid()
                    .assertValidNoBody();
                assertUserUpdated(userBeforeUpdate, payload, true, username, "test_admin_standard");
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            @Test
            void userDoesNotHaveCourtAsCourtUser() {
                testBuilder()
                    .jwt(createJwt("test_court_manager", Set.of(Role.MANAGER), "415", "415"))
                    .url(toUrl("test_court_primary"))
                    .triggerInvalid()
                    .assertMojForbiddenResponse("User not part of court");
            }

            @Test
            void userDoesNotHaveCourtAsBureauUser() {
                testBuilder()
                    .jwt(createJwtBureau("test_bureau_lead", Set.of(Role.MANAGER)))
                    .url(toUrl("test_court_standard"))
                    .triggerInvalid()
                    .assertMojForbiddenResponse("User not part of court");
            }

            @Test
            void isCourtUserButNotManager() {
                testBuilder()
                    .jwt(createJwt("test_court_manager", "415",
                        UserType.COURT, Set.of(), "415"))
                    .url(toUrl("test_court_standard"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void isBureauUserButNotManager() {
                testBuilder()
                    .jwt(createJwt("test_bureau_lead", "400",
                        UserType.BUREAU, Set.of(), "400"))
                    .url(toUrl("test_bureau_standard"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void userNotFound() {
                testBuilder()
                    .url(toUrl("not_found"))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }

            @Test
            void invalidPayload() {
                UpdateUserDto payload = getValidPayload();
                payload.setEmail(null);
                testBuilder()
                    .payload(payload)
                    .triggerInvalid()
                    .assertInvalidPayload(
                        new RestResponseEntityExceptionHandler.FieldError(
                            "email", "must not be blank")
                    );
            }
        }
    }

    @Nested
    @DisplayName("PATCH " + AddCourt.URL)
    class AddCourt extends AbstractControllerIntegrationTest<List<String>, Void> {
        private static final String URL = BASE_URL + "/{username}/courts";

        protected AddCourt() {
            super(HttpMethod.PATCH, template, HttpStatus.ACCEPTED);
        }

        private String toUrl(String username) {
            return URL.replace("{username}", username);
        }

        @Override
        protected String getValidUrl() {
            return toUrl("test_court_multi");
        }

        @Override
        protected String getValidJwt() {
            return createJwt("test_admin_standard", "400",
                UserType.ADMINISTRATOR, Set.of(), "400");
        }

        @Override
        protected List<String> getValidPayload() {
            return List.of("415");
        }

        @DisplayName("Positive")
        @Nested
        class Positive {

            @Test
            void typical() {
                assertUserHasCourts("test_court_multi", "415", "421");
                testBuilder()
                    .payload(List.of("466"))
                    .triggerValid()
                    .assertValidNoBody();
                assertUserHasCourts("test_court_multi", "415", "421", "466");
                assertUserIsUpdatedBy("test_court_multi", SYSTEM_USER);
            }

            @Test
            void userDoesAlreadyHasCourt() {
                assertUserHasCourts("test_court_multi", "415", "421");
                testBuilder()
                    .payload(List.of("415"))
                    .triggerValid()
                    .assertValidNoBody();
                assertUserHasCourts("test_court_multi", "415", "421");
                assertUserIsUpdatedBy("test_court_multi", SYSTEM_USER);
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            @Test
            void isBureauUser() {
                testBuilder()
                    .jwt(createJwt(BUREAU_USER, "400", UserType.BUREAU,
                        Set.of(), "400"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void isCourtUser() {
                testBuilder()
                    .jwt(createJwt(COURT_USER, "415", UserType.COURT,
                        Set.of(), "415"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void courtNotFound() {
                testBuilder()
                    .payload(List.of("666"))
                    .triggerInvalid()
                    .assertNotFound("Court not found");
            }

            @Test
            void userNotFound() {
                testBuilder()
                    .url(toUrl("not_found"))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }

            @Test
            void emptyCourts() {
                testBuilder()
                    .payload(List.of())
                    .triggerInvalid()
                    .assertInvalidPathParam("addCourt.courts: must not be empty");
            }

            @Test
            void invalidCourtCode() {
                testBuilder()
                    .payload(List.of(TestConstants.INVALID_COURT_LOCATION))
                    .triggerInvalid()
                    .assertInvalidPathParam("addCourt.courts[0].<list element>: must match \"^\\d{3}$\"");
            }
        }
    }

    @Nested
    @DisplayName("DELETE " + RemoveCourt.URL)
    class RemoveCourt extends AbstractControllerIntegrationTest<List<String>, Void> {
        private static final String URL = BASE_URL + "/{username}/courts";

        protected RemoveCourt() {
            super(HttpMethod.DELETE, template, HttpStatus.ACCEPTED);
        }

        private String toUrl(String username) {
            return URL.replace("{username}", username);
        }

        @Override
        protected String getValidUrl() {
            return toUrl("test_court_multi");
        }

        @Override
        protected String getValidJwt() {
            return createJwt("test_admin_standard", "400",
                UserType.ADMINISTRATOR, Set.of(), "400");
        }

        @Override
        protected List<String> getValidPayload() {
            return List.of("415");
        }

        @DisplayName("Positive")
        @Nested
        class Positive {

            @Test
            void typical() {
                assertUserHasCourts("test_court_multi", "415", "421");
                testBuilder()
                    .payload(List.of("415"))
                    .triggerValid()
                    .assertValidNoBody();
                assertUserHasCourts("test_court_multi", "421");
                assertUserIsUpdatedBy("test_court_multi", SYSTEM_USER);
            }


            @Test
            void userDoesNotHaveCourt() {
                assertUserHasCourts("test_court_multi", "415", "421");
                testBuilder()
                    .payload(List.of("466"))
                    .triggerValid()
                    .assertValidNoBody();
                assertUserHasCourts("test_court_multi", "415", "421");
                assertUserIsUpdatedBy("test_court_multi", SYSTEM_USER);
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            @Test
            void isBureauUser() {
                testBuilder()
                    .jwt(createJwt(BUREAU_USER, "400", UserType.BUREAU,
                        Set.of(), "400"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void isCourtUser() {
                testBuilder()
                    .jwt(createJwt(COURT_USER, "415", UserType.COURT,
                        Set.of(), "415"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void userNotFound() {
                testBuilder()
                    .url(toUrl("not_found"))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }

            @Test
            void emptyCourts() {
                testBuilder()
                    .payload(List.of())
                    .triggerInvalid()
                    .assertInvalidPathParam("removeCourt.courts: must not be empty");
            }

            @Test
            void invalidCourtCode() {
                testBuilder()
                    .payload(List.of(TestConstants.INVALID_COURT_LOCATION))
                    .triggerInvalid()
                    .assertInvalidPathParam("removeCourt.courts[0].<list element>: must match \"^\\d{3}$\"");
            }
        }
    }

    @Nested
    @DisplayName("PATCH " + UpdateUserType.URL)
    class UpdateUserType extends AbstractControllerIntegrationTest<Void, Void> {
        private static final String URL = BASE_URL + "/{username}/type/{type}";

        protected UpdateUserType() {
            super(HttpMethod.PATCH, template, HttpStatus.ACCEPTED);
        }

        private String toUrl(String username, UserType userType) {
            return toUrl(username, userType.name());
        }

        private String toUrl(String username, String userType) {
            return URL.replace("{username}", username)
                .replace("{type}", userType);
        }

        @Override
        protected String getValidUrl() {
            return toUrl("test_court_standard", UserType.BUREAU);
        }

        @Override
        protected String getValidJwt() {
            return createJwt("test_admin_standard", "400",
                UserType.ADMINISTRATOR, Set.of(), "400");
        }

        @Override
        protected Void getValidPayload() {
            return null;
        }


        @DisplayName("Positive")
        @Nested
        class Positive {
            private void assertUser(String username, UserType userType, Set<Role> roles,
                                    String updatedBy, String... locCodes) {
                transactionTemplate.execute(status -> {
                    User user = getUserFromUsername(username);
                    assertThat(user.getUserType()).isEqualTo(userType);
                    assertThat(user.getCourts()).hasSize(locCodes.length);
                    assertThat(user.getCourts()
                        .stream().map(CourtLocation::getLocCode).toList())
                        .containsExactlyInAnyOrder(locCodes);
                    assertThat(user.getUpdatedBy()).isEqualTo(updatedBy);
                    assertThat(user.getRoles()).isEqualTo(roles);
                    return null;
                });
            }

            @Test
            void typicalCourtToCourt() {
                testBuilder()
                    .url(toUrl("test_court_manager", UserType.COURT))
                    .triggerValid()
                    .assertValidNoBody();
                assertUser("test_court_manager", UserType.COURT, Set.of(Role.MANAGER), SYSTEM_USER, "415");
            }

            @Test
            void typicalAdminToCourt() {
                testBuilder()
                    .url(toUrl("test_admin_standard", UserType.COURT))
                    .triggerValid()
                    .assertValidNoBody();
                assertUser("test_admin_standard", UserType.COURT, Set.of(), "test_admin_standard");
            }


            @Test
            void typicalAdminToBureau() {
                testBuilder()
                    .url(toUrl("test_admin_standard", UserType.BUREAU))
                    .triggerValid()
                    .assertValidNoBody();
                assertUser("test_admin_standard", UserType.BUREAU, Set.of(), "test_admin_standard", "400");
            }

            @Test
            void typicalBureauToCourt() {
                testBuilder()
                    .url(toUrl("test_bureau_standard", UserType.COURT))
                    .triggerValid()
                    .assertValidNoBody();
                assertUser("test_bureau_standard", UserType.COURT, Set.of(), "test_admin_standard");
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            @Test
            void invalidUserType() {
                testBuilder()
                    .url(toUrl("test_court_standard", "INVALID"))
                    .triggerInvalid()
                    .assertInvalidPathParam(
                        "INVALID is the incorrect data type or is not in the expected format (type)");
            }

            @Test
            void isBureauUser() {
                testBuilder()
                    .jwt(createJwt(BUREAU_USER, "400", UserType.BUREAU,
                        Set.of(), "400"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void isCourtUser() {
                testBuilder()
                    .jwt(createJwt(COURT_USER, "415", UserType.COURT,
                        Set.of(), "415"))
                    .triggerInvalid()
                    .assertForbiddenResponse();
            }

            @Test
            void userNotFound() {
                testBuilder()
                    .url(toUrl("not_found", UserType.BUREAU))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }
        }
    }
}
