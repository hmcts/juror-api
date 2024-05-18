package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

class JurorExpenditureReportMidLevelReportITest extends AbstractJurorExpenditureReportReportITest {


    @Autowired
    public JurorExpenditureReportMidLevelReportITest(TestRestTemplate template) {
        super(template, JurorExpenditureReportMidLevelReport.class);
    }


    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/administration/createUsers.sql",
        "/db/mod/reports/FinancialAuditReportsITest_typical.sql"
    })
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(createResponse(
                "£0.00", "£434.28", "£434.28",
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)",
                new GroupedTableData()
                    .add("BACS and cheque approvals",
                        new ReportLinkedMap<String, Object>()
                            .add("2024-03-25", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 74.58)))
                            .add("2024-03-26", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 87.5)))
                            .add("2024-03-27", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 61.16)))
                            .add("2024-03-28", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-03-29", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-01", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-02", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-03", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-04", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-05", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-08", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38))))
            ));
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/administration/createUsers.sql",
        "/db/mod/reports/FinancialAuditReportsITest_typicalCash.sql"
    })
    void positiveCashAndBacs() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(createResponse(
                "£127.22", "£307.06", "£434.28",
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)",
                new GroupedTableData()
                    .add("BACS and cheque approvals",
                        new ReportLinkedMap<String, Object>()
                            .add("2024-03-25", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 32.09)))
                            .add("2024-03-26", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 38.55)))
                            .add("2024-03-27", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 25.38)))
                            .add("2024-03-28", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-03-29", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-01", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-02", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-03", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-04", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-05", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38)))
                            .add("2024-04-08", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 26.38))))
                    .add("Cash",
                        new ReportLinkedMap<String, Object>()
                            .add("2024-03-25", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 42.49)))
                            .add("2024-03-26", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 48.95)))
                            .add("2024-03-27", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("total_paid_sum", 35.78)))))
            );
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql"
    })
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void positiveNotFound() {
        testBuilder()
            .jwt(getCourtJwt("415"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(createResponse(
                "£0.00", "£0.00", "£0.00",
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)", new GroupedTableData()));
    }


    private GroupedReportResponse createResponse(
        String totalCash, String totalBacsAndCheque, String total,
        LocalDate from, LocalDate to, String court,
        GroupedTableData groupedTableData) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name("IS_CASH")
                .nested(GroupByResponse.builder()
                    .name("ATTENDANCE_DATE")
                    .nested(null)
                    .build())
                .build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_cash", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total Cash")
                    .dataType("String")
                    .value(totalCash)
                    .build())
                .add("total_bacs_and_cheque", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total BACS and cheque")
                    .dataType("String")
                    .value(totalBacsAndCheque)
                    .build())
                .add("approved_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Approved From")
                    .dataType("LocalDate")
                    .value(DateTimeFormatter.ISO_DATE.format(from))
                    .build())
                .add("approved_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Approved to")
                    .dataType("LocalDate")
                    .value(DateTimeFormatter.ISO_DATE.format(to))
                    .build())
                .add("overall_total", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Overall total")
                    .dataType("String")
                    .value(total)
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value(court)
                    .build())
            )
            .tableData(
                AbstractReportResponse.TableData.<GroupedTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_paid_sum")
                            .name("Total")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build()))
                    .data(groupedTableData)
                    .build())
            .build();
    }
}
