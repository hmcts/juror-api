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

class JurorExpenditureReportLowLevelReportITest extends AbstractJurorExpenditureReportReportITest {


    @Autowired
    public JurorExpenditureReportLowLevelReportITest(TestRestTemplate template) {
        super(template, JurorExpenditureReportLowLevelReport.class);
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
                "£0.00", "£434.28", "£434.28", 2,
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)",
                new GroupedTableData()
                    .add("BACS and cheque approvals",
                        new ReportLinkedMap<String, Object>()
                            .add("2024-03-25", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 5.71)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 32.09),
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F96")
                                    .add("total_loss_of_earnings_paid", 32.0)
                                    .add("total_subsistence_paid", 5.71)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.78)
                                    .add("total_paid", 42.49)))
                            .add("2024-03-26", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 12.17)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 38.55),
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F96")
                                    .add("total_loss_of_earnings_paid", 32.0)
                                    .add("total_subsistence_paid", 12.17)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.78)
                                    .add("total_paid", 48.95)))
                            .add("2024-03-27", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0.0)
                                    .add("total_smartcard_paid", 1.0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 25.38),
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F96")
                                    .add("total_loss_of_earnings_paid", 32.0)
                                    .add("total_subsistence_paid", 0.0)
                                    .add("total_smartcard_paid", 1.0)
                                    .add("total_travel_paid", 4.78)
                                    .add("total_paid", 35.78)))
                            .add("2024-03-28", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-03-29", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-01", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-02", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-03", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-04", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-05", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0.0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-08", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0.0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38))))
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
                "£127.22", "£307.06", "£434.28", 2,
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)",
                new GroupedTableData()
                    .add("BACS and cheque approvals",
                        new ReportLinkedMap<String, Object>()
                            .add("2024-03-25", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 5.71)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 32.09)))
                            .add("2024-03-26", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 12.17)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 38.55)))
                            .add("2024-03-27", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0.0)
                                    .add("total_smartcard_paid", 1.0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 25.38)))
                            .add("2024-03-28", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-03-29", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-01", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-02", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-03", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-04", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-05", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0.0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38)))
                            .add("2024-04-08", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F94")
                                    .add("total_loss_of_earnings_paid", 22.0)
                                    .add("total_subsistence_paid", 0.0)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.38)
                                    .add("total_paid", 26.38))))
                    .add("Cash",
                        new ReportLinkedMap<String, Object>()
                            .add("2024-03-25", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F96")
                                    .add("total_loss_of_earnings_paid", 32.0)
                                    .add("total_subsistence_paid", 5.71)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.78)
                                    .add("total_paid", 42.49)))
                            .add("2024-03-26", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F96")
                                    .add("total_loss_of_earnings_paid", 32.0)
                                    .add("total_subsistence_paid", 12.17)
                                    .add("total_smartcard_paid", 0)
                                    .add("total_travel_paid", 4.78)
                                    .add("total_paid", 48.95)))
                            .add("2024-03-27", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200106974")
                                    .add("first_name", "Josh")
                                    .add("last_name", "Gallus")
                                    .add("payment_audit", "F96")
                                    .add("total_loss_of_earnings_paid", 32.0)
                                    .add("total_subsistence_paid", 0.0)
                                    .add("total_smartcard_paid", 1.0)
                                    .add("total_travel_paid", 4.78)
                                    .add("total_paid", 35.78))))
            ));
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
                "£0.00", "£0.00", "£0.00", 0,
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)", new GroupedTableData()));
    }

    private GroupedReportResponse createResponse(
        String totalCash, String totalBacsAndCheque, String total,
        int totalApprovals, LocalDate from, LocalDate to, String court,
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
                .add("total_approvals", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total approvals")
                    .dataType("Long")
                    .value(totalApprovals)
                    .build())
            )
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
                            .name("First name")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("last_name")
                            .name("Last name")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("payment_audit")
                            .name("Payment Audit")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_loss_of_earnings_paid")
                            .name("Loss of earnings")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_subsistence_paid")
                            .name("Food and drink")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_smartcard_paid")
                            .name("Smartcard")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_travel_paid")
                            .name("Travel")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_paid")
                            .name("Total")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build()))
                    .data(groupedTableData)
                    .build())
            .build();
    }
}
