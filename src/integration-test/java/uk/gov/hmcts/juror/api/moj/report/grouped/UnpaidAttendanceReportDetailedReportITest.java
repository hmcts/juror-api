package uk.gov.hmcts.juror.api.moj.report.grouped;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/administration/createJudges.sql",
    "/db/administration/createCourts.sql",
    "/db/mod/reports/UnpaidAttendanceReportDetailedITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class UnpaidAttendanceReportDetailedReportITest extends AbstractGroupedReportControllerITest {

    @Autowired
    public UnpaidAttendanceReportDetailedReportITest(TestRestTemplate template) {
        super(template, UnpaidAttendanceReportDetailedReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2023, 1, 1))
            .toDate(LocalDate.of(2023, 1, 3))
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
    void positiveTypicalCourtJustTrial() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getJustTrialResponse());
    }


    @Test
    void positiveTypicalCourtJustPool() {
        testBuilder()
            .jwt(getCourtJwt("413"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getJustPoolResponse());
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void positiveNotFound() {
        testBuilder()
            .jwt(getCourtJwt("416"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(createResponse(0, "LEWES SITTING AT CHICHESTER (416)", new GroupedTableData()));
    }

    @Test
    void negativeTypicalIsBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private GroupedReportResponse getTypicalResponse() {
        return createResponse(5, "CHESTER (415)",
            new GroupedTableData()
                .add("Pool 415230101",
                    new ReportLinkedMap<String, Object>()
                        .add("2023-01-02", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "415000001")
                                .add("first_name", "John0")
                                .add("last_name", "Smith0")
                                .add("audit_number", "P1234")
                                .add("attendance_type", "FULL_DAY")
                                .add("expense_status", "Draft"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "415000002")
                                .add("first_name", "John1")
                                .add("last_name", "Smith1")
                                .add("audit_number", "P1234")
                                .add("attendance_type", "FULL_DAY")
                                .add("expense_status", "For re-approval"))))
                .add("Pool 415230103",
                    new ReportLinkedMap<String, Object>()
                        .add("2023-01-02", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "415000003")
                                .add("first_name", "John2")
                                .add("last_name", "Smith2")
                                .add("audit_number", "P1234")
                                .add("attendance_type", "FULL_DAY")
                                .add("expense_status", "For re-approval"))))
                .add("Trial T100000002",
                    new ReportLinkedMap<String, Object>()
                        .add("2023-01-03", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "415000001")
                                .add("first_name", "John0")
                                .add("last_name", "Smith0")
                                .add("audit_number", "J1234")
                                .add("attendance_type", "NON_ATTENDANCE")
                                .add("expense_status", "For approval"))))
                .add("Trial T100000003",
                    new ReportLinkedMap<String, Object>()
                        .add("2023-01-03", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "415000002")
                                .add("first_name", "John1")
                                .add("last_name", "Smith1")
                                .add("audit_number", "J1231")
                                .add("attendance_type", "NON_ATTENDANCE")
                                .add("expense_status", "For approval"))))
        );
    }

    private GroupedReportResponse getJustTrialResponse() {
        return createResponse(4, "Chelmsford  (414)",
            new GroupedTableData()
                .add("Trial T100000004",
                    new ReportLinkedMap<String, Object>()
                        .add("2023-01-01", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "414000001")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3")
                                .add("audit_number", "J1231")
                                .add("attendance_type", "FULL_DAY")
                                .add("expense_status", "For approval"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "414000002")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4")
                                .add("audit_number", "J1231")
                                .add("attendance_type", "HALF_DAY_LONG_TRIAL")
                                .add("expense_status", "For approval")))
                        .add("2023-01-02", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "414000001")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3")
                                .add("audit_number", "J1235")
                                .add("attendance_type", "FULL_DAY")
                                .add("expense_status", "For approval"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "414000002")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4")
                                .add("audit_number", "J1235")
                                .add("attendance_type", "FULL_DAY")
                                .add("expense_status", "For approval"))))
        );
    }

    private GroupedReportResponse getJustPoolResponse() {
        return createResponse(4, "The Central Criminal Court (413)",
            new GroupedTableData()
                .add("Pool 413230101",
                    new ReportLinkedMap<String, Object>()
                        .add("2023-01-01", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "413000001")
                                .add("first_name", "John5")
                                .add("last_name", "Smith5")
                                .add("audit_number", "P1231")
                                .add("attendance_type", "NON_ATTENDANCE_LONG_TRIAL")
                                .add("expense_status", "For approval"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "413000002")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6")
                                .add("audit_number", "P1231")
                                .add("attendance_type", "FULL_DAY")
                                .add("expense_status", "For approval")))
                        .add("2023-01-02", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "413000001")
                                .add("first_name", "John5")
                                .add("last_name", "Smith5")
                                .add("audit_number", "P1235")
                                .add("attendance_type", "FULL_DAY")
                                .add("expense_status", "For approval"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "413000002")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6")
                                .add("audit_number", "P1235")
                                .add("attendance_type", "FULL_DAY")
                                .add("expense_status", "For approval"))))
        );
    }

    private GroupedReportResponse createResponse(int count, String courtName, GroupedTableData groupedTableData) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name("TRIAL_NUMBER_OR_POOL_NUMBER").nested(null).build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_unpaid_attendances", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total unpaid attendances")
                    .dataType("Long")
                    .value(count)
                    .build())
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2023-01-03")
                    .build())
                .add("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2023-01-01")
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value(courtName)
                    .build()))
            .tableData(
                AbstractReportResponse.TableData.<GroupedTableData>builder()
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
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("audit_number")
                            .name("Audit number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("attendance_type")
                            .name("Attendance Type")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("expense_status")
                            .name("Expense Status")
                            .dataType("String")
                            .headings(null)
                            .build()))
                    .data(groupedTableData)
                    .build())
            .build();
    }
}
