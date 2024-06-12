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

@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert"//False positive
})
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
        "/db/JurorExpenseControllerITest_expenseRates.sql",
        "/db/mod/reports/AbstractJurorExpenditureReportReportITestITest_typical.sql"
    })
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(createResponse(
                "£5.00", "£111.97", "£116.97", 6,
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)",
                new GroupedTableData()
                    .add("BACS and cheque approvals",
                        new ReportLinkedMap<String, Object>()
                            .add("2024-05-14", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200956973")
                                    .add("first_name", "Alyce")
                                    .add("last_name", "Almoney")
                                    .add("payment_audit", "F10")
                                    .add("total_loss_of_earnings_approved_sum", 73.0)
                                    .add("total_subsistence_approved_sum", 0.0)
                                    .add("total_smartcard_approved_sum", 0)
                                    .add("total_travel_approved_sum", 10.0)
                                    .add("total_approved_sum", 83.0)))
                            .add("2024-05-15", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200956973")
                                    .add("first_name", "Alyce")
                                    .add("last_name", "Almoney")
                                    .add("payment_audit", "F12")
                                    .add("total_loss_of_earnings_approved_sum", 2.0)
                                    .add("total_subsistence_approved_sum", 5.71)
                                    .add("total_smartcard_approved_sum", 0)
                                    .add("total_travel_approved_sum", 5.63)
                                    .add("total_approved_sum", 13.34),
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200959864")
                                    .add("first_name", "Kristle")
                                    .add("last_name", "Latessa")
                                    .add("payment_audit", "F18")
                                    .add("total_loss_of_earnings_approved_sum", 9.0)
                                    .add("total_subsistence_approved_sum", 11.42)
                                    .add("total_smartcard_approved_sum", -5.34)
                                    .add("total_travel_approved_sum", 0.0)
                                    .add("total_approved_sum", 15.08),
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200959864")
                                    .add("first_name", "Kristle")
                                    .add("last_name", "Latessa")
                                    .add("payment_audit", "F20")
                                    .add("total_loss_of_earnings_approved_sum", 0.0)
                                    .add("total_subsistence_approved_sum", 0.0)
                                    .add("total_smartcard_approved_sum", 0.55)
                                    .add("total_travel_approved_sum", 0.0)
                                    .add("total_approved_sum", 0.55))))
                    .add("Cash",
                        new ReportLinkedMap<String, Object>()
                            .add("2024-05-15", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200956973")
                                    .add("first_name", "Alyce")
                                    .add("last_name", "Almoney")
                                    .add("payment_audit", "F14")
                                    .add("total_loss_of_earnings_approved_sum", 0.0)
                                    .add("total_subsistence_approved_sum", 0.0)
                                    .add("total_smartcard_approved_sum", 0)
                                    .add("total_travel_approved_sum", 3.0)
                                    .add("total_approved_sum", 3.0),
                                new ReportLinkedMap<String, Object>()
                                    .add("juror_number", "200956973")
                                    .add("first_name", "Alyce")
                                    .add("last_name", "Almoney")
                                    .add("payment_audit", "F16")
                                    .add("total_loss_of_earnings_approved_sum", 2.0)
                                    .add("total_subsistence_approved_sum", 0.0)
                                    .add("total_smartcard_approved_sum", 0)
                                    .add("total_travel_approved_sum", 0.0)
                                    .add("total_approved_sum", 2.0))))
            ));
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql"
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
                    .name("CREATED_ON_DATE")
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
                            .id("total_loss_of_earnings_approved_sum")
                            .name("Loss of earnings")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_subsistence_approved_sum")
                            .name("Food and drink")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_smartcard_approved_sum")
                            .name("Smartcard")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_travel_approved_sum")
                            .name("Travel")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_approved_sum")
                            .name("Total")
                            .dataType("BigDecimal")
                            .headings(null).build())).build()
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
                            .id("payment_audit")
                            .name("Payment Audit")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_loss_of_earnings_approved_sum")
                            .name("Loss of earnings")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_subsistence_approved_sum")
                            .name("Food and drink")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_smartcard_approved_sum")
                            .name("Smartcard")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_travel_approved_sum")
                            .name("Travel")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_approved_sum")
                            .name("Total")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build()))
                    .data(groupedTableData)
                    .build())
            .build();
    }
}
