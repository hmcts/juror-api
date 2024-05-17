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
@SuppressWarnings("PMD.LawOfDemeter")
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
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void positiveTypicalDifferentDates() {
        StandardReportRequest request = getValidPayload();
        request.setFromDate(LocalDate.of(2024, 11, 9));
        request.setToDate(LocalDate.of(2024, 11, 16));
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponseChangedDates());
    }

    @Test
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
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    @Test
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
            .groupBy(GroupByResponse.builder().name(DataType.POOL_NUMBER_BY_APPEARANCE.name()).build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_unpaid_attendances", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total Unpaid Attendances")
                    .dataType("Long")
                    .value(3)
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
                        .add("415230103", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500023")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500024")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4")))
                        .add("415230104", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500026")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6"))))
                    .build())
            .build();
    }

    private GroupedReportResponse getTypicalResponseChangedDates() {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name(DataType.POOL_NUMBER_BY_APPEARANCE.name()).build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_unpaid_attendances", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total Unpaid Attendances")
                    .dataType("Long")
                    .value(3)
                    .build())
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2024-11-16")
                    .build())
                .add("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2024-11-09")
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
                        .add("415230103", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500023")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500024")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4")))
                        .add("415230104", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500026")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6"))))
                    .build())
            .build();
    }

    private GroupedReportResponse getTypicalResponseNoData() {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name(DataType.POOL_NUMBER_BY_APPEARANCE.name()).build())
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
