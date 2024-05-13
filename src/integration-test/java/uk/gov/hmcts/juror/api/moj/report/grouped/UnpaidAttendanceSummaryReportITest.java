package uk.gov.hmcts.juror.api.moj.report.grouped;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/PostponedListByDateReportITest_typical.sql"
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
            .fromDate(LocalDate.of(2023, 1, 1))
            .toDate(LocalDate.of(2023, 1, 2))
            .build());
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void positiveNotFound() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(GroupedReportResponse.builder()
                .groupBy(DataType.POOL_NUMBER)
                .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                    .add("total_unpaid_attendances", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Total Unpaid Attendance")
                        .dataType("Long")
                        .value(0)
                        .build())
                    .add("date_to", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Date To")
                        .dataType("LocalDate")
                        .value("2023-01-02")
                        .build())
                    .add("date_from", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Date From")
                        .dataType("LocalDate")
                        .value("2023-01-01")
                        .build())
                    .add("court_name", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Court Name")
                        .dataType("String")
                        .value("Chester  (415)")
                        .build()))
                .tableData(
                    AbstractReportResponse.TableData.<Map<String, List<LinkedHashMap<String, Object>>>>builder()
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
                        .data(new ReportLinkedMap<>())
                        .build())
                .build());
    }

    private GroupedReportResponse getTypicalResponse() {
        return GroupedReportResponse.builder()
            .groupBy(DataType.POOL_NUMBER)
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_unpaid_attendances", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total Unpaid Attendance")
                    .dataType("Long")
                    .value(0)
                    .build())
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2023-01-02")
                    .build())
                .add("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2023-01-01")
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER  (415)")
                    .build()))
            .tableData(
                AbstractReportResponse.TableData.<Map<String, List<LinkedHashMap<String, Object>>>>builder()
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
                    .data(new ReportLinkedMap<>())
                    .build())
            .build();
    }


}
