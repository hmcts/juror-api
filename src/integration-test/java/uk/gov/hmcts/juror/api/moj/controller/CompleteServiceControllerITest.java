package uk.gov.hmcts.juror.api.moj.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.request.CompleteServiceJurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAndPoolRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteServiceValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorStatusValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: /api/v1/moj/complete-service")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.ExcessiveImports")
class CompleteServiceControllerITest extends AbstractIntegrationTest {

    private final TestRestTemplate template;
    private final JurorPoolRepository jurorPoolRepository;
    private final JurorHistoryRepository jurorHistoryRepository;

    private HttpHeaders httpHeaders;

    private static final String BASE_URL = "/api/v1/moj/complete-service";

    @BeforeEach
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }


    private void validateJurorWasCompleted(LocalDate completionTime, String jurorNumber, String poolNumber,
                                           boolean isDismissal) {
        executeInTransaction(() -> {
            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);
            assertEquals(true, jurorPool.getIsActive(),
                "Juror pool should be active");
            assertFalse(jurorPool.isOnCall(), "Juror pool should not be on call");
            assertEquals(IJurorStatus.COMPLETED, jurorPool.getStatus().getStatus(),
                "Juror pool status should be completed");
            Juror juror = jurorPool.getJuror();

            assertEquals(completionTime, juror.getCompletionDate(),
                "Juror completion date should match");

            if (isDismissal) {
                assertThat(jurorPool.getNextDate()).isNull();
                assertThat(jurorPool.isOnCall()).isFalse();
            }
            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumberOrderById(jurorNumber);
            assertEquals(1, jurorHistories.size(), "Should only be one history entry");
            JurorHistory jurorHistory = jurorHistories.get(0);
            assertEquals(poolNumber, jurorHistory.getPoolNumber(), "Pool number should match");
            assertEquals(jurorNumber, jurorHistory.getJurorNumber(), "Juror number should match");
            assertEquals("COURT_USER", jurorHistory.getCreatedBy(), "User id should match");
            assertEquals(HistoryCodeMod.COMPLETE_SERVICE, jurorHistory.getHistoryCode(), "History code should match");
            assertEquals(completionTime, jurorHistory.getOtherInformationDate(), "Date should match");
        });
    }

    @Nested
    @DisplayName("POST /api/v1/moj/complete-service/{poolNumber}/complete")
    @Sql({"/db/mod/truncate.sql", "/db/CompleteServiceControllerITest_typical.sql"})
    class CompleteService {
        @Test
        void positiveCompleteTypicalSingle() throws Exception {
            LocalDate completionTime = LocalDate.of(2023, 11, 23);
            final String bureauJwt = createJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/complete");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                CompleteServiceJurorNumberListDto.builder()
                    .completionDate(completionTime)
                    .jurorNumbers(List.of("641500005"))
                    .build(), httpHeaders,
                HttpMethod.PATCH, uri);
            ResponseEntity<Void> response =
                template.exchange(request, Void.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            validateJurorWasCompleted(completionTime, "641500005", "415220901", false);
        }

        @Test
        void positiveCompleteTypicalMultiple() throws Exception {
            LocalDate completionTime = LocalDate.of(2023, 11, 23);
            final String bureauJwt = createJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/complete");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                CompleteServiceJurorNumberListDto.builder()
                    .completionDate(completionTime)
                    .jurorNumbers(List.of("641500005", "641500004"))
                    .build(), httpHeaders,
                HttpMethod.PATCH, uri);
            ResponseEntity<Void> response =
                template.exchange(request, Void.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            validateJurorWasCompleted(completionTime, "641500005", "415220901", false);
            validateJurorWasCompleted(completionTime, "641500004", "415220901", false);
        }


        @Test
        void negativeOneJurorNotFound() throws Exception {
            LocalDate completionTime = LocalDate.of(2023, 11, 23);
            final String bureauJwt = createJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/complete");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<CompleteServiceJurorNumberListDto> request = new RequestEntity<>(
                CompleteServiceJurorNumberListDto.builder()
                    .completionDate(completionTime)
                    .jurorNumbers(List.of("641500005", "941500004"))
                    .build(), httpHeaders,
                HttpMethod.PATCH, uri);
            ResponseEntity<String> response =
                template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            JSONAssert
                .assertEquals("Json Should match",
                    "{\"status\":404,"
                        + "\"error\":\"Not Found\","
                        + "\"exception\":\"uk.gov.hmcts.juror.api.moj.exception.MojException$NotFound\","
                        + "\"message\":\"Juror number 941500004 not found in pool 415220901\","
                        + "\"path\":\"/api/v1/moj/complete-service/415220901/complete\"}",
                    response.getBody(), false);
            executeInTransaction(() -> {
                JurorPool jurorPool =
                    jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber("641500005", "415220901");
                assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                    "Juror pool status should not be updated");
                Juror juror = jurorPool.getJuror();
                assertNull(juror.getCompletionDate(),
                    "Completion date should not be update to provided completion date");
            });
        }

        @Test
        void negativeOneJurorNotResponded() throws Exception {
            LocalDate completionTime = LocalDate.of(2023, 11, 23);
            final String bureauJwt = createJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/complete");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<CompleteServiceJurorNumberListDto> request = new RequestEntity<>(
                CompleteServiceJurorNumberListDto.builder()
                    .completionDate(completionTime)
                    .jurorNumbers(List.of("641500005", "641500003"))
                    .build(), httpHeaders,
                HttpMethod.PATCH, uri);
            ResponseEntity<String> response =
                template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

            JSONAssert.assertEquals("Json Should match",
                "{"
                    + "\"message\":\"Unable to complete the service for the following juror number(s) due to invalid "
                    + "state: 641500003\","
                    + "\"code\":\"COMPLETE_SERVICE_JUROR_IN_INVALID_STATE\"}", response.getBody(), false);
            executeInTransaction(() -> {
                JurorPool jurorPool1 = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber("641500005",
                    "415220901");
                assertEquals(IJurorStatus.COMPLETED, jurorPool1.getStatus().getStatus(),
                    "Juror pool status should not change as transaction should rollback");
                Juror juror1 = jurorPool1.getJuror();
                assertNotNull(juror1.getCompletionDate(),
                    "Completion date should not be null.");
                assertEquals(completionTime, juror1.getCompletionDate(),
                    "Completion date should be equal to 2023-11-23");

                JurorPool jurorPool2 = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber("641500003",
                    "415220901");
                assertEquals(IJurorStatus.FAILED_TO_ATTEND, jurorPool2.getStatus().getStatus(),
                    "Juror pool status should not change as juror is in invalid state for completion");
                Juror juror2 = jurorPool2.getJuror();
                assertNull(juror2.getCompletionDate(),
                    "Completion date should not change as transaction should rollback");
            });
        }

        @Test
        void negativeUnauthorisedBureauUser() throws Exception {
            LocalDate completionTime = LocalDate.of(2023, 11, 23);
            final String owner = "400";
            final String bureauJwt = createJwt("COURT_USER", owner);
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/complete");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                CompleteServiceJurorNumberListDto.builder()
                    .completionDate(completionTime)
                    .jurorNumbers(List.of("641500005"))
                    .build(), httpHeaders,
                HttpMethod.PATCH, uri);
            ResponseEntity<String> response =
                template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            JSONAssert
                .assertEquals("Json Should match",
                    "{\"status\":403,"
                        + "\"error\":\"Forbidden\","
                        + "\"exception\":\"org.springframework.security.authorization.AuthorizationDeniedException\","
                        + "\"message\":\"Forbidden\","
                        + "\"path\":\"/api/v1/moj/complete-service/415220901/complete\"}",
                    response.getBody(), false);
        }

    }

    @Nested
    @DisplayName("POST /api/v1/moj/complete-service/{poolNumber}/validate")
    @Sql({"/db/mod/truncate.sql",
        "/db/CompleteServiceControllerITest_typical.sql"})
    class ValidateCompleteService {


        @Test
        void positiveSingleTypicalValid() throws Exception {
            final String owner = "415";
            final String bureauJwt = createJwt("COURT_USER", owner);
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/validate");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                new JurorNumberListDto(List.of("641500005")), httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<CompleteServiceValidationResponseDto> response =
                template.exchange(request, CompleteServiceValidationResponseDto.class);
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            CompleteServiceValidationResponseDto expected = CompleteServiceValidationResponseDto.builder()
                .valid(List.of(JurorStatusValidationResponseDto.builder()
                    .status(2)
                    .jurorNumber("641500005")
                    .firstName("FNAMEZEROFIVE")
                    .lastName("LNAMEZEROFIVE")
                    .build()))
                .invalidNotResponded(List.of())
                .build();
            validateCompleteServiceValidationResponseDto(expected, response.getBody());
        }

        @Test
        void positiveSingleTypicalInValid() throws Exception {
            final String owner = "415";
            final String bureauJwt = createJwt("COURT_USER", owner);
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/validate");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                new JurorNumberListDto(List.of("641500003")), httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<CompleteServiceValidationResponseDto> response =
                template.exchange(request, CompleteServiceValidationResponseDto.class);
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            CompleteServiceValidationResponseDto expected = CompleteServiceValidationResponseDto.builder()
                .valid(List.of())
                .invalidNotResponded(List.of(JurorStatusValidationResponseDto.builder()
                    .status(IJurorStatus.FAILED_TO_ATTEND)
                    .jurorNumber("641500003")
                    .firstName("FNAMEZEROTHREE")
                    .lastName("LNAMEZEROTHREE")
                    .build()))
                .build();
            validateCompleteServiceValidationResponseDto(expected, response.getBody());
        }

        @Test
        void positiveSingleTypicalMultiple() throws Exception {
            final String owner = "415";
            final String bureauJwt = createJwt("COURT_USER", owner);
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/validate");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                new JurorNumberListDto(List.of("641500005", "641500004", "641500003", "641500002")), httpHeaders,
                HttpMethod.POST, uri);
            ResponseEntity<CompleteServiceValidationResponseDto> response =
                template.exchange(request, CompleteServiceValidationResponseDto.class);
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


            CompleteServiceValidationResponseDto expected = CompleteServiceValidationResponseDto.builder()
                .valid(List.of(JurorStatusValidationResponseDto.builder()
                    .status(IJurorStatus.RESPONDED)
                    .jurorNumber("641500005")
                    .firstName("FNAMEZEROFIVE")
                    .lastName("LNAMEZEROFIVE")
                    .build(), JurorStatusValidationResponseDto.builder()
                    .status(IJurorStatus.RESPONDED)
                    .jurorNumber("641500004")
                    .firstName("FNAMEZEROFOUR")
                    .lastName("LNAMEZEROFOUR")
                    .build()))
                .invalidNotResponded(List.of(JurorStatusValidationResponseDto.builder()
                    .status(IJurorStatus.FAILED_TO_ATTEND)
                    .jurorNumber("641500003")
                    .firstName("FNAMEZEROTHREE")
                    .lastName("LNAMEZEROTHREE")
                    .build(), JurorStatusValidationResponseDto.builder()
                    .status(IJurorStatus.TRANSFERRED)
                    .jurorNumber("641500002")
                    .firstName("FNAMEZEROTWO")
                    .lastName("LNAMEZEROTWO")
                    .build()))
                .build();
            validateCompleteServiceValidationResponseDto(expected, response.getBody());
        }

        @Test
        void negativeNotFound() throws Exception {
            final String owner = "415";
            final String bureauJwt = createJwt("COURT_USER", owner);
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/validate");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                new JurorNumberListDto(List.of("123456789")), httpHeaders,
                HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            JSONAssert
                .assertEquals("Json Should match",
                    "{\"status\":404,"
                        + "\"error\":\"Not Found\","
                        + "\"exception\":\"uk.gov.hmcts.juror.api.moj.exception.MojException$NotFound\","
                        + "\"message\":\"Juror number 123456789 not found in pool 415220901\","
                        + "\"path\":\"/api/v1/moj/complete-service/415220901/validate\"}",
                    response.getBody(), false);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            final String owner = "415";
            final String bureauJwt = createJwt("COURT_USER", owner);
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/validate");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                new JurorNumberListDto(List.of("ABC")), httpHeaders,
                HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            assertInvalidPayload(response,
                new RestResponseEntityExceptionHandler.FieldError("jurorNumbers[0]", "must match \"^\\d{9}$\""));
        }

        @Test
        void negativeUnauthorisedBureauUser() throws Exception {
            final String owner = "400";
            final String bureauJwt = createJwt("COURT_USER", owner);
            final URI uri = URI.create("/api/v1/moj/complete-service/415220901/validate");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                new JurorNumberListDto(List.of("123456789")), httpHeaders,
                HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            JSONAssert
                .assertEquals("Json Should match",
                    "{\"status\":403,"
                        + "\"error\":\"Forbidden\","
                        + "\"exception\":\"org.springframework.security.authorization.AuthorizationDeniedException\","
                        + "\"message\":\"Forbidden\","
                        + "\"path\":\"/api/v1/moj/complete-service/415220901/validate\"}",
                    response.getBody(), false);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/complete-service/dismissal")
    @Sql({"/db/mod/truncate.sql", "/db/CompleteServiceController_InitPoolsAtCourt.sql"})
    class CompleteDismissalService {
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Test
        void positiveCompleteDismissalTypicalSingle() throws Exception {
            LocalDate completionTime = LocalDate.now();
            final String owner = "417";
            final String bureauJwt = createJwt("COURT_USER", owner);
            final URI uri = URI.create("/api/v1/moj/complete-service/dismissal");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                CompleteServiceJurorNumberListDto.builder()
                    .completionDate(completionTime)
                    .jurorNumbers(List.of("641700006"))
                    .build(), httpHeaders,
                HttpMethod.PATCH, uri);
            ResponseEntity<Void> response =
                template.exchange(request, Void.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            validateJurorWasCompleted(completionTime, "641700006", "417230101", true);
        }

        @Test
        void positiveCompleteTypicalMultiple() throws Exception {
            LocalDate completionTime = LocalDate.now();
            final String owner = "417";
            final String bureauJwt = createJwt("COURT_USER", owner);
            final URI uri = URI.create("/api/v1/moj/complete-service/dismissal");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                CompleteServiceJurorNumberListDto.builder()
                    .completionDate(completionTime)
                    .jurorNumbers(List.of("641700003", "641700006"))
                    .build(), httpHeaders,
                HttpMethod.PATCH, uri);
            ResponseEntity<Void> response =
                template.exchange(request, Void.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            validateJurorWasCompleted(completionTime, "641700003", "417230101", false);
            validateJurorWasCompleted(completionTime, "641700006", "417230101", false);
        }


        @Test
        void negativeOneJurorNotFound() throws Exception {
            LocalDate completionTime = LocalDate.of(2023, 11, 23);
            final String owner = "417";
            final String bureauJwt = createJwt("COURT_USER", owner);
            final URI uri = URI.create("/api/v1/moj/complete-service/dismissal");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<CompleteServiceJurorNumberListDto> request = new RequestEntity<>(
                CompleteServiceJurorNumberListDto.builder()
                    .completionDate(completionTime)
                    .jurorNumbers(List.of("641700003", "941700009"))  // Juror number 941700009 does not exist
                    .build(), httpHeaders,
                HttpMethod.PATCH, uri);
            ResponseEntity<String> response =
                template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            JSONAssert
                .assertEquals("Json Should match",
                    "{\"status\":404,"
                        + "\"error\":\"Not Found\","
                        + "\"exception\":\"uk.gov.hmcts.juror.api.moj.exception.MojException$NotFound\","
                        + "\"message\":\"Juror number 941700009 not found in database\","
                        + "\"path\":\"/api/v1/moj/complete-service/dismissal\"}",
                    response.getBody(), false);
            executeInTransaction(() -> {
                JurorPool jurorPool =
                    jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber("641700003", "417230101");
                assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                    "Juror pool status should not change as transaction should rollback");

                Juror juror = jurorPool.getJuror();
                assertNull(juror.getCompletionDate(),
                    "Completion date should not change as transaction should rollback");
            });
        }

        @Test
        void negativeUnauthorisedBureauUser() throws Exception {
            LocalDate completionTime = LocalDate.of(2023, 11, 23);
            final String bureauJwt = createJwtBureau("BUREAU_USER");
            final URI uri = URI.create("/api/v1/moj/complete-service/dismissal");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<JurorNumberListDto> request = new RequestEntity<>(
                CompleteServiceJurorNumberListDto.builder()
                    .completionDate(completionTime)
                    .jurorNumbers(List.of("641700003"))
                    .build(), httpHeaders,
                HttpMethod.PATCH, uri);
            ResponseEntity<String> response =
                template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            JSONAssert
                .assertEquals("Json Should match",
                    "{\"status\":403,"
                        + "\"error\":\"Forbidden\","
                        + "\"exception\":\"org.springframework.security.authorization.AuthorizationDeniedException\","
                        + "\"message\":\"Forbidden\","
                        + "\"path\":\"/api/v1/moj/complete-service/dismissal\"}",
                    response.getBody(), false);
        }

    }

    private void validateCompleteServiceValidationResponseDto(CompleteServiceValidationResponseDto expected,
                                                              CompleteServiceValidationResponseDto actual) {
        assertNotNull(actual, "Response should not be null");
        validateJurorStatusValidationResponseDto(expected.getValid(), actual.getValid());
        validateJurorStatusValidationResponseDto(expected.getInvalidNotResponded(),
            actual.getInvalidNotResponded());
    }

    private void validateJurorStatusValidationResponseDto(List<JurorStatusValidationResponseDto> expected,
                                                          List<JurorStatusValidationResponseDto> actual) {
        assertEquals(expected.size(), actual.size(), "Size should match");
        for (int i = 0; i < expected.size(); i++) {
            JurorStatusValidationResponseDto expectedJurorStatusValidationResponseDto = expected.get(i);
            JurorStatusValidationResponseDto actualJurorStatusValidationResponseDto = actual.get(i);
            validateJurorStatusValidationResponseDto(expectedJurorStatusValidationResponseDto,
                actualJurorStatusValidationResponseDto);
        }
    }

    private void validateJurorStatusValidationResponseDto(JurorStatusValidationResponseDto expected,
                                                          JurorStatusValidationResponseDto actual) {
        assertEquals(expected.getStatus(),
            actual.getStatus(), "Status should match");
        assertEquals(expected.getJurorNumber(),
            actual.getJurorNumber(), "Juror number should match");
        assertEquals(expected.getFirstName(),
            actual.getFirstName(), "First name should match");
        assertEquals(expected.getLastName(),
            actual.getLastName(), "Last name should match");
    }

    @Nested
    @DisplayName("PATCH " + UncompleteService.URL)
    @Sql({"/db/mod/truncate.sql", "/db/UncompleteServiceController.sql"})
    class UncompleteService {

        public static final String URL = BASE_URL + "/uncomplete";

        private void validateJurorWasUncompleted(String jurorNumber, String poolNumber) {
            executeInTransaction(() -> {
                JurorPool jurorPool =
                    jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);
                assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                    "Juror pool status should be responded");
                Juror juror = jurorPool.getJuror();

                assertNull(juror.getCompletionDate(),
                    "Juror completion date should be null");

                List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumberOrderById(jurorNumber);
                assertEquals(1, jurorHistories.size(), "Should only be one history entry");
                JurorHistory jurorHistory = jurorHistories.get(0);
                assertEquals(poolNumber, jurorHistory.getPoolNumber(), "Pool number should match");
                assertEquals(jurorNumber, jurorHistory.getJurorNumber(), "Juror number should match");
                assertEquals("COURT_USER", jurorHistory.getCreatedBy(), "User id should match");
                assertEquals(HistoryCodeMod.COMPLETE_SERVICE, jurorHistory.getHistoryCode(),
                    "History code should match");
                assertEquals("Completion date removed", jurorHistory.getOtherInformation(), "Info should match");
            });
        }

        @Test
        void positiveTypicalSingle() throws Exception {
            String jurorNumber = "641500005";
            String poolNumber = "415220901";
            final String bureauJwt = createJwt("COURT_USER", Set.of(Role.SENIOR_JUROR_OFFICER), "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<List<JurorAndPoolRequest>> request = new RequestEntity<>(
                List.of(
                    JurorAndPoolRequest.builder()
                        .jurorNumber(jurorNumber)
                        .poolNumber(poolNumber)
                        .build()
                ), httpHeaders,
                HttpMethod.PATCH, URI.create(URL));


            ResponseEntity<Void> response =
                template.exchange(request, Void.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            validateJurorWasUncompleted(jurorNumber, poolNumber);
        }


        @Test
        void positiveTypicalMultiple() throws Exception {
            String jurorNumber1 = "641500005";
            String poolNumber1 = "415220901";
            String jurorNumber2 = "641500006";
            String poolNumber2 = "415220902";
            String jurorNumber3 = "641500007";
            String poolNumber3 = "415220902";
            final String bureauJwt = createJwt("COURT_USER", Set.of(Role.SENIOR_JUROR_OFFICER), "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<List<JurorAndPoolRequest>> request = new RequestEntity<>(
                List.of(
                    JurorAndPoolRequest.builder()
                        .jurorNumber(jurorNumber1)
                        .poolNumber(poolNumber1)
                        .build(),
                    JurorAndPoolRequest.builder()
                        .jurorNumber(jurorNumber2)
                        .poolNumber(poolNumber2)
                        .build(),
                    JurorAndPoolRequest.builder()
                        .jurorNumber(jurorNumber3)
                        .poolNumber(poolNumber3)
                        .build()
                ), httpHeaders,
                HttpMethod.PATCH, URI.create(URL));


            ResponseEntity<Void> response =
                template.exchange(request, Void.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            validateJurorWasUncompleted(jurorNumber1, poolNumber1);
            validateJurorWasUncompleted(jurorNumber2, poolNumber2);
            validateJurorWasUncompleted(jurorNumber3, poolNumber3);
        }

        @Test
        void negativeNotFound() throws Exception {
            String jurorNumber = "641500000";
            String poolNumber = "415220901";
            final String bureauJwt = createJwt("COURT_USER", Set.of(Role.SENIOR_JUROR_OFFICER), "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<List<JurorAndPoolRequest>> request = new RequestEntity<>(
                List.of(
                    JurorAndPoolRequest.builder()
                        .jurorNumber(jurorNumber)
                        .poolNumber(poolNumber)
                        .build()
                ), httpHeaders,
                HttpMethod.PATCH, URI.create(URL));

            assertNotFound(template.exchange(request, String.class), URL,
                "No complete juror pool found for Juror number 641500000");
        }

        @Test
        void negativeBadPayload() throws Exception {
            String jurorNumber = "641500000";
            String poolNumber = "INVALID";
            final String bureauJwt = createJwt("COURT_USER", Set.of(Role.SENIOR_JUROR_OFFICER), "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<List<JurorAndPoolRequest>> request = new RequestEntity<>(
                List.of(
                    JurorAndPoolRequest.builder()
                        .jurorNumber(jurorNumber)
                        .poolNumber(poolNumber)
                        .build()
                ), httpHeaders,
                HttpMethod.PATCH, URI.create(URL));

            assertInvalidPathParam(template.exchange(request, String.class),
                "uncompleteService.requestList[0].poolNumber: must match \"^\\d{9}$\"");
        }

        @Test
        void negativeUnauthorisedNotSjo() throws Exception {
            String jurorNumber = "641500005";
            String poolNumber = "415220901";
            final String bureauJwt = createJwt("COURT_USER", "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<List<JurorAndPoolRequest>> request = new RequestEntity<>(
                List.of(
                    JurorAndPoolRequest.builder()
                        .jurorNumber(jurorNumber)
                        .poolNumber(poolNumber)
                        .build()
                ), httpHeaders,
                HttpMethod.PATCH, URI.create(URL));

            assertForbiddenResponse(template.exchange(request, String.class), URL);
        }
    }
}
