package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/PersonAttendingSummaryReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class PersonAttendingSummaryReportITest extends AbstractStandardReportControllerITest {
    @Autowired
    public PersonAttendingSummaryReportITest(TestRestTemplate template) {
        super(template, PersonAttendingSummaryReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .date(LocalDate.now().plusDays(1))
            .includeSummoned(false)
            .includePanelMembers(false)
            .build());
    }

    @Test
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    void positiveIncludeSummons() {
        StandardReportRequest request = getValidPayload();
        request.setIncludeSummoned(true);
        request.setDate(LocalDate.now().plusDays(2));
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getIncludeSummonsResponse());
    }

    @Test
    void negativeNoRecordsFound() {
        StandardReportRequest request = getValidPayload();
        request.setDate(LocalDate.now().minusDays(100));
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getNoRecordsResponse());
    }

    @Test
    void negativeInvalidPayloadAttendanceDateMissing() {
        StandardReportRequest request = getValidPayload();
        request.setDate(null);
        testBuilder()
            .payload(request)
            .triggerInvalid()
            .assertInvalidPathParam("date: must not be null");
    }

    @Test
    void negativeUnauthorisedBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private StandardReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("attendance_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Attendance date")
                    .dataType("LocalDate")
                    .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now().plusDays(1)))
                    .build())
                .add("total_due", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total due to attend")
                    .dataType("Integer")
                    .value(3)
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build()))
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("juror_number")
                            .name("Juror Number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("first_name")
                            .name("First Name")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("last_name")
                            .name("Last Name")
                            .dataType("String")
                            .headings(null)
                            .build()))
                    .data(StandardTableData.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500011")
                            .add("first_name", "FNAMEONEONE")
                            .add("last_name", "LNAMEONEONE"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500003")
                            .add("first_name", "FNAMETHREE")
                            .add("last_name", "LNAMETHREE"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500021")
                            .add("first_name", "FNAMETWOONE")
                            .add("last_name", "LNAMETWOONE")))
                    .build())
            .build();
    }

    private StandardReportResponse getIncludeSummonsResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                          .add("attendance_date", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Attendance date")
                              .dataType("LocalDate")
                              .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now().plusDays(2)))
                              .build())
                          .add("total_due", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Total due to attend")
                              .dataType("Integer")
                              .value(2)
                              .build())
                          .add("court_name", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Court Name")
                              .dataType("String")
                              .value("CHESTER (415)")
                              .build()))
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("juror_number")
                            .name("Juror Number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("first_name")
                            .name("First Name")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("last_name")
                            .name("Last Name")
                            .dataType("String")
                            .headings(null)
                            .build()))
                    .data(StandardTableData.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500004")
                            .add("first_name", "FNAMEFOUR")
                            .add("last_name", "LNAMEFOUR"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500007")
                            .add("first_name", "FNAMESEVEN")
                            .add("last_name", "LNAMESEVEN")))
                    .build())
            .build();
    }

    private StandardReportResponse getNoRecordsResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("attendance_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Attendance date")
                    .dataType("LocalDate")
                    .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(100)))
                    .build())
                .add("total_due", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total due to attend")
                    .dataType("Integer")
                    .value(0)
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build()))
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("juror_number")
                            .name("Juror Number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("first_name")
                            .name("First Name")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("last_name")
                            .name("Last Name")
                            .dataType("String")
                            .headings(null)
                            .build()))
                    .data(StandardTableData.of())
                    .build())
            .build();
    }
}
