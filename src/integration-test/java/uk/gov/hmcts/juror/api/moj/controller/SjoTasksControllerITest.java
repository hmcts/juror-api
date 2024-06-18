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
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAndPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.net.URI;
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

        final String courtJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.COURT)
            .login("COURT_USER")
            .owner(TestConstants.VALID_COURT_LOCATION)
            .locCode(TestConstants.VALID_COURT_LOCATION)
            .roles(List.of(Role.MANAGER, Role.SENIOR_JUROR_OFFICER))
            .staff(staff)
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("PATCH " + UndoUpdateJurorToFailedToAttend.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordControllerITest_failedToAttend_undo_typical.sql"})
    class UndoUpdateJurorToFailedToAttend {

        private static final String URL = BASE_URL + "/failed-to-attend/undo";

        private static final String JUROR_NUMBER = "641500005";
        private static final String POOL_NUMBER = "415220901";

        private JurorAndPoolRequest createDto(String jurorNumber, String poolNumber) {
            return JurorAndPoolRequest.builder()
                .jurorNumber(jurorNumber)
                .poolNumber(poolNumber)
                .build();
        }

        @Test
        void positiveTypical() {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorAndPoolRequest dto = createDto("123456789", POOL_NUMBER);
            ResponseEntity<Void> response =
                restTemplate.exchange(
                    new RequestEntity<>(List.of(dto), httpHeaders, HttpMethod.PATCH, URI.create(URL)), Void.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                "Juror pool status should be responded");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumberOrderById(JUROR_NUMBER);
            assertEquals(1, jurorHistories.size(), "Should only be one history entry");
            JurorHistory jurorHistory = jurorHistories.get(0);
            assertEquals(POOL_NUMBER, jurorHistory.getPoolNumber(), "Pool number should match");
            assertEquals(JUROR_NUMBER, jurorHistory.getJurorNumber(), "Juror number should match");
            assertEquals("COURT_USER", jurorHistory.getCreatedBy(), "User id should match");
            assertEquals(HistoryCodeMod.FAILED_TO_ATTEND, jurorHistory.getHistoryCode(), "History code should match");
            assertEquals("FTA status removed", jurorHistory.getOtherInformation(),
                "Info should match");
        }

        @Test
        void negativeNotFound() {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorAndPoolRequest dto = createDto("123456789", POOL_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(List.of(dto), httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertErrorResponse(response,
                HttpStatus.NOT_FOUND,
                URL,
                MojException.NotFound.class,
                "No Failed To Attend juror pool found for Juror number 123456789");
        }

        @Test
        void negativeNotFailedToRespond() {
            final String jurorNumber = "641500004";
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorAndPoolRequest dto = createDto(jurorNumber, POOL_NUMBER);

            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(List.of(dto), httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertBusinessRuleViolation(response,
                "Juror status must be failed to attend in order to undo the failed to attend status.",
                JUROR_STATUS_MUST_BE_FAILED_TO_ATTEND
            );

            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, POOL_NUMBER);
            assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumberOrderById(jurorNumber);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }


        @Test
        void negativeInvalidPayload() {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorAndPoolRequest dto = createDto("INVALID", POOL_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(List.of(dto), httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertInvalidPayload(response,
                new RestResponseEntityExceptionHandler.FieldError("jurorNumber", "must match \"^\\d{9}$\""));


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
            JurorAndPoolRequest dto = createDto(JUROR_NUMBER, POOL_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(List.of(dto), httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);

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
            JurorAndPoolRequest dto = createDto(JUROR_NUMBER, POOL_NUMBER);
            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(List.of(dto), httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);

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
