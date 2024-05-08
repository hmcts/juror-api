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
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.AddAttendanceDayDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.JurorNonAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDateDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsOnTrialResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusGroup;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.RetrieveAttendanceDetailsTag;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.UpdateAttendanceStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.CHECKED_IN;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.CHECKED_OUT;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.EXPENSE_ENTERED;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.ATTENDANCE_RECORD_ALREADY_EXISTS;
import static uk.gov.hmcts.juror.api.utils.DataConversionUtil.getExceptionDetails;

/**
 * Integration tests for the Juror Management controller - attendance/appearance.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
class JurorManagementControllerITest extends AbstractIntegrationTest {

    private static final String JUROR1 = "111111111";
    private static final String JUROR2 = "222222222";
    private static final String JUROR3 = "333333333";
    private static final String JUROR4 = "444444444";
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
    private JurorPoolRepository jurorPoolRepository;

    @Autowired
    private JurorAppearanceService jurorAppearanceService;

    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;

    private HttpHeaders httpHeaders;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    private void initHeaders() {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .login("COURT_USER")
            .userType(UserType.COURT)
            .owner("415")
            .staff(BureauJwtPayload.Staff.builder().courts(Collections.singletonList("415")).build())
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
    @DisplayName("POST addAttendanceDay() - happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAddAttendanceDay.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    void addAttendanceDayHappyPath() {
        AddAttendanceDayDto requestDto = AddAttendanceDayDto.builder()
            .jurorNumber(JUROR1)
            .poolNumber("415230101")
            .locationCode("415")
            .attendanceDate(now())
            .checkInTime(LocalTime.of(9, 30))
            .checkOutTime(LocalTime.of(17, 30))
            .build();

        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-management/add-attendance-day")), String.class);

        assertThat(response.getStatusCode()).as("HTTP status created expected").isEqualTo(CREATED);

        // verify attendance record has been added
        Optional<Appearance> appearanceOpt =
            appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDate(requestDto.getJurorNumber(),
                requestDto.getPoolNumber(), requestDto.getAttendanceDate());
        assertThat(appearanceOpt).isNotEmpty();
        Appearance appearance = appearanceOpt.get();
        assertThat(appearance.getJurorNumber()).isEqualTo(requestDto.getJurorNumber());
        assertThat(appearance.getAttendanceDate()).isEqualTo(requestDto.getAttendanceDate());
        assertThat(appearance.getPoolNumber()).isEqualTo(requestDto.getPoolNumber());
        assertThat(appearance.getCourtLocation().getLocCode()).isEqualTo(requestDto.getLocationCode());
        assertThat(appearance.getTimeIn()).isEqualTo(requestDto.getCheckInTime());
        assertThat(appearance.getTimeOut()).isEqualTo(requestDto.getCheckOutTime());
    }


    @Test
    @DisplayName("POST addAttendanceDay() - Completed Juror")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAddAttendanceDay.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    void addAttendanceDayCompletedJuror() {
        AddAttendanceDayDto requestDto = AddAttendanceDayDto.builder()
            .jurorNumber(JUROR2)
            .poolNumber("415230101")
            .locationCode("415")
            .attendanceDate(now())
            .checkInTime(LocalTime.of(9, 30))
            .checkOutTime(LocalTime.of(17, 30))
            .build();

        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-management/add-attendance-day")), String.class);

        assertThat(response.getStatusCode()).as("HTTP status created expected").isEqualTo(CREATED);

        // verify attendance record has been added
        Optional<Appearance> appearanceOpt =
            appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDate(requestDto.getJurorNumber(),
                requestDto.getPoolNumber(), requestDto.getAttendanceDate());
        assertThat(appearanceOpt).isNotEmpty();
        Appearance appearance = appearanceOpt.get();
        assertThat(appearance.getJurorNumber()).isEqualTo(requestDto.getJurorNumber());
        assertThat(appearance.getAttendanceDate()).isEqualTo(requestDto.getAttendanceDate());
        assertThat(appearance.getPoolNumber()).isEqualTo(requestDto.getPoolNumber());
        assertThat(appearance.getCourtLocation().getLocCode()).isEqualTo(requestDto.getLocationCode());
        assertThat(appearance.getTimeIn()).isEqualTo(requestDto.getCheckInTime());
        assertThat(appearance.getTimeOut()).isEqualTo(requestDto.getCheckOutTime());
    }



    @Test
    @DisplayName("POST addAttendanceDay() - happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAddAttendanceDay.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    void addAttendanceBadPayloadJurorNumber() {
        AddAttendanceDayDto requestDto = AddAttendanceDayDto.builder()
            .jurorNumber("1234567890")
            .poolNumber("415230101")
            .locationCode("415")
            .attendanceDate(now())
            .checkInTime(LocalTime.of(9, 30))
            .checkOutTime(LocalTime.of(17, 30))
            .build();

        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-management/add-attendance-day")), String.class);

        assertThat(response.getStatusCode()).as(HTTP_STATUS_BAD_REQUEST_MESSAGE).isEqualTo(BAD_REQUEST);

    }

    @Test
    @DisplayName("POST addBadPayloadAttendanceDayInFuture() - sad path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAddAttendanceDay.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    void addBadPayloadAttendanceDayInFuture() {
        AddAttendanceDayDto requestDto = AddAttendanceDayDto.builder()
            .jurorNumber(JUROR1)
            .poolNumber("415230101")
            .locationCode("415")
            //attendance day one day in future - edge case
            .attendanceDate(now().plusDays(1))
            .checkInTime(LocalTime.of(9, 30))
            .checkOutTime(LocalTime.of(17, 30))
            .build();

        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-management/add-attendance-day")), String.class);

        assertThat(response.getStatusCode()).as("HTTP status created expected").isEqualTo(BAD_REQUEST);
    }

    @Test
    @DisplayName("POST addAttendanceDay() - happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAddAttendanceDay.sql"})
    void addAttendanceBadPayloadMissingAttendanceDate() {
        AddAttendanceDayDto requestDto = AddAttendanceDayDto.builder()
            .jurorNumber(JUROR1)
            .poolNumber("415230101")
            .locationCode("400")
            .attendanceDate(null)
            .checkInTime(LocalTime.of(9, 30))
            .checkOutTime(LocalTime.of(17, 30))
            .build();

        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-management/add-attendance-day")), String.class);

        assertThat(response.getStatusCode()).as(HTTP_STATUS_BAD_REQUEST_MESSAGE).isEqualTo(BAD_REQUEST);

    }

    @Test
    @DisplayName("POST addAttendanceDay() - happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAddAttendanceDay.sql"})
    void addAttendanceForbiddenAccess() {
        AddAttendanceDayDto requestDto = AddAttendanceDayDto.builder()
            .jurorNumber(JUROR1)
            .poolNumber("415230101")
            .locationCode("400")
            .attendanceDate(now())
            .checkInTime(LocalTime.of(9, 30))
            .checkOutTime(LocalTime.of(17, 30))
            .build();

        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-management/add-attendance-day")), String.class);

        assertThat(response.getStatusCode()).as("FORBIDDEN").isEqualTo(FORBIDDEN);

    }

    @Test
    @DisplayName("POST addAttendanceDay() - happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAddAttendanceDay.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    void addAttendanceForbiddenAccessToJurorPool() {
        AddAttendanceDayDto requestDto = AddAttendanceDayDto.builder()
            .jurorNumber(JUROR1)
            .poolNumber("415230101")
            .locationCode("475")
            .attendanceDate(now())
            .checkInTime(LocalTime.of(9, 30))
            .checkOutTime(LocalTime.of(17, 30))
            .build();

        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-management/add-attendance-day")), String.class);

        assertThat(response.getStatusCode()).as("FORBIDDEN").isEqualTo(FORBIDDEN);

    }

    @Test
    @DisplayName("POST addAttendanceDay() - happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAddAttendanceDay.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    void addAttendancePoolNumberNotFound() {
        AddAttendanceDayDto requestDto = AddAttendanceDayDto.builder()
            .jurorNumber(JUROR1)
            .poolNumber("123456789")
            .locationCode("400")
            .attendanceDate(now())
            .checkInTime(LocalTime.of(9, 30))
            .checkOutTime(LocalTime.of(17, 30))
            .build();

        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-management/add-attendance-day")), String.class);

        assertThat(response.getStatusCode()).as("NOT_FOUND").isEqualTo(NOT_FOUND);

    }

    @Test
    @DisplayName("GET getAppearanceRecords() - all AT_COURT statuses, happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAppearanceTests.sql"})
    void testGetAppearanceAtCourtHappyPath() {
        String localDate = now().minusDays(2).toString().formatted("YYYY-mm-dd");

        ResponseEntity<JurorAppearanceResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, GET,
                    URI.create("/api/v1/moj/juror-management/appearance?locationCode=415&attendanceDate=" + localDate
                        + "&group=" + JurorStatusGroup.AT_COURT)),
                JurorAppearanceResponseDto.class);

        validateAppearanceRecordMultiple(response);
    }


    @Test
    @DisplayName("GET getAppearanceRecords() - IN_WAITING statuses, happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAppearanceTests.sql"})
    void testGetAppearanceInWaitingHappyPath() {
        String localDate = now().minusDays(2).toString().formatted("YYYY-mm-dd");

        ResponseEntity<JurorAppearanceResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, GET,
                    URI.create("/api/v1/moj/juror-management/appearance?locationCode=415&attendanceDate=" + localDate
                        + "&group=" + JurorStatusGroup.IN_WAITING)),
                JurorAppearanceResponseDto.class);


        assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

        JurorAppearanceResponseDto jurorAppearanceResponseDto = response.getBody();
        assert jurorAppearanceResponseDto != null;
        assertThat(jurorAppearanceResponseDto.getData()).as("Expect 2 records to be returned")
            .hasSize(2);

        JurorAppearanceResponseDto.JurorAppearanceResponseData jurorAppearanceResponseData =
            jurorAppearanceResponseDto.getData().get(0);
        assertThat(jurorAppearanceResponseData.getJurorNumber()).isEqualTo(JUROR1);
        assertThat(jurorAppearanceResponseData.getFirstName()).isEqualTo("TEST");
        assertThat(jurorAppearanceResponseData.getLastName()).isEqualTo("LASTNAME");
        assertThat(jurorAppearanceResponseData.getJurorStatus()).isEqualTo(IJurorStatus.RESPONDED);
        assertThat(jurorAppearanceResponseData.getCheckInTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(jurorAppearanceResponseData.getCheckOutTime()).isNull();
        assertThat(jurorAppearanceResponseData.getPoliceCheck()).isEqualTo(PoliceCheck.NOT_CHECKED);

        jurorAppearanceResponseData =
            jurorAppearanceResponseDto.getData().get(1);
        assertThat(jurorAppearanceResponseData.getJurorNumber()).isEqualTo(JUROR3);
        assertThat(jurorAppearanceResponseData.getJurorStatus()).isEqualTo(IJurorStatus.PANEL);
        assertThat(jurorAppearanceResponseData.getPoliceCheck()).isEqualTo(PoliceCheck.INELIGIBLE);
    }

    @Test
    @DisplayName("GET getAppearanceRecords() - all ON_TRIAL statuses, happy path")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitAppearanceTests.sql"})
    void testGetAppearanceOnTrialHappyPath() {
        String localDate = now().minusDays(2).toString().formatted("YYYY-mm-dd");

        ResponseEntity<JurorAppearanceResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, GET,
                    URI.create("/api/v1/moj/juror-management/appearance?locationCode=415&attendanceDate=" + localDate
                        + "&group=" + JurorStatusGroup.ON_TRIAL)),
                JurorAppearanceResponseDto.class);


        assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

        JurorAppearanceResponseDto jurorAppearanceResponseDto = response.getBody();
        assert jurorAppearanceResponseDto != null;
        assertThat(jurorAppearanceResponseDto.getData()).as("Expect 1 record to be returned")
            .hasSize(1);

        JurorAppearanceResponseDto.JurorAppearanceResponseData jurorAppearanceResponseData =
            jurorAppearanceResponseDto.getData().get(0);

        assertThat(jurorAppearanceResponseData.getJurorNumber()).isEqualTo(JUROR2);
        assertThat(jurorAppearanceResponseData.getJurorStatus()).isEqualTo(IJurorStatus.JUROR);
        assertThat(jurorAppearanceResponseData.getPoliceCheck()).isEqualTo(PoliceCheck.ELIGIBLE);
    }

    @Test
    @DisplayName("GET getAppearanceRecords() - no records found for criteria")
    void testGetAppearanceUnhappyPath() {
        ResponseEntity<JurorAppearanceResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, GET,
                    URI.create("/api/v1/moj/juror-management/appearance?locationCode=415&attendanceDate=2023-10-08"
                        + "&group=" + JurorStatusGroup.AT_COURT)),
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
                .isEqualTo(3L);

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

            for (Appearance appearance : appearanceRepository.findAll()) {
                assertThat(appearance.getSatOnJury()).isNull();
            }
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

            for (Appearance appearance : appearanceRepository.findAll()) {
                assertThat(appearance.getSatOnJury()).isNull();
            }
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

            for (Appearance appearance : appearanceRepository.findAll()) {
                assertThat(appearance.getSatOnJury()).isNull();
            }
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
        @DisplayName("PATCH Update attendance - check out all panelled jurors")
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

            for (Appearance appearance : appearanceRepository.findAll()) {
                assertThat(appearance.getSatOnJury()).isNull();
            }
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

            for (Appearance appearance : appearanceRepository.findAll()) {
                assertThat(appearance.getSatOnJury()).isNull();
            }
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

            for (Appearance appearance : appearanceRepository.findAll()) {
                assertThat(appearance.getSatOnJury()).isNull();
            }
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

            for (Appearance appearance : appearanceRepository.findAll()) {
                assertThat(appearance.getSatOnJury()).isNull();
            }
        }

        @Test
        @DisplayName("PATCH Update attendance - confirm attendance (no shows)")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql",
            "/db/JurorExpenseControllerITest_expenseRates.sql"})
        void updateAttendanceNoShow() {
            UpdateAttendanceDto request = buildUpdateAttendanceDto(new ArrayList<>());
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
                .isEqualTo(4L);

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
                .containsExactlyInAnyOrder(EXPENSE_ENTERED, EXPENSE_ENTERED, EXPENSE_ENTERED);

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

            for (Appearance appearance : appearanceRepository.findAll()) {
                assertThat(appearance.getSatOnJury()).isNull();
            }
        }

        @Test
        @DisplayName("PATCH Update attendance - confirm attendance")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDetails.sql",
            "/db/JurorExpenseControllerITest_expenseRates.sql"})
        void updateAttendanceConfirmAttendance() {
            UpdateAttendanceDto request = buildUpdateAttendanceDto(List.of(JUROR1, JUROR6));
            request.getCommonData().setStatus(UpdateAttendanceStatus.CONFIRM_ATTENDANCE);

            ResponseEntity<AttendanceDetailsResponse> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE)), AttendanceDetailsResponse.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);

            AttendanceDetailsResponse responseBody = response.getBody();
            assert responseBody != null;

            AttendanceDetailsResponse.Summary summary = responseBody.getSummary();
            assert summary != null;

            assertThat(summary)
                .extracting(AttendanceDetailsResponse.Summary::getCheckedIn)
                .isEqualTo(2L);

            // verify attendance details have been updated x 2 checked in
            Appearance appearance1 =
                appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR1, request.getCommonData()
                    .getAttendanceDate()).orElseThrow(() ->
                    new MojException.NotFound("No appearance record found", null));
            assertThat(appearance1.getAppearanceStage()).isEqualTo(EXPENSE_ENTERED);
            assertThat(appearance1.getAttendanceAuditNumber()).isEqualTo("P10000000");

            Appearance appearance2 =
                appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR6, request.getCommonData()
                    .getAttendanceDate()).orElseThrow(() ->
                    new MojException.NotFound("No appearance record found", null));
            assertThat(appearance2.getAppearanceStage()).isEqualTo(EXPENSE_ENTERED);
            assertThat(appearance2.getAttendanceAuditNumber()).isEqualTo("P10000000");
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

            for (Appearance appearance : appearanceRepository.findAll()) {
                assertThat(appearance.getSatOnJury()).isNull();
            }
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
    @DisplayName("PATCH Update attendance date")
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/UpdateAttendanceDate.sql"})
    class UpdateAttendanceDate {
        static final LocalDate ATTENDANCE_DATE = now().plusMonths(1);
        static final String POOL_NUMBER_415230101 = "415230101";
        static final String URL_ATTENDANCE_DATE = "/attendance-date";
        static final String UPDATED_ATTENDANCE_DATE_MESSAGE = "Attendance date should have been updated to "
            + ATTENDANCE_DATE;
        static final String RESPONSE_MESSAGE = "Response message from api should confirm %s jurors were updated";
        static final String RESPONSE_EQUAL_TO = "Attendance date updated for %s juror(s)";

        @Test
        @DisplayName("Update attendance date - all jurors updated successfully")
        void updateAttendanceDateAllUpdatedSuccessfully() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR1);
            jurorNumbers.add(JUROR2);
            UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            // check the attendance date for one of the jurors before invoking the api
            LocalDate attendanceDateBefore = retrieveAttendanceDate(JUROR1, POOL_NUMBER_415230101);
            assertThat(attendanceDateBefore).as(UPDATED_ATTENDANCE_DATE_MESSAGE).isNotEqualTo(ATTENDANCE_DATE);

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                .as(String.format(RESPONSE_MESSAGE, 2))
                .isEqualTo(String.format(RESPONSE_EQUAL_TO, 2));

            // verify the attendance date was updated successfully
            LocalDate attendanceDateAfterJuror1 = retrieveAttendanceDate(JUROR1, POOL_NUMBER_415230101);
            assertThat(attendanceDateAfterJuror1).as(UPDATED_ATTENDANCE_DATE_MESSAGE).isEqualTo(ATTENDANCE_DATE);

            LocalDate attendanceDateAfterJuror2 = retrieveAttendanceDate(JUROR2, POOL_NUMBER_415230101);
            assertThat(attendanceDateAfterJuror2).as(UPDATED_ATTENDANCE_DATE_MESSAGE).isEqualTo(ATTENDANCE_DATE);
        }

        @Test
        @DisplayName("Update attendance date - some jurors updated successfully")
        void updateAttendanceDateSomeUpdatedSuccessfully() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR1);
            jurorNumbers.add(JUROR2);
            jurorNumbers.add("123456799"); // this juror does not exist
            UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                .as(String.format(RESPONSE_MESSAGE, 2))
                .isEqualTo(String.format(RESPONSE_EQUAL_TO, 2));

            // verify the attendance date was updated successfully for two jurors
            LocalDate attendanceDateAfterJuror1 = retrieveAttendanceDate(JUROR1, POOL_NUMBER_415230101);
            assertThat(attendanceDateAfterJuror1).as(UPDATED_ATTENDANCE_DATE_MESSAGE).isEqualTo(ATTENDANCE_DATE);

            LocalDate attendanceDateAfterJuror2 = retrieveAttendanceDate(JUROR2, POOL_NUMBER_415230101);
            assertThat(attendanceDateAfterJuror2).as(UPDATED_ATTENDANCE_DATE_MESSAGE).isEqualTo(ATTENDANCE_DATE);
        }

        @Test
        @DisplayName("Update attendance date - on-call flag updated")
        void updateAttendanceDateOnCallFlagUpdated() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR6);
            UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            // check the on-call flag before invoking the api
            Boolean onCallFlagBefore =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR6, POOL_NUMBER_415230101).getOnCall();
            assertThat(onCallFlagBefore).as("On-call flag should be True").isEqualTo(Boolean.TRUE);

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                .as(String.format(RESPONSE_MESSAGE, 1))
                .isEqualTo(String.format(RESPONSE_EQUAL_TO, 1));

            // verify the on-call flag was updated successfully
            Boolean onCallFlagAfter =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR6, POOL_NUMBER_415230101).getOnCall();
            assertThat(onCallFlagAfter).as("On-call flag should be False").isEqualTo(Boolean.FALSE);
        }

        @Test
        @DisplayName("Update attendance date - criteria is not met (record does not belong to officer)")
        void updateAttendanceDateCriteriaNotMetOwner() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR3);
            UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            // check the attendance date before invoking the api
            LocalDate attendanceDateBefore = retrieveAttendanceDate(JUROR3, POOL_NUMBER_415230101);

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                .as(String.format(RESPONSE_MESSAGE, 0))
                .isEqualTo(String.format(RESPONSE_EQUAL_TO, 0));

            // verify the attendance date was not updated
            LocalDate attendanceDateAfter = retrieveAttendanceDate(JUROR3, POOL_NUMBER_415230101);
            assertThat(attendanceDateAfter).as(UPDATED_ATTENDANCE_DATE_MESSAGE).isEqualTo(attendanceDateBefore);
        }

        @Test
        @DisplayName("Update attendance date - criteria is not met (juror number does not exist)")
        void updateAttendanceDateCriteriaNotMetJurorNumber() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add("123456799");
            UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                .as(String.format(RESPONSE_MESSAGE, 0))
                .isEqualTo(String.format(RESPONSE_EQUAL_TO, 0));
        }

        @Test
        @DisplayName("Update attendance date - criteria is not met (juror in a different pool)")
        void updateAttendanceDateCriteriaNotMetPoolNumber() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR4);
            UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                .as(String.format(RESPONSE_MESSAGE, 0))
                .isEqualTo(String.format(RESPONSE_EQUAL_TO, 0));
        }

        @Test
        @DisplayName("Update attendance date - criteria is not met (is not active juror pool)")
        void updateAttendanceDateCriteriaNotMetIsNotActive() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR5);
            UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            LocalDate attendanceDateBefore = retrieveAttendanceDate(JUROR5, POOL_NUMBER_415230101);

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                .as(String.format(RESPONSE_MESSAGE, 0))
                .isEqualTo(String.format(RESPONSE_EQUAL_TO, 0));

            // verify the attendance date was not updated
            LocalDate attendanceDateAfter = retrieveAttendanceDate(JUROR5, POOL_NUMBER_415230101);
            assertThat(attendanceDateAfter).as(UPDATED_ATTENDANCE_DATE_MESSAGE).isEqualTo(attendanceDateBefore);
        }

        @Test
        @DisplayName("Update attendance date - criteria is not met (status is excused)")
        void updateAttendanceDateCriteriaNotMetStatusIsExcused() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR7);
            UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            LocalDate attendanceDateBefore = retrieveAttendanceDate(JUROR7, POOL_NUMBER_415230101);

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                .as(String.format(RESPONSE_MESSAGE, 0))
                .isEqualTo(String.format(RESPONSE_EQUAL_TO, 0));

            // verify the attendance date was not updated
            LocalDate attendanceDateAfter = retrieveAttendanceDate(JUROR7, POOL_NUMBER_415230101);
            assertThat(attendanceDateAfter).as(UPDATED_ATTENDANCE_DATE_MESSAGE).isEqualTo(attendanceDateBefore);
        }

        @Test
        @DisplayName("Update attendance date - invalid request (no juror numbers)")
        void updateAttendanceDateInvalidRequestNoJurorNumbers() {
            List<String> jurorNumbers = new ArrayList<>();
            UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(BAD_REQUEST);
            assertInvalidPayload(responseEntity,
                new RestResponseEntityExceptionHandler.FieldError("jurorNumbers",
                    "Request should contain at least one juror number"));
        }

        @Test
        @DisplayName("Update attendance date - invalid request (no pool number)")
        void updateAttendanceDateInvalidRequestNoPoolNumber() {
            UpdateAttendanceDateDto request = UpdateAttendanceDateDto.builder()
                .jurorNumbers(Collections.singletonList(JUROR1))
                .attendanceDate(ATTENDANCE_DATE)
                .poolNumber(null)
                .build();

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(BAD_REQUEST);
            assertInvalidPayload(responseEntity,
                new RestResponseEntityExceptionHandler.FieldError("poolNumber",
                    "Request should contain a valid pool number"));
        }

        @Test
        @DisplayName("Update attendance date - invalid request (invalid pool number)")
        void updateAttendanceDateInvalidRequestInvalidPoolNumber() {
            UpdateAttendanceDateDto request = UpdateAttendanceDateDto.builder()
                .jurorNumbers(Collections.singletonList(JUROR1))
                .attendanceDate(ATTENDANCE_DATE)
                .poolNumber("12345")
                .build();

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(BAD_REQUEST);
            assertInvalidPayload(responseEntity,
                new RestResponseEntityExceptionHandler.FieldError("poolNumber", "must match \"^\\d{9}$\""));
        }

        @Test
        @DisplayName("Update attendance date - invalid request (missing attendance date)")
        void updateAttendanceDateInvalidRequestMissingAttendanceDate() {
            UpdateAttendanceDateDto request = UpdateAttendanceDateDto.builder()
                .jurorNumbers(Collections.singletonList(JUROR1))
                .attendanceDate(null)
                .poolNumber(POOL_NUMBER_415230101)
                .build();

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create(URL_ATTENDANCE + URL_ATTENDANCE_DATE)), String.class);

            assertThat(responseEntity.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(BAD_REQUEST);
            assertInvalidPayload(responseEntity,
                new RestResponseEntityExceptionHandler.FieldError("attendanceDate",
                    "Request should contain the new attendance date"));
        }

        private LocalDate retrieveAttendanceDate(String jurorNumber, String poolNumber) {
            return jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber).getNextDate();
        }

        private UpdateAttendanceDateDto buildUpdateAttendanceDateDto(List<String> jurorNumbers) {
            return UpdateAttendanceDateDto.builder()
                .jurorNumbers(jurorNumbers)
                .attendanceDate(ATTENDANCE_DATE)
                .poolNumber(POOL_NUMBER_415230101)
                .build();
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

    @Nested
    @DisplayName("GET jurors to dismiss list")
    @Sql({"/db/mod/truncate.sql", "/db/JurorManagementController_poolsAtCourtLocation.sql"})
    class JurorsToDismissList {
        private static final String URL = "/api/v1/moj/juror-management/jurors-to-dismiss";

        @Test
        @DisplayName("GET jurors to dismiss list - happy path")
        void retrieveJurorsToDismissListHappy() {
            List<String> pools = createPools("415230101");

            JurorsToDismissRequestDto request = createJurorsToDismissRequestDto(pools, true, true, 3);

            ResponseEntity<JurorsToDismissResponseDto> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, GET,
                    URI.create(URL)), JurorsToDismissResponseDto.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(Objects.requireNonNull(response.getBody()).getData()).isNotNull();

            List<JurorsToDismissResponseDto.JurorsToDismissData> jurorsToDismissData = response.getBody().getData();
            assertThat(jurorsToDismissData).as("Expect there to be 3 Juror record").hasSize(3);
        }

        @Test
        @DisplayName("GET jurors to dismiss list - include not in attendance")
        void retrieveJurorsToDismissListIncludeNotInAttendance() {
            List<String> pools = createPools("415230101");

            JurorsToDismissRequestDto request = createJurorsToDismissRequestDto(pools, true, true, 4);

            ResponseEntity<JurorsToDismissResponseDto> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, GET,
                    URI.create(URL)), JurorsToDismissResponseDto.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(Objects.requireNonNull(response.getBody()).getData()).isNotNull();

            assertThat(response.getBody().getData())
                .hasSize(4)
                .extracting(JurorsToDismissResponseDto.JurorsToDismissData::getJurorNumber,
                    JurorsToDismissResponseDto.JurorsToDismissData::getFirstName,
                    JurorsToDismissResponseDto.JurorsToDismissData::getLastName,
                    JurorsToDismissResponseDto.JurorsToDismissData::getAttending,
                    JurorsToDismissResponseDto.JurorsToDismissData::getNextDueAtCourt,
                    JurorsToDismissResponseDto.JurorsToDismissData::getCheckInTime,
                    JurorsToDismissResponseDto.JurorsToDismissData::getServiceStartDate)
                .containsExactlyInAnyOrder(
                    tuple("641500003", "TEST", "PERSON3", "In attendance",
                        now().minusDays(10).toString(),
                        LocalTime.of(9, 30), now().minusDays(10)),
                    tuple("641500004", "TEST", "PERSON4", "On call", "On call", null,
                        now().minusDays(10)),
                    tuple("641500006", "TEST", "PERSON6", "In attendance",
                        now().minusDays(10).toString(), LocalTime.of(9, 30),
                        now().minusDays(10)),
                    tuple("641500007", "TEST", "PERSON7", "Other",
                        now().minusDays(10).toString(), null,
                        now().minusDays(10))
                );
        }

        @Test
        @DisplayName("GET jurors to dismiss list - Unhappy path, Bureau User not allowed")
        void retrieveJurorsToDismissListUnhappyBureauUser() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwt("BUREAU_USER", "400"));

            List<String> pools = createPools("415930101");

            JurorsToDismissRequestDto request = createJurorsToDismissRequestDto(pools, true, true, 3);

            ResponseEntity<JurorsToDismissResponseDto> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, GET,
                    URI.create(URL)), JurorsToDismissResponseDto.class);

            assertThat(response.getStatusCode()).as("Expect HTTP Status of Forbidden").isEqualTo(FORBIDDEN);
            assertThat(Objects.requireNonNull(response.getBody()).getData()).isNull();
        }

        @Test
        @DisplayName("GET jurors to dismiss list - Unhappy path, pool not found")
        void retrieveJurorsToDismissListUnhappyPoolNotFound() {
            List<String> pools = createPools("415930101");  // pool does not exist

            JurorsToDismissRequestDto request = createJurorsToDismissRequestDto(pools, true, true, 3);

            ResponseEntity<JurorsToDismissResponseDto> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, GET,
                    URI.create(URL)), JurorsToDismissResponseDto.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_OK_MESSAGE).isEqualTo(OK);
            assertThat(Objects.requireNonNull(response.getBody()).getData()).isEmpty();
        }

        private List<String> createPools(String poolNumber) {
            List<String> pools = new ArrayList<>();
            pools.add(poolNumber);

            return pools;
        }

        private JurorsToDismissRequestDto createJurorsToDismissRequestDto(List<String> pools,
                                                                          Boolean includeNotInAttendance,
                                                                          Boolean includeOnCall,
                                                                          int numberOfJurorsToDismiss) {
            return JurorsToDismissRequestDto.builder()
                .poolNumbers(pools)
                .locationCode("415")
                .includeNotInAttendance(includeNotInAttendance)
                .includeOnCall(includeOnCall)
                .numberOfJurorsToDismiss(numberOfJurorsToDismiss)
                .build();
        }
    }

    @Nested
    @DisplayName("Non Attendance tests")
    class NonAttendance {

        @Test
        @DisplayName("Add non attendance - record okay")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitNonAttendance.sql",
            "/db/JurorExpenseControllerITest_expenseRates.sql"})
        void addNonAttendanceHappy() {

            JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber("111111111")
                .nonAttendanceDate(now())
                .poolNumber("415230101")
                .locationCode("415")
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create("/api/v1/moj/juror-management/non-attendance")), String.class);

            assertThat(response.getStatusCode()).as("HTTP status created expected").isEqualTo(CREATED);

            // verify non-attendance record has been added
            Optional<Appearance> appearanceOpt =
                appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDate(request.getJurorNumber(),
                    "415230101", request.getNonAttendanceDate());
            assertThat(appearanceOpt).isNotEmpty();
            Appearance appearance = appearanceOpt.get();
            assertThat(appearance.getJurorNumber()).isEqualTo(request.getJurorNumber());
            assertThat(appearance.getAttendanceDate()).isEqualTo(request.getNonAttendanceDate());
            assertThat(appearance.getPoolNumber()).isEqualTo(request.getPoolNumber());
            assertThat(appearance.getCourtLocation().getLocCode()).isEqualTo(request.getLocationCode());
            assertThat(appearance.getNonAttendanceDay()).isTrue();
            assertThat(appearance.getLossOfEarningsDue()).isEqualTo(BigDecimal.valueOf(63.25));

        }

        @Test
        @DisplayName("Add non attendance - completed juror okay")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitNonAttendance.sql",
            "/db/JurorExpenseControllerITest_expenseRates.sql"})
        void addNonAttendanceCompletedJuror() {

            JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(JUROR4)
                .nonAttendanceDate(now())
                .poolNumber("415230101")
                .locationCode("415")
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create("/api/v1/moj/juror-management/non-attendance")), String.class);

            assertThat(response.getStatusCode()).as("HTTP status created expected").isEqualTo(CREATED);

            // verify non-attendance record has been added
            Optional<Appearance> appearanceOpt =
                appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDate(request.getJurorNumber(),
                    "415230101", request.getNonAttendanceDate());
            assertThat(appearanceOpt).isNotEmpty();
            Appearance appearance = appearanceOpt.get();
            assertThat(appearance.getJurorNumber()).isEqualTo(request.getJurorNumber());
            assertThat(appearance.getAttendanceDate()).isEqualTo(request.getNonAttendanceDate());
            assertThat(appearance.getPoolNumber()).isEqualTo(request.getPoolNumber());
            assertThat(appearance.getCourtLocation().getLocCode()).isEqualTo(request.getLocationCode());
            assertThat(appearance.getNonAttendanceDay()).isTrue();
            assertThat(appearance.getLossOfEarningsDue()).isEqualTo(BigDecimal.valueOf(63.25));

        }

        @Test
        @DisplayName("Add non attendance - loss over limit")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitNonAttendance.sql",
            "/db/JurorExpenseControllerITest_expenseRates.sql"})
        void addNonAttendanceHappyLossOverLimit() {

            JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber("222222222")
                .nonAttendanceDate(now())
                .poolNumber("415230101")
                .locationCode("415")
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create("/api/v1/moj/juror-management/non-attendance")), String.class);

            assertThat(response.getStatusCode()).as("HTTP status created expected").isEqualTo(CREATED);

            // verify non-attendance record has been added
            Optional<Appearance> appearanceOpt =
                appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDate(request.getJurorNumber(),
                    "415230101", request.getNonAttendanceDate());
            assertThat(appearanceOpt).isNotEmpty();
            Appearance appearance = appearanceOpt.get();
            assertThat(appearance.getJurorNumber()).isEqualTo(request.getJurorNumber());
            assertThat(appearance.getAttendanceDate()).isEqualTo(request.getNonAttendanceDate());
            assertThat(appearance.getPoolNumber()).isEqualTo(request.getPoolNumber());
            assertThat(appearance.getCourtLocation().getLocCode()).isEqualTo(request.getLocationCode());
            assertThat(appearance.getNonAttendanceDay()).isTrue();
            assertThat(appearance.getLossOfEarningsDue()).isEqualTo(BigDecimal.valueOf(64.95));

        }

        @Test
        @DisplayName("Add non attendance - record already present")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitNonAttendance.sql"})
        void addNonAttendanceUnhappyAlreadyPresent() {

            JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber("222222222")
                .nonAttendanceDate(now().minusDays(1))
                .poolNumber("415230101")
                .locationCode("415")
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create("/api/v1/moj/juror-management/non-attendance")), String.class);

            assertThat(response.getStatusCode()).as("HTTP status unprocessable entity expected")
                .isEqualTo(UNPROCESSABLE_ENTITY);

            assertBusinessRuleViolation(response, "Juror 222222222 already has an attendance "
                + "record for the date " + now().minusDays(1), ATTENDANCE_RECORD_ALREADY_EXISTS);

        }

        @Test
        @DisplayName("Add non attendance - no show record already present")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitNonAttendance.sql",
            "/db/JurorExpenseControllerITest_expenseRates.sql"})
        void addNonAttendanceUnhappyNoShowAlreadyPresent() {

            JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber("333333333")
                .nonAttendanceDate(now().minusDays(1))
                .poolNumber("415230101")
                .locationCode("415")
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create("/api/v1/moj/juror-management/non-attendance")), String.class);

            assertThat(response.getStatusCode()).as("HTTP status created expected").isEqualTo(CREATED);

            // verify non-attendance record has been added
            Optional<Appearance> appearanceOpt =
                appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDate(request.getJurorNumber(),
                    "415230101", request.getNonAttendanceDate());
            assertThat(appearanceOpt).isNotEmpty();
            Appearance appearance = appearanceOpt.get();
            assertThat(appearance.getJurorNumber()).isEqualTo(request.getJurorNumber());
            assertThat(appearance.getAttendanceDate()).isEqualTo(request.getNonAttendanceDate());
            assertThat(appearance.getPoolNumber()).isEqualTo(request.getPoolNumber());
            assertThat(appearance.getCourtLocation().getLocCode()).isEqualTo(request.getLocationCode());
            assertThat(appearance.getNonAttendanceDay()).isTrue();
            assertThat(appearance.getLossOfEarningsDue()).isEqualTo(BigDecimal.valueOf(60.25));
        }

        @Test
        @DisplayName("Add non attendance - invalid court location")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitNonAttendance.sql"})
        void addNonAttendanceUnhappyInvalidCourt() {

            JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber("222222222")
                .nonAttendanceDate(now().minusDays(1))
                .poolNumber("415230101")
                .locationCode("999")
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create("/api/v1/moj/juror-management/non-attendance")), String.class);

            assertNotFound(response, "/api/v1/moj/juror-management/non-attendance",
                "Court location 999 not found");
        }

        @Test
        @DisplayName("Add non attendance - add a non attendance record invalid date")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitNonAttendance.sql"})
        void addNonAttendanceUnhappyInvalidDateBeforeStartDate() {

            JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber("111111111")
                .nonAttendanceDate(now().minusDays(20))
                .poolNumber("415230101")
                .locationCode("415")
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create("/api/v1/moj/juror-management/non-attendance")), String.class);

            assertThat(response.getStatusCode()).as("HTTP status unprocessable entity expected")
                .isEqualTo(UNPROCESSABLE_ENTITY);

            JSONObject exceptionDetails = getExceptionDetails(response);
            assertThat(exceptionDetails.getString("message")).isEqualTo("Non-attendance date is "
                + "before the service start date of the pool");
            assertThat(exceptionDetails.getString("code")).isEqualTo("APPEARANCE_RECORD_BEFORE_SERVICE_START_DATE");

        }

        @Test
        @DisplayName("Add non attendance - add a non attendance record date in the future")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/InitNonAttendance.sql"})
        void addBadPayloadNonAttendanceDayInFuture() {
            JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber("111111111")
                .nonAttendanceDate(now().plusDays(1))
                .poolNumber("415230101")
                .locationCode("415")
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create("/api/v1/moj/juror-management/non-attendance")), String.class);

            assertThat(response.getStatusCode()).as("HTTP status created expected").isEqualTo(BAD_REQUEST);
        }
    }


    @Nested
    @DisplayName("Jurors on Trial tests")
    class JurorsOnTrial {

        @Test
        @DisplayName("Get Jurors on Trials - happy path")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/JurorsOnTrial.sql"})
        void jurorsOnTrialHappy() {

            ResponseEntity<JurorsOnTrialResponseDto> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET,
                        URI.create("/api/v1/moj/juror-management/jurors-on-trial/415?attendanceDate="
                            + now())),
                    JurorsOnTrialResponseDto.class);

            assertThat(response.getStatusCode()).as("HTTP status OK expected").isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();

            // verify returned data
            JurorsOnTrialResponseDto jurorsOnTrialResponseDto = response.getBody();
            assertThat(jurorsOnTrialResponseDto.getTrialsList().size()).as("Expect 2 records to be returned")
                .isEqualTo(2);

            JurorsOnTrialResponseDto.JurorsOnTrialResponseData first = jurorsOnTrialResponseDto.getTrialsList().get(0);
            assertThat(first.getTrialNumber()).isEqualTo("T10000001");
            assertThat(first.getParties()).isEqualTo("test trial");
            assertThat(first.getTrialType()).isEqualTo("Civil");
            assertThat(first.getJudge()).isEqualTo("judge jose");
            assertThat(first.getCourtroom()).isEqualTo("small room");
            assertThat(first.getNumberAttended()).isEqualTo(2);
            assertThat(first.getTotalJurors()).isEqualTo(4);
            assertThat(first.getAttendanceAudit()).isEqualTo("J00000002");

            JurorsOnTrialResponseDto.JurorsOnTrialResponseData second = jurorsOnTrialResponseDto.getTrialsList().get(1);
            assertThat(second.getTrialNumber()).isEqualTo("T10000002");
            assertThat(second.getParties()).isEqualTo("test trial");
            assertThat(second.getTrialType()).isEqualTo("Civil");
            assertThat(second.getJudge()).isEqualTo("judge June");
            assertThat(second.getCourtroom()).isEqualTo("other room");
            assertThat(second.getNumberAttended()).isEqualTo(2);
            assertThat(second.getTotalJurors()).isEqualTo(3);
            assertThat(second.getAttendanceAudit()).isEqualTo("J00000003");
        }

        @Test
        @DisplayName("Get Jurors on Trials - Bureau User no access")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/JurorsOnTrial.sql"})
        void jurorsOnTrialBureauUserNoAccess() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwt("BUREAU_USER", "400"));
            ResponseEntity<JurorsOnTrialResponseDto> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET,
                        URI.create("/api/v1/moj/juror-management/jurors-on-trial/415?attendanceDate=" + now())),
                    JurorsOnTrialResponseDto.class);

            assertThat(response.getStatusCode()).as("HTTP status Forbidden expected").isEqualTo(FORBIDDEN);
        }

        @Test
        @DisplayName("Confirm attendance for jurors on a trial - happy path")
        @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/ConfirmJuryAttendance.sql",
            "/db/JurorExpenseControllerITest_expenseRates.sql"})
        void confirmAttendanceHappy() {

            UpdateAttendanceDto request = buildUpdateAttendanceDto();

            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create("/api/v1/moj/juror-management/confirm-jury-attendance")), Void.class);

            assertThat(response.getStatusCode()).as("HTTP status OK expected")
                .isEqualTo(OK);

            // verify attendance records have been updated
            Optional<Appearance> appearanceOpt =
                appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDate("222222222",
                    "415230101", now().minusDays(2));
            assertThat(appearanceOpt).isNotEmpty();
            Appearance appearance = appearanceOpt.get();
            assertThat(appearance.getTimeIn()).isEqualTo(LocalTime.of(9, 30));
            assertThat(appearance.getTimeOut()).isEqualTo(LocalTime.of(17, 00));
            assertThat(appearance.getAppearanceStage()).isEqualTo(EXPENSE_ENTERED);
            assertThat(appearance.getAttendanceAuditNumber()).isEqualTo("J10123456");
            assertThat(appearance.getSatOnJury()).isTrue();

            appearanceOpt = appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDate(
                "333333333", "415230101", now().minusDays(2));
            assertThat(appearanceOpt).isNotEmpty();
            appearance = appearanceOpt.get();
            assertThat(appearance.getTimeIn()).isEqualTo(LocalTime.of(9, 30));
            assertThat(appearance.getTimeOut()).isEqualTo(LocalTime.of(17, 00));
            assertThat(appearance.getAppearanceStage()).isEqualTo(EXPENSE_ENTERED);
            assertThat(appearance.getAttendanceAuditNumber()).isEqualTo("J10123456");
            assertThat(appearance.getSatOnJury()).isTrue();


            // verify juror history records have been created
            assertThat(jurorHistoryRepository.findByJurorNumber("222222222")
                .stream().anyMatch(jh -> jh.getHistoryCode().equals(HistoryCodeMod.JURY_ATTENDANCE)
                    && jh.getOtherInformation().equalsIgnoreCase("J10123456"))).isTrue();

            assertThat(jurorHistoryRepository.findByJurorNumber("333333333")
                .stream().anyMatch(jh -> jh.getHistoryCode().equals(HistoryCodeMod.JURY_ATTENDANCE)
                    && jh.getOtherInformation().equalsIgnoreCase("J10123456"))).isTrue();
        }

        @Test
        @DisplayName("Confirm attendance for jurors on a trial - Bureau no access")
        void confirmAttendanceBureauNoAccess() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwt("BUREAU_USER", "400"));
            UpdateAttendanceDto request = buildUpdateAttendanceDto();

            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, PATCH,
                    URI.create("/api/v1/moj/juror-management/confirm-jury-attendance")), Void.class);

            assertThat(response.getStatusCode()).as("HTTP status Forbidden expected")
                .isEqualTo(FORBIDDEN);
        }

        private UpdateAttendanceDto buildUpdateAttendanceDto() {
            UpdateAttendanceDto.CommonData commonData = new UpdateAttendanceDto.CommonData();
            commonData.setStatus(UpdateAttendanceStatus.CONFIRM_ATTENDANCE);
            commonData.setAttendanceDate(now().minusDays(2));
            commonData.setLocationCode("415");
            commonData.setCheckInTime(LocalTime.of(9, 30));
            commonData.setCheckOutTime(LocalTime.of(17, 00));
            commonData.setSingleJuror(Boolean.FALSE);

            UpdateAttendanceDto request = new UpdateAttendanceDto();
            request.setCommonData(commonData);
            request.setJuror(Arrays.asList("222222222", "333333333"));

            return request;
        }
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
        assertThat(jurorAppearanceResponseData.getPoliceCheck()).isEqualTo(PoliceCheck.NOT_CHECKED);
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
        assertThat(jurorAppearanceResponseData.getPoliceCheck()).isEqualTo(PoliceCheck.NOT_CHECKED);

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
