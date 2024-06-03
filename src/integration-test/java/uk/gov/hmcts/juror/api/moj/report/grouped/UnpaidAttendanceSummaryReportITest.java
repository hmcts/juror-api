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
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/UnpaidAttendanceSummaryReportITest_typical.sql"
})
class UnpaidAttendanceSummaryReportITest extends AbstractGroupedReportControllerITest {
    @Autowired
    public UnpaidAttendanceSummaryReportITest(TestRestTemplate template) {
        super(template, UnpaidAttendanceSummaryReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2024, 10, 9))
            .toDate(LocalDate.of(2024, 10, 16))
            .build());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void positiveTypicalWiderDateRange() {
        StandardReportRequest request = getValidPayload();
        request.setFromDate(LocalDate.of(2024, 1, 1));
        request.setToDate(LocalDate.of(2024, 12, 31));
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponseWiderDateRange());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void positiveTypicalNoData() {
        StandardReportRequest request = getValidPayload();
        request.setFromDate(LocalDate.of(2024, 12, 9));
        request.setToDate(LocalDate.of(2024, 12, 16));
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponseNoData());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void negativeInvalidPayloadMissingDate() {
        StandardReportRequest request = getValidPayload();
        request.setToDate(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .assertInvalidPathParam("toDate: must not be null");
    }

    private GroupedReportResponse getTypicalResponse() {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name(DataType.ATTENDANCE_DATE.name()).build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_unpaid_attendances", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total Unpaid Attendances")
                    .dataType("Long")
                    .value(6)
                    .build())
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2024-10-16")
                    .build())
                .add("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2024-10-09")
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
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
                            .build()))
                    .data(new GroupedTableData()
                        .add("2024-10-10", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500023")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500024")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500026")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6")))
                        .add("2024-10-11", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500023")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500024")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500026")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6")
                            )))
                    .build())
            .build();
    }

    private GroupedReportResponse getTypicalResponseWiderDateRange() {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name(DataType.ATTENDANCE_DATE.name()).build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_unpaid_attendances", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total Unpaid Attendances")
                    .dataType("Long")
                    .value(12)
                    .build())
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2024-12-31")
                    .build())
                .add("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2024-01-01")
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
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
                            .build()))
                    .data(new GroupedTableData()
                        .add("2024-10-10", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500023")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500024")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500026")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6")))
                        .add("2024-10-11", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500023")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500024")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500026")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6")))
                        .add("2024-11-10", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500023")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500024")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500026")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6")))
                        .add("2024-11-11", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500023")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500024")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500026")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6"))))
                    .build())
            .build();
    }

    private GroupedReportResponse getTypicalResponseNoData() {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name(DataType.ATTENDANCE_DATE.name()).build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_unpaid_attendances", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total Unpaid Attendances")
                    .dataType("Long")
                    .value(0)
                    .build())
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2024-12-16")
                    .build())
                .add("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2024-12-09")
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
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
                            .build()))
                    .data(new GroupedTableData())
                    .build())
            .build();
    }


}
