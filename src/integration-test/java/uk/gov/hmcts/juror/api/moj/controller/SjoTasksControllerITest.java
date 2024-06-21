package uk.gov.hmcts.juror.api.moj.controller;

import lombok.SneakyThrows;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_STATUS_MUST_BE_FAILED_TO_ATTEND;

/**
 * Integration tests for the Juror Record controller.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.ExcessiveImports")
public class SjoTasksControllerITest extends AbstractIntegrationTest {
    private static final String BASE_URL = "/api/v1/moj/sjo-tasks";

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private JurorPoolRepository jurorPoolRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;

    private HttpHeaders httpHeaders;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();
    }

    private void initHeaders() {
        BureauJwtPayload.Staff staff = new BureauJwtPayload.Staff();
        staff.setCourts(Collections.singletonList(TestConstants.VALID_COURT_LOCATION));

        final String courtJwt = getCourtJwt("COURT_USER", Set.of(Role.MANAGER, Role.SENIOR_JUROR_OFFICER));

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("POST " + GetCompleteJurors.URL)
    @Sql({"/db/mod/truncate.sql", "/db/CompleteServiceControllerSearch.sql"})
    @SuppressWarnings("PMD.TooManyMethods")
    class GetCompleteJurors {
        public static final String URL = BASE_URL + "/juror/search";

        ResponseEntity<PaginatedList<JurorDetailsDto>> triggerValid(JurorPoolSearch search) throws Exception {
            RequestEntity<JurorPoolSearch> request = new RequestEntity<>(search, httpHeaders,
                HttpMethod.POST, URI.create(URL));

            ResponseEntity<PaginatedList<JurorDetailsDto>> response =
                restTemplate.exchange(request, new ParameterizedTypeReference<>() {
            });

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            return response;
        }

        @Test
        void positiveJurorFirstNameSearch() throws Exception {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);

            ResponseEntity<PaginatedList<JurorDetailsDto>> response = triggerValid(
                JurorPoolSearch.builder()
                    .jurorName("FNAMEZERO")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()
            );

            PaginatedList<JurorDetailsDto> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTotalPages()).isEqualTo(1L);
            assertThat(body.getTotalItems()).isEqualTo(4L);
            assertThat(body.getCurrentPage()).isEqualTo(1L);

            List<JurorDetailsDto> data = body.getData();
            assertThat(data).isNotNull().hasSize(4);
            validateCompleteJurorResponse641500005(data.get(0));
            validateCompleteJurorResponse641500007(data.get(1));
            validateCompleteJurorResponse641500008(data.get(2));
            validateCompleteJurorResponse641500009(data.get(3));
        }

        @Test
        void positiveJurorLastNameSearch() throws Exception {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);

            ResponseEntity<PaginatedList<JurorDetailsDto>> response = triggerValid(
                JurorPoolSearch.builder()
                    .jurorName("LNAMEONE")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()
            );

            PaginatedList<JurorDetailsDto> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTotalPages()).isEqualTo(1L);
            assertThat(body.getTotalItems()).isEqualTo(5L);
            assertThat(body.getCurrentPage()).isEqualTo(1L);

            List<JurorDetailsDto> data = body.getData();
            assertThat(data).isNotNull().hasSize(5);
            validateCompleteJurorResponse641500010(data.get(0));
            validateCompleteJurorResponse641500011(data.get(1));
            validateCompleteJurorResponse641500012(data.get(2));
            validateCompleteJurorResponse641500013(data.get(3));
            validateCompleteJurorResponse641500014(data.get(4));
        }

        @Test
        void positiveJurorFirstAndLastNameSearch() throws Exception {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);

            ResponseEntity<PaginatedList<JurorDetailsDto>> response = triggerValid(
                JurorPoolSearch.builder()
                    .jurorName("FNAMEZEROSEVEN LNAMEZEROSE")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()
            );

            PaginatedList<JurorDetailsDto> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTotalPages()).isEqualTo(1L);
            assertThat(body.getTotalItems()).isEqualTo(1L);
            assertThat(body.getCurrentPage()).isEqualTo(1L);

            List<JurorDetailsDto> data = body.getData();
            assertThat(data).isNotNull().hasSize(1);
            validateCompleteJurorResponse641500007(data.get(0));
        }

        @Test
        void positiveJurorNumberSearch() throws Exception {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);

            ResponseEntity<PaginatedList<JurorDetailsDto>> response = triggerValid(
                JurorPoolSearch.builder()
                    .jurorNumber("64150000")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()
            );

            PaginatedList<JurorDetailsDto> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTotalPages()).isEqualTo(1L);
            assertThat(body.getTotalItems()).isEqualTo(4L);
            assertThat(body.getCurrentPage()).isEqualTo(1L);

            List<JurorDetailsDto> data = body.getData();
            assertThat(data).isNotNull().hasSize(4);
            validateCompleteJurorResponse641500005(data.get(0));
            validateCompleteJurorResponse641500007(data.get(1));
            validateCompleteJurorResponse641500008(data.get(2));
            validateCompleteJurorResponse641500009(data.get(3));
        }

        @Test
        void positivePostCodeSearch() throws Exception {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);

            ResponseEntity<PaginatedList<JurorDetailsDto>> response = triggerValid(
                JurorPoolSearch.builder()
                    .postcode("CH0 5AN")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()
            );

            PaginatedList<JurorDetailsDto> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTotalPages()).isEqualTo(1L);
            assertThat(body.getTotalItems()).isEqualTo(2L);
            assertThat(body.getCurrentPage()).isEqualTo(1L);

            List<JurorDetailsDto> data = body.getData();
            assertThat(data).isNotNull().hasSize(2);
            validateCompleteJurorResponse641500005(data.get(0));
            validateCompleteJurorResponse641500007(data.get(1));
        }

        @Test
        void positivePoolNumberSearch() throws Exception {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);

            ResponseEntity<PaginatedList<JurorDetailsDto>> response = triggerValid(
                JurorPoolSearch.builder()
                    .poolNumber("415220902")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()
            );

            PaginatedList<JurorDetailsDto> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTotalPages()).isEqualTo(1L);
            assertThat(body.getTotalItems()).isEqualTo(3L);
            assertThat(body.getCurrentPage()).isEqualTo(1L);

            List<JurorDetailsDto> data = body.getData();
            assertThat(data).isNotNull().hasSize(3);
            validateCompleteJurorResponse641500010(data.get(0));
            validateCompleteJurorResponse641500011(data.get(1));
            validateCompleteJurorResponse641500012(data.get(2));
        }

        @Test
        void positivePagination() throws Exception {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);

            ResponseEntity<PaginatedList<JurorDetailsDto>> response = triggerValid(
                JurorPoolSearch.builder()
                    .poolNumber("415")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(5)
                    .pageNumber(1)
                    .build()
            );

            PaginatedList<JurorDetailsDto> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTotalPages()).isEqualTo(2L);
            assertThat(body.getTotalItems()).isEqualTo(9L);
            assertThat(body.getCurrentPage()).isEqualTo(1L);

            List<JurorDetailsDto> data = body.getData();
            assertThat(data).isNotNull().hasSize(5);
            validateCompleteJurorResponse641500005(data.get(0));
            validateCompleteJurorResponse641500007(data.get(1));
            validateCompleteJurorResponse641500008(data.get(2));
            validateCompleteJurorResponse641500009(data.get(3));
            validateCompleteJurorResponse641500010(data.get(4));

            response = triggerValid(
                JurorPoolSearch.builder()
                    .poolNumber("415")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(5)
                    .pageNumber(2)
                    .build()
            );

            PaginatedList<JurorDetailsDto> body2 = response.getBody();
            assertThat(body2).isNotNull();
            assertThat(body2.getTotalPages()).isEqualTo(2L);
            assertThat(body2.getTotalItems()).isEqualTo(9L);
            assertThat(body2.getCurrentPage()).isEqualTo(2L);

            List<JurorDetailsDto> data2 = body2.getData();
            assertThat(data2).isNotNull().hasSize(4);
            validateCompleteJurorResponse641500011(data2.get(0));
            validateCompleteJurorResponse641500012(data2.get(1));
            validateCompleteJurorResponse641500013(data2.get(2));
            validateCompleteJurorResponse641500014(data2.get(3));
        }

        @Test
        void negativeNotFound() {
            RequestEntity<JurorPoolSearch> request = new RequestEntity<>(
                JurorPoolSearch.builder()
                    .poolNumber("321")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(5)
                    .pageNumber(2)
                    .build(), httpHeaders,
                HttpMethod.POST, URI.create(URL));

            assertNotFound(restTemplate.exchange(request, String.class), URL,
                "No juror pools found that meet your search criteria.");
        }

        @Test
        void negativeBadPayload() {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);

            RequestEntity<JurorPoolSearch> request = new RequestEntity<>(
                JurorPoolSearch.builder()
                    .jurorName("ABC")
                    .jurorNumber("12")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(5)
                    .pageNumber(2)
                    .build(), httpHeaders,
                HttpMethod.POST, URI.create(URL));

            assertInvalidPayload(restTemplate.exchange(request, String.class),
                new RestResponseEntityExceptionHandler.FieldError("jurorName",
                    "Field jurorName should be excluded if any of the following fields are present: "
                        + "[jurorNumber, postcode]"));
        }

        @Test
        void negativeUnauthorisedNotSjo() {
            setAuthorization("COURT_USER", "415", UserType.COURT);

            RequestEntity<JurorPoolSearch> request = new RequestEntity<>(
                JurorPoolSearch.builder()
                    .poolNumber("415")
                    .jurorStatus(IJurorStatus.COMPLETED)
                    .pageLimit(5)
                    .pageNumber(2)
                    .build(), httpHeaders,
                HttpMethod.POST, URI.create(URL));

            assertForbiddenResponse(restTemplate.exchange(request, String.class), URL);
        }

        private void validateCompleteJurorResponse641500005(JurorDetailsDto response) {
            validateCompleteJurorResponse(
                response,
                "641500005",
                "415220901",
                "FNAMEZEROFIVE",
                "LNAMEZEROFIVE",
                "CH0 5AN",
                LocalDate.of(2023, 1, 5)
            );
        }

        private void validateCompleteJurorResponse641500007(JurorDetailsDto response) {
            validateCompleteJurorResponse(
                response,
                "641500007",
                "415220901",
                "FNAMEZEROSEVEN",
                "LNAMEZEROSEVEN",
                "CH0 5AN",
                LocalDate.of(2023, 1, 7)
            );
        }

        private void validateCompleteJurorResponse641500008(JurorDetailsDto response) {
            validateCompleteJurorResponse(
                response,
                "641500008",
                "415220901",
                "FNAMEZEROEIGHT",
                "LNAMEZEROEIGHT",
                "CH0 8AN",
                LocalDate.of(2023, 1, 8)
            );
        }

        private void validateCompleteJurorResponse641500009(JurorDetailsDto response) {
            validateCompleteJurorResponse(
                response,
                "641500009",
                "415220901",
                "FNAMEZERONINE",
                "LNAMEZERONINE",
                "CH0 9AN",
                LocalDate.of(2023, 1, 9)
            );
        }

        private void validateCompleteJurorResponse641500010(JurorDetailsDto response) {
            validateCompleteJurorResponse(
                response,
                "641500010",
                "415220902",
                "FNAMEONEZERO",
                "LNAMEONEZERO",
                "CH1 0AN",
                LocalDate.of(2023, 1, 10)
            );
        }

        private void validateCompleteJurorResponse641500011(JurorDetailsDto response) {
            validateCompleteJurorResponse(
                response,
                "641500011",
                "415220902",
                "FNAMEONEONE",
                "LNAMEONEONE",
                "CH1 1AN",
                LocalDate.of(2023, 1, 11)
            );
        }

        private void validateCompleteJurorResponse641500012(JurorDetailsDto response) {
            validateCompleteJurorResponse(
                response,
                "641500012",
                "415220902",
                "FNAMEONETWO",
                "LNAMEONETWO",
                "CH1 2AN",
                LocalDate.of(2023, 1, 12)
            );
        }

        private void validateCompleteJurorResponse641500013(JurorDetailsDto response) {
            validateCompleteJurorResponse(
                response,
                "641500013",
                "415220903",
                "FNAMEONETHREE",
                "LNAMEONETHREE",
                "CH1 3AN",
                LocalDate.of(2023, 1, 13)
            );
        }

        private void validateCompleteJurorResponse641500014(JurorDetailsDto response) {
            validateCompleteJurorResponse(
                response,
                "641500014",
                "415220903",
                "FNAMEONEFOUR",
                "LNAMEONEFOUR",
                "CH1 4AN",
                LocalDate.of(2023, 1, 14)
            );
        }


        private void validateCompleteJurorResponse(JurorDetailsDto response,
            String jurorNumber,
            String poolNumber,
            String firstName,
            String lastName,
            String postCode,
            LocalDate completionDate) {

            assertThat(response).isNotNull();
            assertThat(response.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(response.getPoolNumber()).isEqualTo(poolNumber);
            assertThat(response.getFirstName()).isEqualTo(firstName);
            assertThat(response.getLastName()).isEqualTo(lastName);
            assertThat(response.getPostCode()).isEqualTo(postCode);
            assertThat(response.getCompletionDate()).isEqualTo(completionDate);
        }
    }

    @Nested
    @DisplayName("PATCH " + UndoUpdateJurorToFailedToAttend.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordControllerITest_failedToAttend_undo_typical.sql"})
    class UndoUpdateJurorToFailedToAttend {

        private static final String URL = BASE_URL + "/failed-to-attend/undo";

        private static final String JUROR_NUMBER = "641500005";
        private static final String POOL_NUMBER = "415220901";

        private JurorNumberListDto createDto(String... jurorNumber) {
            return JurorNumberListDto.builder()
                .jurorNumbers(List.of(jurorNumber))
                .build();
        }

        @Test
        void positiveTypical() {
            final String jurorNumber = "100000001";
            final String poolNumber = "415240101";

            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberListDto dto = createDto(jurorNumber);
            ResponseEntity<Void> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), Void.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(jurorNumber, true, "415");
            assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                "Juror pool status should be responded");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumberOrderById(jurorNumber);
            assertEquals(1, jurorHistories.size(), "Should only be one history entry");
            JurorHistory jurorHistory = jurorHistories.get(0);
            assertEquals(poolNumber, jurorHistory.getPoolNumber(), "Pool number should match");
            assertEquals(jurorNumber, jurorHistory.getJurorNumber(), "Juror number should match");
            assertEquals("COURT_USER", jurorHistory.getCreatedBy(), "User id should match");
            assertEquals(HistoryCodeMod.FAILED_TO_ATTEND, jurorHistory.getHistoryCode(), "History code should match");
            assertEquals("FTA status removed", jurorHistory.getOtherInformation(),
                "Info should match");
        }

        @Test
        void negativeNotFound() {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberListDto dto = createDto("123456789");
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertErrorResponse(response,
                HttpStatus.NOT_FOUND,
                URL,
                MojException.NotFound.class,
                "No Failed To Attend juror pool found for Juror number 123456789");
        }

        @Test
        void negativeNotFailedToAttend() {
            final String jurorNumber = "100000002";
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberListDto dto = createDto(jurorNumber);

            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertBusinessRuleViolation(response,
                "Juror status must be failed to attend in order to undo the failed to attend status.",
                JUROR_STATUS_MUST_BE_FAILED_TO_ATTEND
            );

            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(jurorNumber, true,
                "415");
            assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumberOrderById(jurorNumber);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }


        @Test
        void negativeInvalidPayload() {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberListDto dto = createDto("INVALID");
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertInvalidPayload(response,
                new RestResponseEntityExceptionHandler.FieldError("jurorNumbers[0]", "must match \"^\\d{9}$\""));


            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.FAILED_TO_ATTEND, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumberOrderById(JUROR_NUMBER);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }

        @Test
        void negativeUnauthorisedWrongLevel() {
            setAuthorization("COURT_USER", "415", UserType.COURT);
            JurorNumberListDto dto = createDto(JUROR_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);

            assertForbiddenResponse(response, URL);

            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.FAILED_TO_ATTEND, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumberOrderById(JUROR_NUMBER);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }

        @Test
        void negativeUnauthorisedBureau() {
            setAuthorization("BUREAU_USER", "400", UserType.BUREAU);
            JurorNumberListDto dto = createDto(JUROR_NUMBER);
            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);

            assertForbiddenResponse(response, URL);

            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.FAILED_TO_ATTEND, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumberOrderById(JUROR_NUMBER);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }
    }

    @SneakyThrows
    private void setAuthorization(String login, String owner, UserType userType, Role... roles) {
        httpHeaders.remove(HttpHeaders.AUTHORIZATION);
        httpHeaders.set(HttpHeaders.AUTHORIZATION,
            createJwt(login, owner, userType, Set.of(roles), owner)
        );
    }

}
