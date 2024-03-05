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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.administration.CourtRoomDto;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + AdministrationCourtRoomControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.ExcessiveImports")
public class AdministrationCourtRoomControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/moj/administration/court-rooms";

    private HttpHeaders httpHeaders;
    private final TestRestTemplate template;

    @Autowired
    private final CourtroomRepository courtroomRepository;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }


    static CourtRoomDto getValidCourtRoomDto() {
        return getValidCourtRoomDto(1);
    }

    static CourtRoomDto getValidCourtRoomDto(int id) {
        return CourtRoomDto.builder()
            .roomName("TST" + id)
            .roomDescription("Test: Courtroom " + id)
            .build();
    }

    @Nested
    @DisplayName("GET " + ViewCourtRoomsDetails.URL)
    @Sql(value = {"/db/administration/tearDownCourtRooms.sql",
        "/db/administration/createCourtRooms.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownCourtRooms.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class ViewCourtRoomsDetails {
        public static final String URL = BASE_URL + "/{loc_code}";

        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            void assertValid(String locCode, CourtRoomDto... expectedResponse) {
                final String jwt =
                    createBureauJwt(COURT_USER, locCode, UserType.COURT, Set.of(Role.MANAGER), locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<List<CourtRoomDto>> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(locCode))),
                    new ParameterizedTypeReference<>() {
                    });
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).isEqualTo(List.of(expectedResponse));
            }

            @Test
            void typicalCourt1() {
                assertValid("415",
                    getValidCourtRoomDto(1),
                    getValidCourtRoomDto(2),
                    getValidCourtRoomDto(5)
                );
            }

            @Test
            void typicalCourt2() {
                assertValid("416",
                    getValidCourtRoomDto(3),
                    getValidCourtRoomDto(4)
                );
            }

            @Test
            void noData() {
                assertValid("998");
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode) {
                return triggerInvalid(owner, urlLocCode, UserType.COURT, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode,
                                                          UserType userType, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, userType, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(urlLocCode))),
                    String.class);
            }

            @Test
            void invalidLocCode() {
                assertInvalidPathParam(triggerInvalid("INVALID", "INVALID"),
                    "viewCourtRoomsDetails.locCode: must match \"^\\d{3}$\"");
            }

            @Test
            void unauthorisedNotManagerUser() {
                assertForbiddenResponse(triggerInvalid("415", "415", UserType.COURT, Set.of(Role.COURT_OFFICER)),
                    toUrl("415"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                assertForbiddenResponse(triggerInvalid("415", "416"),
                    toUrl("416"));
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid("400", "400", UserType.BUREAU, Set.of(Role.BUREAU_OFFICER)),
                    toUrl("400"));
            }
        }
    }

    @Nested
    @DisplayName("POST " + CreateCourtRoom.URL)
    class CreateCourtRoom {
        public static final String URL = BASE_URL + "/{loc_code}";

        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            void assertValid(String locCode, CourtRoomDto request) {
                final String jwt =
                    createBureauJwt(COURT_USER, locCode, UserType.COURT, Set.of(Role.MANAGER), locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(toUrl(locCode))),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be accepted")
                    .isEqualTo(HttpStatus.ACCEPTED);
                assertThat(response.getBody()).isNull();

                Courtroom courtRoom =
                    courtroomRepository.findByCourtLocationLocCodeAndRoomNumber(locCode, request.getRoomName());

                assertThat(courtRoom).isNotNull();
                assertThat(courtRoom.getRoomNumber()).isEqualTo(request.getRoomName());
                assertThat(courtRoom.getDescription()).isEqualTo(request.getRoomDescription());
                assertThat(courtRoom.getCourtLocation().getLocCode()).isEqualTo(locCode);
            }

            @Test
            void typicalCourt1() {
                assertValid("415", getValidCourtRoomDto(9));
            }

            @Test
            void typicalCourt2() {
                assertValid("416", getValidCourtRoomDto(8));
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode, CourtRoomDto request) {
                return triggerInvalid(owner, urlLocCode, request, UserType.COURT, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode,
                                                          CourtRoomDto request,
                                                          UserType userType, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, userType, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(toUrl(urlLocCode))),
                    String.class);
            }

            @Test
            void invalidLocCode() {
                assertInvalidPathParam(triggerInvalid("INVALID", "INVALID", getValidCourtRoomDto()),
                    "createCourtRoom.locCode: must match \"^\\d{3}$\"");
            }

            @Test
            void unauthorisedNotManagerUser() {
                assertForbiddenResponse(triggerInvalid("415", "415", getValidCourtRoomDto(),
                        UserType.COURT, Set.of(Role.COURT_OFFICER)),
                    toUrl("415"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                assertForbiddenResponse(triggerInvalid("415", "416", getValidCourtRoomDto()),
                    toUrl("416"));
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid("400", "400", getValidCourtRoomDto(), UserType.BUREAU,
                        Set.of(Role.BUREAU_OFFICER)),
                    toUrl("400"));
            }


            @Test
            void invalidPayload() {
                CourtRoomDto createCourtRoom = getValidCourtRoomDto();
                createCourtRoom.setRoomName(null);
                assertInvalidPayload(triggerInvalid("415", "416", createCourtRoom),
                    new RestResponseEntityExceptionHandler.FieldError("roomName", "must not be blank"));
            }

            @Test
            void courtRoomNotFound() {
                assertNotFound(triggerInvalid("998", "998", getValidCourtRoomDto()),
                    toUrl("998"), "Court location not found");
            }
        }
    }

    @Nested
    @DisplayName("GET " + ViewCourtRoomDetails.URL)
    @Sql(value = {"/db/administration/tearDownCourtRooms.sql",
        "/db/administration/createCourtRooms.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownCourtRooms.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class ViewCourtRoomDetails {
        public static final String URL = BASE_URL + "/{loc_code}/{id}";

        private String toUrl(String locCode, Long id) {
            return toUrl(locCode, id.toString());
        }

        private String toUrl(String locCode, String id) {
            return URL.replace("{loc_code}", locCode)
                .replace("{id}", id);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            void assertValid(String locCode, Long id, CourtRoomDto expectedResponse) {
                final String jwt =
                    createBureauJwt(COURT_USER, locCode, UserType.COURT, Set.of(Role.MANAGER), locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<CourtRoomDto> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(locCode, id))),
                    CourtRoomDto.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).isEqualTo(expectedResponse);
            }

            @Test
            void typicalCourt1() {
                assertValid("415", 99_991L, getValidCourtRoomDto(1));
            }

            @Test
            void typicalCourt2() {
                assertValid("416", 99_994L, getValidCourtRoomDto(4));
            }

        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode, String id) {
                return triggerInvalid(owner, urlLocCode, id, UserType.COURT, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode,
                                                          String id,
                                                          UserType userType, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, userType, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(urlLocCode, id))),
                    String.class);
            }

            @Test
            void invalidLocCode() {
                assertInvalidPathParam(triggerInvalid("INVALID", "INVALID", "1"),
                    "viewCourtRoomDetails.locCode: must match \"^\\d{3}$\"");
            }

            @Test
            void invalidId() {
                assertInvalidPathParam(triggerInvalid("415", "415", "INVALID"),
                    "INVALID is the incorrect data type or is not in the expected format (id)");
            }

            @Test
            void unauthorisedNotManagerUser() {
                assertForbiddenResponse(triggerInvalid("415", "415", "1", UserType.COURT, Set.of(Role.COURT_OFFICER)),
                    toUrl("415", "1"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                assertForbiddenResponse(triggerInvalid("415", "416", "1"),
                    toUrl("416", "1"));
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid("400", "400", "1", UserType.BUREAU, Set.of(Role.BUREAU_OFFICER)),
                    toUrl("400", "1"));
            }

            @Test
            void notFound() {
                assertNotFound(triggerInvalid("415", "415", "999"),
                    toUrl("415", "999"), "Court room not found");
            }
        }
    }

    @Nested
    @DisplayName("PUT " + UpdateCourtRoom.URL)
    @Sql(value = {"/db/administration/tearDownCourtRooms.sql",
        "/db/administration/createCourtRooms.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownCourtRooms.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class UpdateCourtRoom {
        public static final String URL = BASE_URL + "/{loc_code}/{id}";

        private String toUrl(String locCode, Long id) {
            return toUrl(locCode, id.toString());
        }

        private String toUrl(String locCode, String id) {
            return URL.replace("{loc_code}", locCode)
                .replace("{id}", id);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            void assertValid(String locCode, Long id, CourtRoomDto request) {
                final String jwt =
                    createBureauJwt(COURT_USER, locCode, UserType.COURT, Set.of(Role.MANAGER), locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, PUT,
                        URI.create(toUrl(locCode, id))),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be accepted")
                    .isEqualTo(HttpStatus.ACCEPTED);
                assertThat(response.getBody()).isNull();

                Courtroom courtRoom =
                    courtroomRepository.findById(id).orElseThrow(() -> new AssertionError("Court room not found"));

                assertThat(courtRoom).isNotNull();
                assertThat(courtRoom.getRoomNumber()).isEqualTo(request.getRoomName());
                assertThat(courtRoom.getDescription()).isEqualTo(request.getRoomDescription());
                assertThat(courtRoom.getCourtLocation().getLocCode()).isEqualTo(locCode);
            }

            @Test
            void typicalCourt1() {
                assertValid("415", 99_991L, getValidCourtRoomDto(7));
            }

            @Test
            void typicalCourt2() {
                assertValid("416", 99_994L, getValidCourtRoomDto(6));
            }

        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode,
                                                          String id, CourtRoomDto request) {
                return triggerInvalid(owner, urlLocCode, id, request, UserType.COURT, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode,
                                                          String id, CourtRoomDto request,
                                                          UserType userType, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, userType, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, PUT,
                        URI.create(toUrl(urlLocCode, id))),
                    String.class);
            }

            @Test
            void invalidLocCode() {
                assertInvalidPathParam(triggerInvalid("INVALID", "INVALID", "1", getValidCourtRoomDto()),
                    "updateCourtRoom.locCode: must match \"^\\d{3}$\"");
            }

            @Test
            void invalidId() {
                assertInvalidPathParam(triggerInvalid("415", "415", "INVALID", getValidCourtRoomDto()),
                    "INVALID is the incorrect data type or is not in the expected format (id)");
            }

            @Test
            void unauthorisedNotManagerUser() {
                assertForbiddenResponse(triggerInvalid("415", "415", "1", getValidCourtRoomDto(),
                        UserType.COURT, Set.of(Role.COURT_OFFICER)),
                    toUrl("415", "1"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                assertForbiddenResponse(triggerInvalid("415", "416", "1", getValidCourtRoomDto()),
                    toUrl("416", "1"));
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid("400", "400", "1", getValidCourtRoomDto(),
                        UserType.BUREAU, Set.of(Role.BUREAU_OFFICER)),
                    toUrl("400", "1"));
            }


            @Test
            void invalidPayload() {
                CourtRoomDto createCourtRoom = getValidCourtRoomDto();
                createCourtRoom.setRoomName(null);
                assertInvalidPayload(triggerInvalid("415", "416", "1", createCourtRoom),
                    new RestResponseEntityExceptionHandler.FieldError("roomName", "must not be blank"));
            }

            @Test
            void courtRoomNotFound() {
                assertNotFound(triggerInvalid("998", "998", "1", getValidCourtRoomDto()),
                    toUrl("998", "1"), "Court room not found");
            }
        }
    }
}
