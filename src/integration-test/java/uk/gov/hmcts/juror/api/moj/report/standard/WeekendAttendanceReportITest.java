package uk.gov.hmcts.juror.api.moj.report.standard;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/WeekendAttendanceReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class WeekendAttendanceReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    public WeekendAttendanceReportITest(TestRestTemplate template) {
        super(template, WeekendAttendanceReport.class);
    }

    @Override
    protected String getValidJwt() {

        final BureauJwtPayload bureauJwtPayload = TestUtils.getJwtPayloadSuperUser("415", "Chester");

        return mintBureauJwt(bureauJwtPayload);
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .locCode("415")
            .build());
    }

    @Test
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyReportResponse);
    }

    @Test
    void positiveTypicalBureau() {
        testBuilder()
            .jwt(getValidBureauJwt())
            .triggerValid()
            .responseConsumer(this::verifyReportResponse);
    }

    @Test
    void negativeInvalidCourtLocCode() {
        StandardReportRequest request = getValidPayload();
        request.setLocCode("999");
        ControllerTest.ControllerTestResponseString res = testBuilder()
            .payload(request)
            .triggerInvalid();
        assertThat(res.body()).isNotNull();
        assertThat(res.body().toString()).contains("\"status\":400,\"error\":\"Bad Request\"");
        assertThat(res.body().toString()).contains("Invalid loc code: 999");
    }

    protected String getValidBureauJwt() {

        final BureauJwtPayload bureauJwtPayload = TestUtils.getJwtPayloadSuperUser("400", "Bureau");

        return mintBureauJwt(bureauJwtPayload);
    }


    private void verifyReportResponse(StandardReportResponse response) {
        assertThat(response).isNotNull();
        super.verifyAndRemoveReportCreated(response);

        assertThat(response.getHeadings()).isNotNull();
        assertThat(response.getHeadings().size()).isEqualTo(5);

        verifyHeadings(response);
        validateTableHeadings(response);

    }

    private void validateTableHeadings(StandardReportResponse response) {
        assertThat(response.getTableData()).isNotNull();
        AbstractReportResponse.TableData<StandardTableData> tableData = response.getTableData();
        assertThat(tableData.getHeadings()).isNotNull();
        assertThat(tableData.getHeadings()).hasSize(8);

        // validate all the headings are present
        AbstractReportResponse.TableData.Heading tableHeading = tableData.getHeadings().get(0);
        assertThat(tableHeading.getId()).isEqualTo("juror_number");
        assertThat(tableHeading.getName()).isEqualTo("Juror Number");
        assertThat(tableHeading.getDataType()).isEqualTo("String");
        tableHeading = tableData.getHeadings().get(1);
        assertThat(tableHeading.getId()).isEqualTo("first_name");
        assertThat(tableHeading.getName()).isEqualTo("First Name");
        assertThat(tableHeading.getDataType()).isEqualTo("String");
        tableHeading = tableData.getHeadings().get(2);
        assertThat(tableHeading.getId()).isEqualTo("last_name");
        assertThat(tableHeading.getName()).isEqualTo("Last Name");
        assertThat(tableHeading.getDataType()).isEqualTo("String");
        tableHeading = tableData.getHeadings().get(3);
        assertThat(tableHeading.getId()).isEqualTo("attendance_date");
        assertThat(tableHeading.getName()).isEqualTo("Attendance Date");
        assertThat(tableHeading.getDataType()).isEqualTo("LocalDate");
        tableHeading = tableData.getHeadings().get(4);
        assertThat(tableHeading.getId()).isEqualTo("day");
        assertThat(tableHeading.getName()).isEqualTo("Day");
        assertThat(tableHeading.getDataType()).isEqualTo("String");
        tableHeading = tableData.getHeadings().get(5);
        assertThat(tableHeading.getId()).isEqualTo("total_paid");
        assertThat(tableHeading.getName()).isEqualTo("Paid");
        assertThat(tableHeading.getDataType()).isEqualTo("BigDecimal");
        tableHeading = tableData.getHeadings().get(6);
        assertThat(tableHeading.getId()).isEqualTo("appearance_pool_number");
        assertThat(tableHeading.getName()).isEqualTo("Pool Number");
        assertThat(tableHeading.getDataType()).isEqualTo("String");
        tableHeading = tableData.getHeadings().get(7);
        assertThat(tableHeading.getId()).isEqualTo("appearance_trial_number");
        assertThat(tableHeading.getName()).isEqualTo("Trial Number");
        assertThat(tableHeading.getDataType()).isEqualTo("String");
    }

    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") // false positive
    private void verifyHeadings(StandardReportResponse response) {
        assertThat(response.getHeadings().containsKey("date_from")).isTrue();
        AbstractReportResponse.DataTypeValue reportHeading = response.getHeadings().get("date_from");
        assertThat(reportHeading).isNotNull();
        assertThat(reportHeading.getDisplayName()).isEqualTo("Date from");
        assertThat(reportHeading.getDataType()).isEqualTo("LocalDate");
        assertThat(reportHeading.getValue()).isEqualTo(DateTimeFormatter.ISO_DATE.format(LocalDate.now()
                                                                                             .withDayOfMonth(1)));

        assertThat(response.getHeadings().containsKey("date_to")).isTrue();
        reportHeading = response.getHeadings().get("date_to");
        assertThat(reportHeading).isNotNull();
        assertThat(reportHeading.getDisplayName()).isEqualTo("Date to");
        assertThat(reportHeading.getDataType()).isEqualTo("LocalDate");
        assertThat(reportHeading.getValue()).isEqualTo(DateTimeFormatter.ISO_DATE.format(LocalDate.now()));

        assertThat(response.getHeadings().containsKey("total")).isTrue();
        reportHeading = response.getHeadings().get("total");
        assertThat(reportHeading).isNotNull();
        assertThat(reportHeading.getDisplayName()).isEqualTo("Total");
        assertThat(reportHeading.getDataType()).isEqualTo("Long");
        // cannot assert exact value as it depends on current date

        assertThat(response.getHeadings().containsKey("time_created")).isTrue();
        AbstractReportResponse.DataTypeValue timeCreated = response.getHeadings().get("time_created");
        assertThat(timeCreated.getDisplayName()).isEqualTo("Time created");
        assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
        assertThat(timeCreated.getValue()).isNotNull();
        LocalDateTime localDateTime = LocalDateTime.parse((String) timeCreated.getValue(),
                                                          DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertThat(localDateTime).isCloseTo(LocalDateTime.now(),
                                            within(10, ChronoUnit.SECONDS));

        assertThat(response.getHeadings().containsKey("court_name")).isTrue();
        reportHeading = response.getHeadings().get("court_name");
        assertThat(reportHeading).isNotNull();
        assertThat(reportHeading.getDisplayName()).isEqualTo("Court Name");
        assertThat(reportHeading.getDataType()).isEqualTo("String");
        assertThat(reportHeading.getValue()).isEqualTo("CHESTER (415)");
    }

}
