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
import java.util.Map;

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
        "/db/JurorExpenseControllerITest_expenseRates.sql",
        "/db/mod/reports/AbstractJurorExpenditureReportReportITestITest_typical.sql"
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
                            .add("total_loss_of_earnings_approved_sum", 84.0)
                            .add("total_loss_of_earnings_approved_count", 10)
                            .add("total_subsistence_approved_sum", 17.13)
                            .add("total_subsistence_approved_count", 3)
                            .add("total_smartcard_approved_sum", 5.89)
                            .add("total_smartcard_approved_count", 3)
                            .add("total_travel_approved_sum", 15.63)
                            .add("total_travel_approved_count", 5)
                            .add("total_approved_sum", 122.65)))
                    .add("Cash", List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("total_loss_of_earnings_approved_sum", 2.0)
                            .add("total_loss_of_earnings_approved_count", 1)
                            .add("total_subsistence_approved_sum", 0.0)
                            .add("total_subsistence_approved_count", 0)
                            .add("total_smartcard_approved_sum", 0)
                            .add("total_smartcard_approved_count", 0)
                            .add("total_travel_approved_sum", 3.0)
                            .add("total_travel_approved_count", 1)
                            .add("total_approved_sum", 5.0))))
            );
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
            .groupBy(GroupByResponse.builder().name("IS_CASH").build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value(court)
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
            )
            .tableData(
                AbstractReportResponse.TableData.<GroupedTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_loss_of_earnings_approved_sum")
                            .name("Loss of earnings")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_loss_of_earnings_approved_count")
                            .name("Loss of earnings Count")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_subsistence_approved_sum")
                            .name("Food and drink")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_subsistence_approved_count")
                            .name("Food and drink Count")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_smartcard_approved_sum")
                            .name("Smartcard")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_smartcard_approved_count")
                            .name("Smartcard Count")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_travel_approved_sum")
                            .name("Travel")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_travel_approved_count")
                            .name("Travel Count")
                            .dataType("Long")
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
