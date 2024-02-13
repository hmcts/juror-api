package uk.gov.hmcts.juror.api.moj.controller;

import com.querydsl.core.Tuple;
import org.json.JSONObject;
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
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.RetrieveAttendanceDetailsTag;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.UpdateAttendanceStatus;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;

import java.net.URI;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.APPEARANCE_CONFIRMED;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.CHECKED_IN;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.CHECKED_OUT;
import static uk.gov.hmcts.juror.api.utils.DataConversionUtil.getExceptionDetails;

/**
 * Integration tests for the Juror Management controller - attendance/appearance.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JurorManagementControllerITest extends AbstractIntegrationTest {

    private static final String JUROR1 = "111111111";
    private static final String JUROR2 = "222222222";
    private static final String JUROR3 = "333333333";
    private static final String JUROR5 = "555555555";
    private static final String JUROR6 = "666666666";
    private static final String JUROR7 = "777777777";
    private static final String JUROR8 = "888888888";
    private static final String JUROR9 = "999999999";

    private static final String URL_ATTENDANCE = "/api/v1/moj/juror-management/attendance";
    private static final String HTTP_STATUS_OK_MESSAGE = "Expect the HTTP status to be OK";
    private static final String HTTP_STATUS_BAD_REQUEST_MESSAGE = "Expect the HTTP status to be BAD_REQUEST";


    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AppearanceRepository appearanceRepository;

    @Autowired
    private JurorAppearanceService jurorAppearanceService;

    private HttpHeaders httpHeaders;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();
    }

    private void initHeaders() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("COURT_USER")
            .daysToExpire(89)
            .owner("415")
            .staff(BureauJWTPayload.Staff.builder().courts(Collections.singletonList("415")).build())
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("PUT processAppearance() - happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAppearanceTests.sql"})
    void createCheckInHappyPath() {
        JurorAppearanceDto requestDto = JurorAppearanceDto.builder()
            .jurorNumber(JUROR1)
            .locationCode("415")
            .attendanceDate(now())
            .checkInTime(LocalTime.of(9, 30))
            .appearanceStage(CHECKED_IN)
            .build();

        ResponseEntity<JurorAppearanceResponseDto.JurorAppearanceResponseData> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                    URI.create("/api/v1/moj/juror-management/appearance")),
                JurorAppearanceResponseDto.JurorAppearanceResponseData.class);

        validateAppearanceRecord(response);
    }

    @Test
    @DisplayName("GET getAppearanceRecords() - happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAppearanceTests.sql"})
    void testGetAppearanceHappyPath() {
        String localDate = now().minusDays(2).toString().formatted("YYYY-mm-dd");

        ResponseEntity<JurorAppearanceResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, GET,
                    URI.create("/api/v1/moj/juror-management/appearance?locationCode=415&attendanceDate=" + localDate)),
                JurorAppearanceResponseDto.class);

        validateAppearanceRecordMultiple(response);
    }

    @Test
    @DisplayName("GET getAppearanceRecords() - no records found for criteria")
    void testGetAppearanceUnhappyPath() {
        ResponseEntity<JurorAppearanceResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, GET,
                    URI.create("/api/v1/moj/juror-management/appearance?locationCode=415&attendanceDate=2023-10-08")),
                JurorAppearanceResponseDto.class);

        assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

        assertThat(response.getBody().getData().size()).as("Expect there to be no records returned")
            .isEqualTo(0);
    }

    @Nested
    @DisplayName("GET Retrieve attendance")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/RetrieveAttendanceDetails.sql"})
    class RetrieveAttendance {

        @Test
        @DisplayName("GET Retrieve attendance - for tag JUROR_NUMBER")
        void retrieveAttendanceJurorNumberTag() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR6);
            jurors.add(JUROR3);
            jurors.add(JUROR1);
            jurors.add(JUROR2);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(buildRetrieveAttendanceDetailsDto(jurors), httpHeaders, GET,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            List<AttendanceDetailsResponse.Details> data = response.getBody().getDetails();
            assertThat(data)
                .hasSize(4)
                .extracting(AttendanceDetailsResponse.Details::getJurorNumber)
                .containsExactlyInAnyOrder(JUROR6, JUROR2, JUROR1, JUROR3);

            assertThat(data)
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(
                    LocalTime.of(9, 30),
                    LocalTime.of(9, 30),
                    LocalTime.of(9, 30),
                    LocalTime.of(9, 30));

            assertThat(data)
                .extracting(AttendanceDetailsResponse.Details::getCheckOutTime)
                .containsExactlyInAnyOrder(
                    null, LocalTime.of(17, 30), null, LocalTime.of(17, 3));
        }

        @Test
        @DisplayName("GET Retrieve attendance details okay - for tag NOT_CHECKED_OUT")
        void retrieveAttendanceNotCheckedOutTag() {
            RetrieveAttendanceDetailsDto request = buildRetrieveAttendanceDetailsDto(null);
            request.getCommonData().setTag(RetrieveAttendanceDetailsTag.NOT_CHECKED_OUT);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, GET,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            List<AttendanceDetailsResponse.Details> data = response.getBody().getDetails();
            assertThat(data)
                .as("Expect 2 records to be returned")
                .hasSize(2)
                .extracting(AttendanceDetailsResponse.Details::getJurorNumber)
                .containsExactlyInAnyOrder(JUROR6, JUROR3);

            assertThat(data)
                .as("Expect check-in time to be 09:30")
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(
                    LocalTime.of(9, 30),
                    LocalTime.of(9, 30));

            assertThat(data)
                .as("Expect check-out time to be null")
                .extracting(AttendanceDetailsResponse.Details::getCheckOutTime)
                .containsExactlyInAnyOrder(null, null);
        }

        @Test
        @DisplayName("GET Retrieve attendance details okay - for tag CONFIRM_ATTENDANCE")
        void retrieveAttendanceConfirmAttendanceTag() {
            RetrieveAttendanceDetailsDto request = buildRetrieveAttendanceDetailsDto(null);
            request.getCommonData().setTag(RetrieveAttendanceDetailsTag.CONFIRM_ATTENDANCE);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, GET,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            List<AttendanceDetailsResponse.Details> data = response.getBody().getDetails();
            assertThat(data)
                .as("Expect 2 records to be returned")
                .hasSize(2)
                .extracting(AttendanceDetailsResponse.Details::getJurorNumber)
                .containsExactlyInAnyOrder(JUROR8, JUROR9);

            assertThat(data)
                .as("Expect check-in time to be null")
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(null, null);

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary)
                .as("Expect 2 jurors to be checked in")
                .extracting(AttendanceDetailsResponse.Summary::getCheckedIn)
                .isEqualTo(2L);

            assertThat(summary)
                .as("Expect 2 jurors to be absent")
                .extracting(AttendanceDetailsResponse.Summary::getAbsent)
                .isEqualTo(2L);
        }

        @Test
        @DisplayName("GET Retrieve attendance details - juror has not been checked in and does not exist in the "
            + "appearance table")
        void retrieveAttendanceNoJurors() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR9);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(buildRetrieveAttendanceDetailsDto(jurors), httpHeaders, GET,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            List<AttendanceDetailsResponse.Details> data = response.getBody().getDetails();
            assertThat(data).hasSize(0);
        }
    }

    @Nested
    @DisplayName("PATCH Update attendance")
    class UpdateAttendance {

        @Test
        @DisplayName("PATCH Update attendance - check out all jurors")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void updateAttendanceCheckOutAllJurors() {
            UpdateAttendanceDto request = buildUpdateAttendanceDto(null);
            request.getCommonData().setCheckOutTime(LocalTime.of(17, 51));

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            List<AttendanceDetailsResponse.Details> details = response.getBody().getDetails();
            assertThat(details)
                .hasSize(1)
                .extracting(AttendanceDetailsResponse.Details::getJurorNumber)
                .containsExactlyInAnyOrder(JUROR3);

            assertThat(details)
                .extracting(AttendanceDetailsResponse.Details::getJurorStatus)
                .containsExactlyInAnyOrder(IJurorStatus.PANEL);

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getCheckedOut)
                .isEqualTo(3L);

            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getPanelled)
                .isEqualTo(1L);

            // verify attendance details have been updated
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR2); // checked-out (updated)
            jurors.add(JUROR6); // checked-out (updated)
            jurors.add(JUROR7); // checked-out (updated)
            jurors.add(JUROR3); // panelled (no change)
            List<Tuple> tuples = appearanceRepository.retrieveAttendanceDetails(
                buildRetrieveAttendanceDetailsDto(jurors));
            AttendanceDetailsResponse attendanceResponse = buildAttendanceResponse(tuples);
            List<AttendanceDetailsResponse.Details> retrievedDetails = attendanceResponse.getDetails();

            // check-in time should not have been updated for this scenario
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(
                    LocalTime.of(9, 30),
                    LocalTime.of(9, 30),
                    LocalTime.of(9, 30),
                    LocalTime.of(15, 53));

            // check-out time should only have been updated JUROR2, JUROR6, JUROR7
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckOutTime)
                .containsExactlyInAnyOrder(
                    LocalTime.of(17, 51),
                    LocalTime.of(17, 51),
                    LocalTime.of(17, 51),
                    null);

            // app-stage should only have been updated for JUROR2, JUROR6, JUROR7
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getAppearanceStage)
                .containsExactlyInAnyOrder(CHECKED_OUT, CHECKED_OUT, CHECKED_OUT, CHECKED_IN);
        }

        @Test
        @DisplayName("PATCH Update attendance - check out single juror")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void updateAttendanceCheckOutSingleJuror() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR6);
            UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
            request.getCommonData().setSingleJuror(Boolean.TRUE);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getCheckedOut)
                .isEqualTo(1L);

            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getPanelled)
                .isEqualTo(0L);

            // verify attendance details have been updated
            List<Tuple> tuples = appearanceRepository.retrieveAttendanceDetails(
                buildRetrieveAttendanceDetailsDto(jurors));
            AttendanceDetailsResponse attendanceResponse = buildAttendanceResponse(tuples);
            List<AttendanceDetailsResponse.Details> retrievedDetails = attendanceResponse.getDetails();

            assertThat(retrievedDetails)
                .as("Status should be RESPONDED")
                .extracting(AttendanceDetailsResponse.Details::getJurorStatus)
                .containsExactlyInAnyOrder(IJurorStatus.RESPONDED);

            // check-in time should not have been updated for this scenario
            assertThat(retrievedDetails)
                .as("check-in time should not have been updated")
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(LocalTime.of(9, 30));

            // check-out time should have been updated for this scenario
            assertThat(retrievedDetails)
                .as("check-out time should have been updated")
                .extracting(AttendanceDetailsResponse.Details::getCheckOutTime)
                .containsExactlyInAnyOrder(LocalTime.of(17, 51));

            // app-stage time should have been updated for this scenario
            assertThat(retrievedDetails)
                .as("app-stage time should have been updated")
                .extracting(AttendanceDetailsResponse.Details::getAppearanceStage)
                .containsExactlyInAnyOrder(CHECKED_OUT);
        }

        @Test
        @DisplayName("PATCH Update attendance - checkout multiple jurors in list")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void updateAttendanceCheckOutMultipleJurorsInList() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR6);
            jurors.add(JUROR2);
            UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getCheckedOut)
                .isEqualTo(2L);

            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getPanelled)
                .isEqualTo(0L);

            // verify attendance details have been updated
            List<Tuple> tuples =
                appearanceRepository.retrieveAttendanceDetails(buildRetrieveAttendanceDetailsDto(jurors));
            AttendanceDetailsResponse attendanceResponse = buildAttendanceResponse(tuples);
            List<AttendanceDetailsResponse.Details> retrievedDetails = attendanceResponse.getDetails();

            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getJurorStatus)
                .containsExactlyInAnyOrder(IJurorStatus.RESPONDED, IJurorStatus.JUROR);

            // check-in time should not have been updated for this scenario
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(LocalTime.of(9, 30), LocalTime.of(9, 30));

            // check-out time should have been updated for this scenario
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckOutTime)
                .containsExactlyInAnyOrder(LocalTime.of(17, 51), LocalTime.of(17, 51));

            // app-stage time should have been updated for this scenario
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getAppearanceStage)
                .containsExactlyInAnyOrder(CHECKED_OUT, CHECKED_OUT);
        }

        @Test
        @DisplayName("PATCH Update attendance - Exception: checking out multiple jurors but SingleJuror flag is true")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void updateAttendanceCheckOutMultipleButSingleJurorFlagIsTrue() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR6);
            jurors.add(JUROR2);
            UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
            request.getCommonData().setSingleJuror(Boolean.TRUE);

            ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<>(request, httpHeaders,
                PATCH, URI.create(URL_ATTENDANCE)), String.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_BAD_REQUEST_MESSAGE).isEqualTo(BAD_REQUEST);

            JSONObject exceptionDetails = getExceptionDetails(response);
            assertThat(exceptionDetails.getString("error")).isEqualTo("Bad Request");
            assertThat(exceptionDetails.getString("message"))
                .isEqualTo("Multiple jurors not allowed for single record update");
        }

        @Test
        @DisplayName("PATCH Update attendance - - check out all panelled jurors")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void updateAttendanceCheckOutAllPanelledJurors() {
            UpdateAttendanceDto request = buildUpdateAttendanceDto(null);
            request.getCommonData().setStatus(UpdateAttendanceStatus.CHECK_OUT_PANELLED);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getCheckedOut)
                .isEqualTo(1L);

            // verify attendance details have been updated
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR3);
            List<Tuple> tuples =
                appearanceRepository.retrieveAttendanceDetails(buildRetrieveAttendanceDetailsDto(jurors));
            AttendanceDetailsResponse attendanceResponse = buildAttendanceResponse(tuples);
            List<AttendanceDetailsResponse.Details> retrievedDetails = attendanceResponse.getDetails();

            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getJurorStatus)
                .containsExactlyInAnyOrder(IJurorStatus.PANEL);

            // check-in time should not have been updated for this scenario
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(LocalTime.of(9, 30));

            // check-out time should have been updated
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckOutTime)
                .containsExactlyInAnyOrder(LocalTime.of(17, 51));

            // app-stage should have been updated
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getAppearanceStage)
                .containsExactlyInAnyOrder(CHECKED_OUT);
        }

        @Test
        @DisplayName("PATCH Update attendance - - check out all panelled jurors in list")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void updateAttendanceCheckOutAllPanelledJurorsInList() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR3);
            jurors.add(JUROR2);
            UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
            request.getCommonData().setStatus(UpdateAttendanceStatus.CHECK_OUT_PANELLED);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getCheckedOut)
                .isEqualTo(2L);

            // verify attendance details have been updated
            List<String> verifyJurors = new ArrayList<>();
            verifyJurors.add(JUROR2);
            verifyJurors.add(JUROR3);
            List<Tuple> tuples = appearanceRepository
                .retrieveAttendanceDetails(buildRetrieveAttendanceDetailsDto(verifyJurors));
            AttendanceDetailsResponse attendanceResponse = buildAttendanceResponse(tuples);
            List<AttendanceDetailsResponse.Details> retrievedDetails = attendanceResponse.getDetails();

            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getJurorStatus)
                .containsExactlyInAnyOrder(IJurorStatus.PANEL, IJurorStatus.JUROR);

            // check-in time should not have been updated for this scenario
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(LocalTime.of(9, 30), LocalTime.of(9, 30));

            // check-out time should have been updated
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckOutTime)
                .containsExactlyInAnyOrder(LocalTime.of(17, 51), LocalTime.of(17, 51));

            // app-stage should have been updated
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getAppearanceStage)
                .containsExactlyInAnyOrder(CHECKED_OUT, CHECKED_OUT);
        }

        @Test
        @DisplayName("PATCH Update attendance - check in all jurors updated ")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void updateAttendanceCheckIn() {
            UpdateAttendanceDto request = buildUpdateAttendanceDto(null);
            request.getCommonData().setStatus(UpdateAttendanceStatus.CHECK_IN);
            request.getCommonData().setCheckOutTime(null);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            List<AttendanceDetailsResponse.Details> details = response.getBody().getDetails();
            assertThat(details).isNull();

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getCheckedIn)
                .isEqualTo(6L);

            // verify attendance details have been updated
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR1);
            jurors.add(JUROR2);
            jurors.add(JUROR3);
            jurors.add(JUROR5);
            jurors.add(JUROR6);
            jurors.add(JUROR7);
            List<Tuple> tuples =
                appearanceRepository.retrieveAttendanceDetails(buildRetrieveAttendanceDetailsDto(jurors));
            AttendanceDetailsResponse attendanceResponse = buildAttendanceResponse(tuples);
            List<AttendanceDetailsResponse.Details> retrievedDetails = attendanceResponse.getDetails();

            // check-in time have been updated
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(
                    LocalTime.of(9, 50),
                    LocalTime.of(9, 50),
                    LocalTime.of(9, 50),
                    LocalTime.of(9, 50),
                    LocalTime.of(9, 50),
                    LocalTime.of(9, 50));

            // check-out time
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckOutTime)
                .containsExactlyInAnyOrder(null, null, null, null, null, LocalTime.of(12, 30));

            // app-stage should only have been updated for JUROR2, JUROR6, JUROR7
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getAppearanceStage)
                .containsExactlyInAnyOrder(CHECKED_IN, CHECKED_IN, CHECKED_IN, CHECKED_IN, CHECKED_IN, CHECKED_IN);
        }

        @Test
        @DisplayName("PATCH Update attendance - check in and out of juror updated")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void updateAttendanceCheckInAndOut() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR6);
            UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
            request.getCommonData().setStatus(UpdateAttendanceStatus.CHECK_IN_AND_OUT);
            request.getCommonData().setCheckInTime(LocalTime.of(9, 30));
            request.getCommonData().setCheckOutTime(LocalTime.of(17, 30));
            request.getCommonData().setSingleJuror(Boolean.TRUE);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            List<AttendanceDetailsResponse.Details> details = response.getBody().getDetails();
            assertThat(details).isNull();

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getCheckedInAndOut)
                .isEqualTo(1L);

            // verify attendance details have been updated
            List<Tuple> tuples =
                appearanceRepository.retrieveAttendanceDetails(buildRetrieveAttendanceDetailsDto(jurors));
            AttendanceDetailsResponse attendanceResponse = buildAttendanceResponse(tuples);
            List<AttendanceDetailsResponse.Details> retrievedDetails = attendanceResponse.getDetails();

            // check-in time have been updated
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(LocalTime.of(9, 30));

            // check-out time
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getCheckOutTime)
                .containsExactlyInAnyOrder(LocalTime.of(17, 30));

            // app-stage should only have been updated for JUROR2, JUROR6, JUROR7
            assertThat(retrievedDetails)
                .extracting(AttendanceDetailsResponse.Details::getAppearanceStage)
                .containsExactlyInAnyOrder(CHECKED_OUT);
        }

        @Test
        @DisplayName("PATCH Update attendance - confirm attendance")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void updateAttendanceNoShow() {
            UpdateAttendanceDto request = buildUpdateAttendanceDto(null);
            request.getCommonData().setCheckInTime(null);
            request.getCommonData().setCheckOutTime(null);
            request.getCommonData().setStatus(UpdateAttendanceStatus.CONFIRM_ATTENDANCE);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getCheckedIn)
                .isEqualTo(3L);

            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getAbsent)
                .isEqualTo(2L);

            // verify attendance details have been updated x 3 checked in
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR1); // checked-in
            jurors.add(JUROR6); // checked-in
            jurors.add(JUROR7); // checked-in

            List<Tuple> tuples =
                appearanceRepository.retrieveAttendanceDetails(buildRetrieveAttendanceDetailsDto(jurors));
            AttendanceDetailsResponse attendanceResponse = buildAttendanceResponse(tuples);
            List<AttendanceDetailsResponse.Details> details = attendanceResponse.getDetails();

            assertThat(details)
                .extracting(AttendanceDetailsResponse.Details::getAppearanceStage)
                .containsExactlyInAnyOrder(APPEARANCE_CONFIRMED, APPEARANCE_CONFIRMED, APPEARANCE_CONFIRMED);

            assertThat(details)
                .extracting(AttendanceDetailsResponse.Details::getIsNoShow)
                .containsExactlyInAnyOrder(null, null, null);

            jurors.clear();
            details.clear();
            tuples.clear();
            jurors.add(JUROR8); // absent
            jurors.add(JUROR9); // absent

            tuples = appearanceRepository.retrieveAttendanceDetails(buildRetrieveAttendanceDetailsDto(jurors));
            attendanceResponse = buildAttendanceResponse(tuples);
            details = attendanceResponse.getDetails();

            assertThat(details)
                .as("Expect 2 records to be returned with null appearance stage")
                .extracting(AttendanceDetailsResponse.Details::getAppearanceStage)
                .containsExactlyInAnyOrder(null, null);

            assertThat(details)
                .as("Expect 2 records to be returned with isNoShow = true")
                .extracting(AttendanceDetailsResponse.Details::getIsNoShow)
                .containsExactlyInAnyOrder(Boolean.TRUE, Boolean.TRUE);
        }

        @Test
        @DisplayName("PATCH Update attendance() - not updated because check-in after check-out")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void updateAttendanceCheckInIsAfterCheckOut() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR7);
            UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
            request.getCommonData().setCheckInTime(LocalTime.of(15, 53));
            request.getCommonData().setCheckOutTime(LocalTime.of(13, 53));
            request.getCommonData().setSingleJuror(Boolean.TRUE);

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE)), String.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_BAD_REQUEST_MESSAGE).isEqualTo(BAD_REQUEST);

            JSONObject exceptionDetails = getExceptionDetails(response);
            assertThat(exceptionDetails.getString("error")).isEqualTo("Bad Request");
            assertThat(exceptionDetails.getString("message"))
                .isEqualTo("Check-out time cannot be before check-in");

            // verify attendance details have NOT been updated
            List<Tuple> tuples =
                appearanceRepository.retrieveAttendanceDetails(buildRetrieveAttendanceDetailsDto(jurors));
            AttendanceDetailsResponse attendanceResponse = buildAttendanceResponse(tuples);
            List<AttendanceDetailsResponse.Details> details = attendanceResponse.getDetails();

            assertThat(details)
                .extracting(AttendanceDetailsResponse.Details::getAppearanceStage)
                .containsExactlyInAnyOrder(CHECKED_IN);

            assertThat(details)
                .extracting(AttendanceDetailsResponse.Details::getCheckInTime)
                .containsExactlyInAnyOrder(LocalTime.of(15, 53));

            assertThat(details)
                .extracting(AttendanceDetailsResponse.Details::getCheckOutTime)
                .containsExactlyInAnyOrder(LocalTime.of(12, 30));
        }

        private UpdateAttendanceDto buildUpdateAttendanceDto(List<String> jurors) {
            UpdateAttendanceDto.CommonData commonData = new UpdateAttendanceDto.CommonData();
            commonData.setStatus(UpdateAttendanceStatus.CHECK_OUT);
            commonData.setAttendanceDate(now().minusDays(2));
            commonData.setLocationCode("415");
            commonData.setCheckInTime(LocalTime.of(9, 50)); // CI time will be ignored if status = CO
            commonData.setCheckOutTime(LocalTime.of(17, 51));
            commonData.setSingleJuror(Boolean.FALSE);

            UpdateAttendanceDto request = new UpdateAttendanceDto();
            request.setCommonData(commonData);
            request.setJuror(jurors);

            return request;
        }

        private AttendanceDetailsResponse buildAttendanceResponse(List<Tuple> tuples) {
            List<AttendanceDetailsResponse.Details> attendanceDetails = new ArrayList<>();

            tuples.forEach(tuple -> {
                AttendanceDetailsResponse.Details details = AttendanceDetailsResponse.Details.builder()
                    .jurorNumber(tuple.get(0, String.class))
                    .firstName(tuple.get(1, String.class))
                    .lastName(tuple.get(2, String.class))
                    .jurorStatus(tuple.get(3, Integer.class))
                    .checkInTime(tuple.get(4, LocalTime.class))
                    .checkOutTime(tuple.get(5, LocalTime.class))
                    .isNoShow(tuple.get(6, Boolean.class))
                    .appearanceStage(tuple.get(7, AppearanceStage.class))
                    .build();
                attendanceDetails.add(details);
            });

            return AttendanceDetailsResponse.builder().details(attendanceDetails).build();
        }
    }

    @Nested
    @DisplayName("DELETE Delete attendance")
    class DeleteAttendance {

        @Test
        @DisplayName("DELETE delete attendance - delete attendance record okay")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void deleteAttendance() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR7);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(buildUpdateAttendanceDtoDelete(jurors), httpHeaders, DELETE,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary.getDeleted()).isEqualTo(1L);
            assertThat(summary.getAdditionalInformation()).isBlank();

            // verify attendance record no longer exists
            List<Tuple> tuples =
                appearanceRepository.retrieveAttendanceDetails(buildRetrieveAttendanceDetailsDto(jurors));
            assertThat(tuples).size().isEqualTo(0);
        }

        @Test
        @DisplayName("DELETE delete attendance - attendance record does not exist")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void deleteAttendanceRecordDoesNotExist() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR8);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(buildUpdateAttendanceDtoDelete(jurors), httpHeaders, DELETE,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            AttendanceDetailsResponse.Summary summary = response.getBody().getSummary();
            assertThat(summary.getDeleted()).isEqualTo(0L);
            assertThat(summary.getAdditionalInformation())
                .isEqualTo("No attendance record found for juror number 888888888");
        }

        @Test
        @DisplayName("DELETE delete attendance - Exception multiple jurors being deleted")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql"})
        void deleteAttendanceRecordMultipleJurors() {
            List<String> jurors = new ArrayList<>();
            jurors.add(JUROR8);
            jurors.add(JUROR2);

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(buildUpdateAttendanceDtoDelete(jurors), httpHeaders, DELETE,
                    URI.create(URL_ATTENDANCE)), String.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_BAD_REQUEST_MESSAGE).isEqualTo(BAD_REQUEST);

            JSONObject exceptionDetails = getExceptionDetails(response);
            assertThat(exceptionDetails.getString("error")).isEqualTo("Bad Request");
            assertThat(exceptionDetails.getString("message"))
                .isEqualTo("Cannot delete multiple juror attendance records");
        }

        private UpdateAttendanceDto buildUpdateAttendanceDtoDelete(List<String> jurors) {
            UpdateAttendanceDto.CommonData commonData = new UpdateAttendanceDto.CommonData();
            commonData.setStatus(UpdateAttendanceStatus.DELETE);
            commonData.setAttendanceDate(now().minusDays(2));
            commonData.setLocationCode("415");
            commonData.setSingleJuror(Boolean.TRUE);

            UpdateAttendanceDto request = new UpdateAttendanceDto();
            request.setCommonData(commonData);
            request.setJuror(jurors);

            return request;
        }

    }

    @Test
    @DisplayName("GET jurors to dismiss list - happy path")
    @Sql({"/db/mod/truncate.sql", "/db/JurorManagementController_poolsAtCourtLocation.sql"})
    public void getJurorsToDismissListHappy() {
        List<String> pools = new ArrayList<>();
        pools.add("415230101");

        final JurorsToDismissRequestDto request = JurorsToDismissRequestDto.builder()
            .poolNumbers(pools)
            .locationCode("415")
            .includeNotInAttendance(true)
            .includeOnCall(true)
            .numberOfJurorsToDismiss(3)
            .build();

        ResponseEntity<JurorsToDismissResponseDto> response =
            restTemplate.exchange(new RequestEntity<>(request, httpHeaders, GET,
                URI.create ("/api/v1/moj/juror-management/jurors-to-dismiss")), JurorsToDismissResponseDto.class);

        assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
        assertThat(response.getBody().getData()).isNotNull();

        List<JurorsToDismissResponseDto.JurorsToDismissData> jurorsToDismissData = response.getBody().getData();
        assertThat(jurorsToDismissData).as("Expect there to be 3 Juror record").hasSize(3);

    }

    @Test
    @DisplayName("GET jurors to dismiss list - Unhappy path, Bureau User not allowed")
    @Sql({"/db/mod/truncate.sql", "/db/JurorManagementController_poolsAtCourtLocation.sql"})
    public void getJurorsToDismissListUnhappyBureauUser() throws Exception {
        List<String> pools = new ArrayList<>();
        pools.add("415930101");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt("BUREAU_USER", "400"));
        final JurorsToDismissRequestDto request = JurorsToDismissRequestDto.builder()
            .poolNumbers(pools)
            .locationCode("415")
            .includeNotInAttendance(true)
            .includeOnCall(true)
            .numberOfJurorsToDismiss(3)
            .build();

        ResponseEntity<JurorsToDismissResponseDto> response =
            restTemplate.exchange(new RequestEntity<>(request, httpHeaders, GET,
                URI.create ("/api/v1/moj/juror-management/jurors-to-dismiss")), JurorsToDismissResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect HTTP Status of Forbidden").isEqualTo(FORBIDDEN);
        assertThat(response.getBody().getData()).isNull();

    }

    @Test
    @DisplayName("GET jurors to dismiss list - Unhappy path, pool not found")
    @Sql({"/db/mod/truncate.sql", "/db/JurorManagementController_poolsAtCourtLocation.sql"})
    public void getJurorsToDismissListUnhappyPoolNotFound() {
        List<String> pools = new ArrayList<>();
        pools.add("415930101");  // pool does not exist

        JurorsToDismissRequestDto request = JurorsToDismissRequestDto.builder()
            .poolNumbers(pools)
            .locationCode("415")
            .includeNotInAttendance(true)
            .includeOnCall(true)
            .numberOfJurorsToDismiss(3)
            .build();

        ResponseEntity<JurorsToDismissResponseDto> response =
            restTemplate.exchange(new RequestEntity<>(request, httpHeaders, GET,
                URI.create ("/api/v1/moj/juror-management/jurors-to-dismiss")), JurorsToDismissResponseDto.class);

        assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
        assertThat(response.getBody().getData()).isEmpty();

    }

    private RetrieveAttendanceDetailsDto buildRetrieveAttendanceDetailsDto(List<String> jurors) {
        RetrieveAttendanceDetailsDto.CommonData commonData = new RetrieveAttendanceDetailsDto.CommonData();
        commonData.setAttendanceDate(now().minusDays(2));
        commonData.setLocationCode("415");
        commonData.setTag(RetrieveAttendanceDetailsTag.JUROR_NUMBER);

        return RetrieveAttendanceDetailsDto.builder()
            .commonData(commonData)
            .juror(jurors)
            .build();
    }

    private static void validateAppearanceRecord(
        ResponseEntity<JurorAppearanceResponseDto.JurorAppearanceResponseData> response) {

        assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

        JurorAppearanceResponseDto.JurorAppearanceResponseData jurorAppearanceResponseData = response.getBody();
        assertThat(jurorAppearanceResponseData).isNotNull();

        assertThat(jurorAppearanceResponseData.getJurorNumber()).isEqualTo(JUROR1);
        assertThat(jurorAppearanceResponseData.getFirstName()).isEqualTo("TEST");
        assertThat(jurorAppearanceResponseData.getLastName()).isEqualTo("LASTNAME");
        assertThat(jurorAppearanceResponseData.getJurorStatus()).isEqualTo(IJurorStatus.RESPONDED);
        assertThat(jurorAppearanceResponseData.getCheckInTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(jurorAppearanceResponseData.getCheckOutTime()).isNull();
    }

    private void validateAppearanceRecordMultiple(ResponseEntity<JurorAppearanceResponseDto> response) {
        assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

        JurorAppearanceResponseDto jurorAppearanceResponseDto = response.getBody();
        assertThat(jurorAppearanceResponseDto.getData().size()).as("Expect 3 records to be returned")
            .isEqualTo(3);

        JurorAppearanceResponseDto.JurorAppearanceResponseData jurorAppearanceResponseData =
            jurorAppearanceResponseDto.getData().get(0);
        assertThat(jurorAppearanceResponseData.getJurorNumber()).isEqualTo(JUROR1);
        assertThat(jurorAppearanceResponseData.getFirstName()).isEqualTo("TEST");
        assertThat(jurorAppearanceResponseData.getLastName()).isEqualTo("LASTNAME");
        assertThat(jurorAppearanceResponseData.getJurorStatus()).isEqualTo(IJurorStatus.RESPONDED);
        assertThat(jurorAppearanceResponseData.getCheckInTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(jurorAppearanceResponseData.getCheckOutTime()).isNull();

        jurorAppearanceResponseData =
            jurorAppearanceResponseDto.getData().get(1);
        assertThat(jurorAppearanceResponseData.getJurorNumber()).isEqualTo(JUROR2);
        assertThat(jurorAppearanceResponseData.getJurorStatus()).isEqualTo(IJurorStatus.JUROR);

        jurorAppearanceResponseData =
            jurorAppearanceResponseDto.getData().get(2);
        assertThat(jurorAppearanceResponseData.getJurorNumber()).isEqualTo(JUROR3);
        assertThat(jurorAppearanceResponseData.getJurorStatus()).isEqualTo(IJurorStatus.PANEL);
    }
}
