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
import uk.gov.hmcts.juror.api.moj.report.TmpSupport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert"//False positive
})
class JurorExpenditureReportHighLevelReportITest extends AbstractJurorExpenditureReportReportITest {


    @Autowired
    public JurorExpenditureReportHighLevelReportITest(TestRestTemplate template) {
        super(template, JurorExpenditureReportHighLevelReport.class);
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
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)",
                new GroupedTableData()
                    .add("BACS and cheque approvals", List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("total_loss_of_earnings_paid_sum", 338.0)
                            .add("total_loss_of_earnings_paid_count", 14)
                            .add("total_subsistence_paid_sum", 35.76)
                            .add("total_subsistence_paid_count", 4)
                            .add("total_smartcard_paid_sum", 2.0)
                            .add("total_smartcard_paid_count", 2)
                            .add("total_travel_paid_sum", 62.52)
                            .add("total_travel_paid_count", 14)
                            .add("total_paid_sum", 434.28)))
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
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)",
                new GroupedTableData()
                    .add("BACS and cheque approvals", List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("total_loss_of_earnings_paid_sum", 242.0)
                            .add("total_loss_of_earnings_paid_count", 11)
                            .add("total_subsistence_paid_sum", 17.88)
                            .add("total_subsistence_paid_count", 2)
                            .add("total_smartcard_paid_sum", 1.0)
                            .add("total_smartcard_paid_count", 1)
                            .add("total_travel_paid_sum", 48.18)
                            .add("total_travel_paid_count", 11)
                            .add("total_paid_sum", 307.06)))
                    .add("Cash", List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("total_loss_of_earnings_paid_sum", 96.0)
                            .add("total_loss_of_earnings_paid_count", 3)
                            .add("total_subsistence_paid_sum", 17.88)
                            .add("total_subsistence_paid_count", 2)
                            .add("total_smartcard_paid_sum", 1.0)
                            .add("total_smartcard_paid_count", 1)
                            .add("total_travel_paid_sum", 14.34)
                            .add("total_travel_paid_count", 3)
                            .add("total_paid_sum", 127.22)))
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
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)", new GroupedTableData()));
    }


    private GroupedReportResponse createResponse(
        LocalDate from, LocalDate to, String court,
        GroupedTableData groupedTableData) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name("IS_CASH")
                .nested(null)
                .build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
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
                            .id("total_loss_of_earnings_paid_sum")
                            .name("Loss of earnings")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_loss_of_earnings_paid_count")
                            .name("Loss of earnings Count")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_subsistence_paid_sum")
                            .name("Food and drink")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_subsistence_paid_count")
                            .name("Food and drink Count")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_smartcard_paid_sum")
                            .name("Smartcard")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_smartcard_paid_count")
                            .name("Smartcard Count")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_travel_paid_sum")
                            .name("Travel")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_travel_paid_count")
                            .name("Travel Count")
                            .dataType("Long")
                            .headings(null)
                            .build(),
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
