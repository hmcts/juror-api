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
@SuppressWarnings({"PMD.TooManyMethods",
    "PMD.JUnitTestsShouldIncludeAssert"}//False positive
)
class YieldPerformanceReportITest extends AbstractControllerIntegrationTest<YieldPerformanceReportRequest,
    YieldPerformanceReportResponse> {
    public static final String URL = "/api/v1/moj/reports/yield-performance";
    public static final String LOCAL_DATE = "LocalDate";

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
        return YieldPerformanceReportRequest.builder()
            .allCourts(false)
            .courtLocCodes(List.of("415","774"))
            .fromDate(LocalDate.parse("2024-08-01"))
            .toDate(LocalDate.parse("2024-08-20"))
            .build();
    }

    @Test
    void viewByCourtNegativeBalance() {

        testBuilder()
            .payload(YieldPerformanceReportRequest.builder()
            .allCourts(false)
            .courtLocCodes(List.of("415"))
            .fromDate(LocalDate.parse("2024-08-01"))
            .toDate(LocalDate.parse("2024-08-20"))
            .build())
            .triggerValid()
            .responseConsumer(this::assertHeadingsCourt)
            .responseConsumer(this::verifyCourtPayloadNegativeBalance);

    }

    @Test
    void viewByCourtPositiveBalance() {

        testBuilder()
            .payload(YieldPerformanceReportRequest.builder()
                .allCourts(false)
                .courtLocCodes(List.of("417"))
                .fromDate(LocalDate.parse("2024-08-01"))
                .toDate(LocalDate.parse("2024-08-20"))
                .build())
            .triggerValid()
            .responseConsumer(this::assertHeadingsCourt)
            .responseConsumer(this::verifyCourtPayloadPositiveBalance);

    }

    @Test
    void viewByCourts() {

        testBuilder()
            .triggerValid()
            .responseConsumer(this::assertHeadingsCourt)
            .responseConsumer(this::verifyCourtsPayload);

    }

    @Test
    void viewByAllCourts() {

        testBuilder()
            .payload(YieldPerformanceReportRequest.builder()
                .allCourts(true)
                .fromDate(LocalDate.parse("2024-07-21"))
                .toDate(LocalDate.parse("2024-08-07"))
                .build())
            .triggerValid()
            .responseConsumer(this::verifyHeadingsAllCourts)
            .responseConsumer(this::verifyAllCourtsPayload);

    }

    @Test
    void unhappyCourtUser() {

        testBuilder()
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


    public void assertHeadingsCourt(YieldPerformanceReportResponse response) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getHeadings()).isNotNull();

        Assertions.assertThat(response.getHeadings().containsKey("date_from")).isTrue();
        AbstractReportResponse.DataTypeValue dateFrom =
            response.getHeadings().get("date_from");
        Assertions.assertThat(dateFrom).isNotNull();
        Assertions.assertThat(dateFrom.getDisplayName()).isEqualTo("Date from");
        Assertions.assertThat(dateFrom.getDataType()).isEqualTo(LOCAL_DATE);
        Assertions.assertThat(dateFrom.getValue()).isNotNull();
        Assertions.assertThat(dateFrom.getValue()).isEqualTo("2024-08-01");

        Assertions.assertThat(response.getHeadings().containsKey("date_to")).isTrue();
        AbstractReportResponse.DataTypeValue dateTo =
            response.getHeadings().get("date_to");
        Assertions.assertThat(dateTo).isNotNull();
        Assertions.assertThat(dateTo.getDisplayName()).isEqualTo("Date to");
        Assertions.assertThat(dateTo.getDataType()).isEqualTo(LOCAL_DATE);
        Assertions.assertThat(dateTo.getValue()).isNotNull();
        Assertions.assertThat(dateTo.getValue()).isEqualTo("2024-08-20");

        Assertions.assertThat(response.getHeadings().containsKey("report_created")).isTrue();
        AbstractReportResponse.DataTypeValue timeCreated =
            response.getHeadings().get("report_created");
        Assertions.assertThat(timeCreated).isNotNull();
        Assertions.assertThat(timeCreated.getDisplayName()).isNull();
        Assertions.assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
        Assertions.assertThat(timeCreated.getValue()).isNotNull();
        LocalDateTime localDateTime = LocalDateTime.parse((String) timeCreated.getValue(),
            DateTimeFormatter.ISO_DATE_TIME);
        Assertions.assertThat(localDateTime).isCloseTo(LocalDateTime.now(),
            within(10, ChronoUnit.SECONDS));

        // verify the no of table headings
        Assertions.assertThat(response.getTableData()).isNotNull();
        Assertions.assertThat(response.getTableData().getHeadings()).isNotNull();
        Assertions.assertThat(response.getTableData().getHeadings().size()).isEqualTo(6);

        // verify the table headings
        Assertions.assertThat(response.getTableData().getHeadings().get(0).getId()).isEqualTo(
            YieldPerformanceReportResponse.TableHeading.COURT.getId());
        Assertions.assertThat(response.getTableData().getHeadings().get(0).getName()).isEqualTo("Court");
        Assertions.assertThat(response.getTableData().getHeadings().get(0).getDataType()).isEqualTo("String");

        Assertions.assertThat(response.getTableData().getHeadings().get(1).getId()).isEqualTo(
            YieldPerformanceReportResponse.TableHeading.REQUESTED.getId());
        Assertions.assertThat(response.getTableData().getHeadings().get(1).getName()).isEqualTo("Requested");
        Assertions.assertThat(response.getTableData().getHeadings().get(1).getDataType()).isEqualTo("Integer");

        Assertions.assertThat(response.getTableData().getHeadings().get(2).getId()).isEqualTo(
            YieldPerformanceReportResponse.TableHeading.CONFIRMED.getId());
        Assertions.assertThat(response.getTableData().getHeadings().get(2).getName()).isEqualTo("Confirmed");
        Assertions.assertThat(response.getTableData().getHeadings().get(2).getDataType()).isEqualTo("Integer");

        Assertions.assertThat(response.getTableData().getHeadings().get(3).getId()).isEqualTo(
            YieldPerformanceReportResponse.TableHeading.BALANCE.getId());
        Assertions.assertThat(response.getTableData().getHeadings().get(3).getName()).isEqualTo("Balance");
        Assertions.assertThat(response.getTableData().getHeadings().get(3).getDataType()).isEqualTo("Integer");

        Assertions.assertThat(response.getTableData().getHeadings().get(4).getId()).isEqualTo(
            YieldPerformanceReportResponse.TableHeading.DIFFERENCE.getId());
        Assertions.assertThat(response.getTableData().getHeadings().get(4).getName()).isEqualTo("Difference");
        Assertions.assertThat(response.getTableData().getHeadings().get(4).getDataType()).isEqualTo("Double");

        Assertions.assertThat(response.getTableData().getHeadings().get(5).getId()).isEqualTo(
            YieldPerformanceReportResponse.TableHeading.COMMENTS.getId());
        Assertions.assertThat(response.getTableData().getHeadings().get(5).getName()).isEqualTo("Comments");
        Assertions.assertThat(response.getTableData().getHeadings().get(5).getDataType()).isEqualTo("String");

    }

    public void verifyHeadingsAllCourts(YieldPerformanceReportResponse response) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getHeadings()).isNotNull();

        Assertions.assertThat(response.getHeadings().containsKey("date_from")).isTrue();
        AbstractReportResponse.DataTypeValue dateFrom =
            response.getHeadings().get("date_from");
        Assertions.assertThat(dateFrom).isNotNull();
        Assertions.assertThat(dateFrom.getDisplayName()).isEqualTo("Date from");
        Assertions.assertThat(dateFrom.getDataType()).isEqualTo(LOCAL_DATE);
        Assertions.assertThat(dateFrom.getValue()).isNotNull();
        Assertions.assertThat(dateFrom.getValue()).isEqualTo("2024-07-21");

        Assertions.assertThat(response.getHeadings().containsKey("date_to")).isTrue();
        AbstractReportResponse.DataTypeValue dateTo =
            response.getHeadings().get("date_to");
        Assertions.assertThat(dateTo).isNotNull();
        Assertions.assertThat(dateTo.getDisplayName()).isEqualTo("Date to");
        Assertions.assertThat(dateTo.getDataType()).isEqualTo(LOCAL_DATE);
        Assertions.assertThat(dateTo.getValue()).isNotNull();
        Assertions.assertThat(dateTo.getValue()).isEqualTo("2024-08-07");
    }

    @SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
    public void verifyCourtPayloadNegativeBalance(YieldPerformanceReportResponse response) {
        Assertions.assertThat(response).isNotNull();

        Assertions.assertThat(response.getTableData()).isNotNull();
        Assertions.assertThat(response.getTableData().getData()).isNotNull();
        Assertions.assertThat(response.getTableData().getData().size()).isEqualTo(1);

        YieldPerformanceReportResponse.TableData.YieldData data = response.getTableData().getData().get(0);
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getCourt()).isEqualTo("CHESTER (415)");
        Assertions.assertThat(data.getRequested()).isEqualTo(17);
        Assertions.assertThat(data.getConfirmed()).isEqualTo(4);
        Assertions.assertThat(data.getBalance()).isEqualTo(-13);
        Assertions.assertThat(data.getDifference()).isEqualTo(-76.47059);
        Assertions.assertThat(data.getComments()).isEqualTo("415240801 - This is a test comment 1"
            + System.lineSeparator() + "415240802 - This is a test comment 2");
    }

    public void verifyCourtPayloadPositiveBalance(YieldPerformanceReportResponse response) {
        Assertions.assertThat(response).isNotNull();

        Assertions.assertThat(response.getTableData()).isNotNull();
        Assertions.assertThat(response.getTableData().getData()).isNotNull();
        Assertions.assertThat(response.getTableData().getData().size()).isEqualTo(1);

        YieldPerformanceReportResponse.TableData.YieldData data = response.getTableData().getData().get(0);
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getCourt()).isEqualTo("COVENTRY (417)");
        Assertions.assertThat(data.getRequested()).isEqualTo(6);
        Assertions.assertThat(data.getConfirmed()).isEqualTo(9);
        Assertions.assertThat(data.getBalance()).isEqualTo(3);
        Assertions.assertThat(data.getDifference()).isEqualTo(50.0);
        Assertions.assertThat(data.getComments()).isEqualTo("");
    }

    @SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
    public void verifyCourtsPayload(YieldPerformanceReportResponse response) {
        Assertions.assertThat(response).isNotNull();

        Assertions.assertThat(response.getTableData()).isNotNull();
        Assertions.assertThat(response.getTableData().getData()).isNotNull();
        Assertions.assertThat(response.getTableData().getData().size()).isEqualTo(2);

        YieldPerformanceReportResponse.TableData.YieldData data = response.getTableData().getData().get(0);
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getCourt()).isEqualTo("CHESTER (415)");
        Assertions.assertThat(data.getRequested()).isEqualTo(17);
        Assertions.assertThat(data.getConfirmed()).isEqualTo(4);
        Assertions.assertThat(data.getBalance()).isEqualTo(-13);
        Assertions.assertThat(data.getDifference()).isEqualTo(-76.47059);
        Assertions.assertThat(data.getComments()).isEqualTo("415240801 - This is a test comment 1"
            + System.lineSeparator() + "415240802 - This is a test comment 2");

        data = response.getTableData().getData().get(1);
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getCourt()).isEqualTo("WELSHPOOL (774)");
        Assertions.assertThat(data.getRequested()).isEqualTo(11);
        Assertions.assertThat(data.getConfirmed()).isEqualTo(1);
        Assertions.assertThat(data.getBalance()).isEqualTo(-10);
        Assertions.assertThat(data.getDifference()).isEqualTo(-90.90909);
        Assertions.assertThat(data.getComments()).isEqualTo("");

    }

    @SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
    public void verifyAllCourtsPayload(YieldPerformanceReportResponse response) {
        Assertions.assertThat(response).isNotNull();

        Assertions.assertThat(response.getTableData()).isNotNull();
        Assertions.assertThat(response.getTableData().getData()).isNotNull();
        Assertions.assertThat(response.getTableData().getData().size()).isEqualTo(2);

        YieldPerformanceReportResponse.TableData.YieldData data = response.getTableData().getData().get(0);
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getCourt()).isEqualTo("CHESTER (415)");
        Assertions.assertThat(data.getRequested()).isEqualTo(17);
        Assertions.assertThat(data.getConfirmed()).isEqualTo(4);
        Assertions.assertThat(data.getBalance()).isEqualTo(-13);
        Assertions.assertThat(data.getDifference()).isEqualTo(-76.47059);
        Assertions.assertThat(data.getComments()).isEqualTo("415240801 - This is a test comment 1"
            + System.lineSeparator() + "415240802 - This is a test comment 2");

        data = response.getTableData().getData().get(1);
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getCourt()).isEqualTo("LEWES SITTING AT CHICHESTER (416)");
        Assertions.assertThat(data.getRequested()).isEqualTo(10);
        Assertions.assertThat(data.getConfirmed()).isEqualTo(3);
        Assertions.assertThat(data.getBalance()).isEqualTo(-7);
        Assertions.assertThat(data.getDifference()).isEqualTo(-70.0);
        Assertions.assertThat(data.getComments()).isEqualTo("416240801 - This is a test comment 3");

    }

}
