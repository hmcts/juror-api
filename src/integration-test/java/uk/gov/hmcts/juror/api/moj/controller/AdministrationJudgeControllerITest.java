package uk.gov.hmcts.juror.api.moj.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeCreateDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeUpdateDto;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.trial.JudgeRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + AdministrationJudgeControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.ExcessiveImports")
public class AdministrationJudgeControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/moj/administration/judges";

    private HttpHeaders httpHeaders;
    private final TestRestTemplate template;

    @Autowired
    private final JudgeRepository judgeRepository;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("GET  " + ViewJudgeDetails.URL)
    @Sql(value = {"/db/administration/tearDownJudges.sql",
        "/db/administration/createJudges.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownJudges.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class ViewJudgeDetails {
        public static final String URL = BASE_URL + "/{judge_id}";

        private String toUrl(long judgeId) {
            return toUrl(String.valueOf(judgeId));
        }

        private String toUrl(String judgeId) {
            return URL.replace("{judge_id}", judgeId);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            void assertValid(long judgeCode, JudgeDetailsDto expectedResponse) {
                final String jwt =
                    createBureauJwt(COURT_USER, "415", UserType.COURT, Set.of(Role.MANAGER), "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<JudgeDetailsDto> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(judgeCode))),
                    JudgeDetailsDto.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).isEqualTo(expectedResponse);
            }

            @Test
            void typicalActiveTrue() {
                assertValid(999_991,
                    JudgeDetailsDto.builder()
                        .judgeId(999_991)
                        .judgeName("JUDGE1")
                        .judgeCode("TST1")
                        .isActive(true)
                        .lastUsed(LocalDateTime.of(2023, 1, 5, 9, 30))
                        .build());
            }

            @Test
            void typicalActiveFalse() {
                assertValid(999_992,
                    JudgeDetailsDto.builder()
                        .judgeId(999_992)
                        .judgeName("JUDGE2")
                        .judgeCode("TST2")
                        .isActive(false)
                        .lastUsed(LocalDateTime.of(2023, 2, 5, 9, 31))
                        .build());
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(String id, String owner) {
                return triggerInvalid(id, owner, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String id, String owner, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, UserType.COURT, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(id))),
                    String.class);
            }

            @Test
            void invalidJudgeId() throws JsonProcessingException {
                assertInvalidPathParam(triggerInvalid("INVALID", "415"),
                    "INVALID is the incorrect data type or is not in the expected format (judge_id)");
            }

            @Test
            void unauthorisedNotManager() {
                assertForbiddenResponse(triggerInvalid("999991", "415", Set.of(Role.COURT_OFFICER)),
                    toUrl("999991"));
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid("999991", "400"),
                    toUrl("999991"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                //Judge owner is 415 logged in user is 416 should not be able to view
                assertMojForbiddenResponse(triggerInvalid("999991", "416"),
                    toUrl("999991"), "User does not have access");
            }

            @Test
            void judgeNotFound() {
                assertNotFound(triggerInvalid("1999991", "416"),
                    toUrl("1999991"), "Judge not found");
            }
        }
    }

    @Nested
    @DisplayName("DELETE  " + DeleteJudge.URL)
    @Sql(value = {"/db/administration/tearDownJudges.sql",
        "/db/administration/createJudges.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownJudges.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class DeleteJudge {
        public static final String URL = BASE_URL + "/{judge_id}";

        private String toUrl(long judgeId) {
            return toUrl(String.valueOf(judgeId));
        }

        private String toUrl(String judgeId) {
            return URL.replace("{judge_id}", judgeId);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            void assertValid(long judgeCode) {
                final String jwt =
                    createBureauJwt(COURT_USER, "415", UserType.COURT, Set.of(Role.MANAGER), "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(httpHeaders, DELETE,
                        URI.create(toUrl(judgeCode))),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be accepted")
                    .isEqualTo(HttpStatus.ACCEPTED);
                assertThat(response.getBody()).isNull();
            }

            @Test
            void typical() {
                assertValid(999_993);
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(String id, String owner) {
                return triggerInvalid(id, owner, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String id, String owner, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, UserType.COURT, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, DELETE,
                        URI.create(toUrl(id))),
                    String.class);
            }

            @Test
            void invalidJudgeId() throws JsonProcessingException {
                assertInvalidPathParam(triggerInvalid("INVALID", "415"),
                    "INVALID is the incorrect data type or is not in the expected format (judge_id)");
            }

            @Test
            void unauthorisedNotManager() {
                assertForbiddenResponse(triggerInvalid("999993", "415", Set.of(Role.COURT_OFFICER)),
                    toUrl("999993"));
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid("999993", "400"),
                    toUrl("999993"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                //Judge owner is 415 logged in user is 416 should not be able to view
                assertMojForbiddenResponse(triggerInvalid("999993", "416"),
                    toUrl("999993"), "User does not have access");
            }

            @Test
            void judgeNotFound() {
                assertNotFound(triggerInvalid("1999991", "416"),
                    toUrl("1999991"), "Judge not found");
            }

            @Test
            void judgeHasBeenUsed() {
                assertBusinessRuleViolation(triggerInvalid("999991", "415"),
                    "Judge has been used and cannot be deleted",
                    MojException.BusinessRuleViolation.ErrorCode.CANNOT_DELETE_USED_JUDGE);
            }
        }
    }


    @Nested
    @DisplayName("GET  " + ViewAllJudgeDetails.URL)
    @Sql(value = {"/db/administration/tearDownJudges.sql",
        "/db/administration/createJudges.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownJudges.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class ViewAllJudgeDetails {
        public static final String URL = BASE_URL;

        private ViewAllJudgeDetails() {

        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            void assertValid(String owner, Boolean isActive, List<JudgeDetailsDto> expectedResponse) {
                final String jwt =
                    createBureauJwt(COURT_USER, owner, UserType.COURT, Set.of(Role.MANAGER), owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                String suffix = "";
                if (isActive != null) {
                    suffix = "?is_active=" + isActive;
                }

                ResponseEntity<List<JudgeDetailsDto>> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(URL + suffix)),
                    new ParameterizedTypeReference<>() {
                    });
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).isEqualTo(expectedResponse);
            }

            @Test
            void typicalNoFilter() {
                assertValid("415", null, List.of(
                    JudgeDetailsDto.builder()
                        .judgeId(999_991)
                        .judgeName("JUDGE1")
                        .judgeCode("TST1")
                        .isActive(true)
                        .lastUsed(LocalDateTime.of(2023, 1, 5, 9, 30))
                        .build(),
                    JudgeDetailsDto.builder()
                        .judgeId(999_992)
                        .judgeName("JUDGE2")
                        .judgeCode("TST2")
                        .isActive(false)
                        .lastUsed(LocalDateTime.of(2023, 2, 5, 9, 31))
                        .build(),
                    JudgeDetailsDto.builder()
                        .judgeId(999_993)
                        .judgeName("JUDGE3")
                        .judgeCode("TST3")
                        .isActive(false)
                        .lastUsed(null)
                        .build()
                ));
            }

            @Test
            void typicalIsActiveTrue() {
                assertValid("415", true, List.of(
                    JudgeDetailsDto.builder()
                        .judgeId(999_991)
                        .judgeName("JUDGE1")
                        .judgeCode("TST1")
                        .isActive(true)
                        .lastUsed(LocalDateTime.of(2023, 1, 5, 9, 30))
                        .build()
                ));
            }

            @Test
            void typicalIsActiveFalse() {
                assertValid("415", false, List.of(
                    JudgeDetailsDto.builder()
                        .judgeId(999_992)
                        .judgeName("JUDGE2")
                        .judgeCode("TST2")
                        .isActive(false)
                        .lastUsed(LocalDateTime.of(2023, 2, 5, 9, 31))
                        .build(),
                    JudgeDetailsDto.builder()
                        .judgeId(999_993)
                        .judgeName("JUDGE3")
                        .judgeCode("TST3")
                        .isActive(false)
                        .lastUsed(null)
                        .build()
                ));
            }

            @Test
            void notData() {
                assertValid("416", false, List.of());
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(String id, String owner) {
                return triggerInvalid(id, owner, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String isActive, String owner, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, UserType.COURT, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                String suffix = "";
                if (isActive != null) {
                    suffix = "?is_active=" + isActive;
                }

                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(URL + suffix)),
                    String.class);
            }

            @Test
            void invalidIsActiveFlag() throws JsonProcessingException {
                assertInvalidPathParam(triggerInvalid("INVALID", "415"),
                    "INVALID is the incorrect data type or is not in the expected format (is_active)");
            }

            @Test
            void unauthorisedNotManager() {
                assertForbiddenResponse(triggerInvalid(null, "415", Set.of(Role.COURT_OFFICER)), URL);
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid(null, "400"), URL);
            }
        }
    }

    @Nested
    @DisplayName("POST  " + CreateJudgeDetails.URL)
    @Sql(value = {"/db/administration/tearDownJudges.sql",
        "/db/administration/createJudges.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownJudges.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class CreateJudgeDetails {
        public static final String URL = BASE_URL;

        private CreateJudgeDetails() {

        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            void assertValid(JudgeCreateDto request) {
                final String jwt =
                    createBureauJwt(COURT_USER, "415", UserType.COURT, Set.of(Role.MANAGER), "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(URL)),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.ACCEPTED);
                assertThat(response.getBody()).isNull();

                Judge judge =
                    judgeRepository.findByOwnerAndCode("415", request.getJudgeCode())
                        .orElseThrow(() -> new AssertionError("Judge not found"));
                assertThat(judge.getCode()).isEqualTo(request.getJudgeCode());
                assertThat(judge.getName()).isEqualTo(request.getJudgeName());
                assertThat(judge.isActive()).isTrue();
            }

        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(JudgeCreateDto request, String owner) {
                return triggerInvalid(request, owner, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(JudgeCreateDto request, String owner,
                                                          Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, UserType.COURT, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(URL)),
                    String.class);
            }

            private JudgeCreateDto getValidPayload() {
                return JudgeCreateDto.builder()
                    .judgeCode("CD1")
                    .judgeName("New Name")
                    .build();
            }


            @Test
            void unauthorisedNotManager() {
                assertForbiddenResponse(triggerInvalid(getValidPayload(), "415", Set.of(Role.COURT_OFFICER)),
                    URL);
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid(getValidPayload(), "400"),
                    URL);
            }

            @Test
            void codeAlreadyInUse() {
                JudgeCreateDto payload = getValidPayload();
                payload.setJudgeCode("TST2");
                assertBusinessRuleViolation(triggerInvalid(payload, "415"),
                    "Judge with this code already exists",
                    MojException.BusinessRuleViolation.ErrorCode.CODE_ALREADY_IN_USE);
            }

            @Test
            void invalidPayload() {
                JudgeCreateDto payload = getValidPayload();
                payload.setJudgeCode(null);
                assertInvalidPayload(triggerInvalid(payload, "416"),
                    new RestResponseEntityExceptionHandler.FieldError("judgeCode", "must not be blank"));
            }
        }
    }

    @Nested
    @DisplayName("PUT  " + UpdateJudgeDetails.URL)
    @Sql(value = {"/db/administration/tearDownJudges.sql",
        "/db/administration/createJudges.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownJudges.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class UpdateJudgeDetails {
        public static final String URL = BASE_URL + "/{judge_id}";

        private String toUrl(long judgeId) {
            return toUrl(String.valueOf(judgeId));
        }

        private String toUrl(String judgeId) {
            return URL.replace("{judge_id}", judgeId);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            void assertValid(long judgeCode, JudgeUpdateDto request) {
                final String jwt =
                    createBureauJwt(COURT_USER, "415", UserType.COURT, Set.of(Role.MANAGER), "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, PUT,
                        URI.create(toUrl(judgeCode))),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.ACCEPTED);
                assertThat(response.getBody()).isNull();

                Judge judge =
                    judgeRepository.findById(judgeCode).orElseThrow(() -> new AssertionError("Judge not found"));
                assertThat(judge.getCode()).isEqualTo(request.getJudgeCode());
                assertThat(judge.getName()).isEqualTo(request.getJudgeName());
                assertThat(judge.isActive()).isEqualTo(request.getIsActive());
            }

            @Test
            void typicalUpdateJudgeIsActiveTrue() {
                assertValid(999_992,
                    JudgeUpdateDto.builder()
                        .isActive(true)
                        .judgeCode("CD1")
                        .judgeName("New Name")
                        .build());
            }

            @Test
            void typicalUpdateJudgeIsActiveFalse() {
                assertValid(999_991,
                    JudgeUpdateDto.builder()
                        .isActive(false)
                        .judgeCode("CD3")
                        .judgeName("New Name 3")
                        .build());
            }

        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(String id, JudgeUpdateDto request, String owner) {
                return triggerInvalid(id, request, owner, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String id, JudgeUpdateDto request, String owner,
                                                          Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, UserType.COURT, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, PUT,
                        URI.create(toUrl(id))),
                    String.class);
            }

            private JudgeUpdateDto getValidPayload() {
                return JudgeUpdateDto.builder()
                    .isActive(true)
                    .judgeCode("CD1")
                    .judgeName("New Name")
                    .build();
            }

            @Test
            void invalidJudgeId() throws JsonProcessingException {
                assertInvalidPathParam(triggerInvalid("INVALID", getValidPayload(), "415"),
                    "INVALID is the incorrect data type or is not in the expected format (judge_id)");
            }

            @Test
            void unauthorisedNotManger() {
                assertForbiddenResponse(triggerInvalid("999991", getValidPayload(), "415", Set.of(Role.COURT_OFFICER)),
                    toUrl("999991"));
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid("999991", getValidPayload(), "400"),
                    toUrl("999991"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                //Judge owner is 415 logged in user is 416 should not be able to view
                assertMojForbiddenResponse(triggerInvalid("999991", getValidPayload(), "416"),
                    toUrl("999991"), "User does not have access");
            }

            @Test
            void judgeNotFound() {
                assertNotFound(triggerInvalid("1999991", getValidPayload(), "416"),
                    toUrl("1999991"), "Judge not found");
            }

            @Test
            void codeAlreadyInUse() {
                JudgeUpdateDto payload = getValidPayload();
                payload.setJudgeCode("TST2");
                assertBusinessRuleViolation(triggerInvalid("999991", payload, "415"),
                    "Judge with this code already exists",
                    MojException.BusinessRuleViolation.ErrorCode.CODE_ALREADY_IN_USE);
            }

            @Test
            void invalidPayload() {
                JudgeUpdateDto payload = getValidPayload();
                payload.setIsActive(null);
                assertInvalidPayload(triggerInvalid("1999991", payload, "416"),
                    new RestResponseEntityExceptionHandler.FieldError("isActive", "must not be null"));
            }
        }
    }

}
