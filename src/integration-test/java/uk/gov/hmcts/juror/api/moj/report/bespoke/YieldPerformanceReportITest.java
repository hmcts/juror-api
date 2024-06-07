package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractControllerIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.YieldPerformanceReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.BDDAssertions.within;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + YieldPerformanceReportITest.URL)
@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/mod/reports/YieldPerformanceReportITest_typical.sql",
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert"//False positive
)
class YieldPerformanceReportITest extends AbstractControllerIntegrationTest<YieldPerformanceReportRequest,
    YieldPerformanceReportResponse> {
    public static final String URL = "/api/v1/moj/reports/yield-performance";
    public static final String LOCAL_DATE = "LocalDate";
    public static final String STRING = "String";

    @Autowired
    public YieldPerformanceReportITest(TestRestTemplate template) {
        super(HttpMethod.POST, template, HttpStatus.OK);
    }

    @Override
    protected String getValidUrl() {
        return URL;
    }

    @Override
    protected String getValidJwt() {
        return createJwt(
            "test_bureau_standard",
            "400",
            UserType.BUREAU,
            Set.of(),
            "400"
        );
    }

    @Override
    protected YieldPerformanceReportRequest getValidPayload() {
        return null;
    }

    @Test
    void viewByCourt() {

        YieldPerformanceReportRequest payload =  YieldPerformanceReportRequest.builder()
            .allCourts(false)
            .courtLocCodes(List.of("415"))
            .fromDate(LocalDate.parse("2024-08-01"))
            .toDate(LocalDate.parse("2024-08-20"))
            .build();

        testBuilder()
            .payload(payload)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveTimeCreated)
            .assertEquals(YieldPerformanceReportResponse.builder()
                .headings(Map.of(
                    "date_from", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Date from")
                        .dataType(LOCAL_DATE)
                        .value("2024-08-01")
                        .build(),
                    "date_to", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Date to")
                        .dataType(LOCAL_DATE)
                        .value("2024-08-20")
                        .build(),
                    "report_created", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Report created")
                        .dataType(LOCAL_DATE)
                        .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build()))
                .yieldPerformanceData(List.of(
                    YieldPerformanceReportResponse.YieldPerformanceData.builder()
                        .courtLocation("774")
                        .requested(11)
                        .confirmed(1)
                        .balance(10)
                        .difference(0.09)
                        .comments("Comments")
                        .build()))
                .build());
    }

    @Test
    void viewByCourts() {

        YieldPerformanceReportRequest payload =  YieldPerformanceReportRequest.builder()
            .allCourts(false)
            .courtLocCodes(List.of("415","774"))
            .fromDate(LocalDate.parse("2024-08-01"))
            .toDate(LocalDate.parse("2024-08-20"))
            .build();

        testBuilder()
            .payload(payload)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveTimeCreated)
            .assertEquals(YieldPerformanceReportResponse.builder()
                .headings(Map.of(
                    "date_from", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Date from")
                        .dataType(LOCAL_DATE)
                        .value("2024-08-01")
                        .build(),
                    "date_to", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Date to")
                        .dataType(LOCAL_DATE)
                        .value("2024-08-20")
                        .build(),
                    "report_created", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Report created")
                        .dataType(LOCAL_DATE)
                        .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build()))
                .yieldPerformanceData(List.of(
                    YieldPerformanceReportResponse.YieldPerformanceData.builder()
                        .courtLocation("415")
                        .requested(11)
                        .confirmed(1)
                        .balance(10)
                        .difference(0.09)
                        .comments("Comments")
                        .build(),
                    YieldPerformanceReportResponse.YieldPerformanceData.builder()
                        .courtLocation("774")
                        .requested(11)
                        .confirmed(1)
                        .balance(10)
                        .difference(0.09)
                        .comments("Comments")
                        .build()))
                .build());
    }

    @Test
    void viewByAllCourts() {

        YieldPerformanceReportRequest payload =  YieldPerformanceReportRequest.builder()
            .allCourts(true)
            .fromDate(LocalDate.parse("2024-08-01"))
            .toDate(LocalDate.parse("2024-08-07"))
            .build();

        testBuilder()
            .payload(payload)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveTimeCreated)
            .assertEquals(YieldPerformanceReportResponse.builder()
                .headings(Map.of("courts", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Courts")
                        .dataType(STRING)
                        .value("All courts")
                        .build(),
                    "date_from", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Date from")
                        .dataType(LOCAL_DATE)
                        .value("2024-08-01")
                        .build(),
                    "date_to", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Date to")
                        .dataType(LOCAL_DATE)
                        .value("2024-08-07")
                        .build(),
                    "report_created", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Report created")
                        .dataType(LOCAL_DATE)
                        .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build()))
                .yieldPerformanceData(List.of(
                    YieldPerformanceReportResponse.YieldPerformanceData.builder()
                        .courtLocation("415")
                        .requested(11)
                        .confirmed(1)
                        .balance(10)
                        .difference(0.09)
                        .comments("Comments")
                        .build(),
                    YieldPerformanceReportResponse.YieldPerformanceData.builder()
                        .courtLocation("774")
                        .requested(11)
                        .confirmed(1)
                        .balance(10)
                        .difference(0.09)
                        .comments("Comments")
                        .build()))
                .build());
    }

    @Test
    void unhappyCourtUser() {

        YieldPerformanceReportRequest payload =  YieldPerformanceReportRequest.builder()
            .allCourts(false)
            .courtLocCodes(List.of("415"))
            .fromDate(LocalDate.parse("2024-08-01"))
            .toDate(LocalDate.parse("2024-08-20"))
            .build();

        testBuilder()
            .payload(payload)
            .jwt(getCourtJwt())
            .triggerInvalid()
            .assertForbiddenResponse();
    }

    private String getCourtJwt() {
        return createJwt(
            "test_court_standard",
            "415",
            UserType.COURT,
            Set.of(),
            "415","462"
        );
    }


    public void verifyAndRemoveTimeCreated(YieldPerformanceReportResponse response) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getHeadings()).isNotNull();
        Assertions.assertThat(response.getHeadings().containsKey("report_created")).isTrue();

        AbstractReportResponse.DataTypeValue timeCreated =
            response.getHeadings().get("time_created");
        Assertions.assertThat(timeCreated).isNotNull();
        Assertions.assertThat(timeCreated.getDisplayName()).isEqualTo("Time created");
        Assertions.assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
        Assertions.assertThat(timeCreated.getValue()).isNotNull();
        LocalDateTime localDateTime = LocalDateTime.parse((String) timeCreated.getValue(),
            DateTimeFormatter.ISO_DATE_TIME);
        Assertions.assertThat(localDateTime).isCloseTo(LocalDateTime.now(),
            within(10, ChronoUnit.SECONDS));
        response.getHeadings().remove("time_created");
    }

}
