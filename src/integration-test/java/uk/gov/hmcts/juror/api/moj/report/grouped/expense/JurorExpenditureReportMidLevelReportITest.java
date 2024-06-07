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
        "/db/JurorExpenseControllerITest_expenseRates.sql",
        "/db/mod/reports/AbstractJurorExpenditureReportReportITestITest_typical.sql"
    })
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(createResponse(
                "£5.00", "£111.97", "£116.97",
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)",
                new GroupedTableData()
                    .add("BACS and cheque approvals", List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("created_on_date", "2024-05-14")
                            .add("total_approved_sum", 83.0),
                        new ReportLinkedMap<String, Object>()
                            .add("created_on_date", "2024-05-15")
                            .add("total_approved_sum", 28.97)))
                    .add("Cash", List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("created_on_date", "2024-05-15")
                            .add("total_approved_sum", 5.0)))
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
                "£0.00", "£0.00", "£0.00",
                DEFAULT_FROM_DATE, DEFAULT_TO_DATE, "CHESTER (415)", new GroupedTableData()));
    }


    private GroupedReportResponse createResponse(
        String totalCash, String totalBacsAndCheque, String total,
        LocalDate from, LocalDate to, String court,
        GroupedTableData groupedTableData) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name("IS_CASH").build())
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
                    .build()))
            .tableData(
                AbstractReportResponse.TableData.<GroupedTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("created_on_date")
                            .name("Created On")
                            .dataType("LocalDate")
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
