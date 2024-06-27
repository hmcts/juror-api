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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/PaymentStatusReportITest_typical.sql"
})
@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert"//False positive
})
class PaymentStatusReportITest extends AbstractGroupedReportControllerITest {

    @Autowired
    public PaymentStatusReportITest(TestRestTemplate template) {
        super(template, PaymentStatusReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder().build());
    }

    @Test
    void positiveTypical() {
        testBuilder()
            .triggerInvalid()
                .printResponse()
            .printResponseBodyAsJson();


        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(GroupedReportResponse.builder()
                .groupBy(GroupByResponse.builder().name("EXTRACTED").nested(null).build())
                .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                    .add("court_name", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Court Name")
                        .dataType("String")
                        .value("CHESTER (415)")
                        .build()))
                .tableData(
                    AbstractReportResponse.TableData.<GroupedTableData>builder()
                        .headings(List.of(
                            StandardReportResponse.TableData.Heading.builder()
                                .id("creation_date")
                                .name("Date Approved")
                                .dataType("LocalDate")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("total_amount")
                                .name("Amount")
                                .dataType("BigDecimal")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("payments")
                                .name("Payments")
                                .dataType("Long")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("consolidated_file_reference")
                                .name("Consolidated file reference")
                                .dataType("Long")
                                .headings(null)
                                .build()))
                        .data(new GroupedTableData()
                            .add("Sent for payment", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("creation_date",
                                        DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(10)))
                                    .add("total_amount", 11.17)
                                    .add("payments", 1)
                                    .add("consolidated_file_reference", "File Name 1"),
                                new ReportLinkedMap<String, Object>()
                                    .add("creation_date",
                                        DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(10)))
                                    .add("total_amount", 15.13)
                                    .add("payments", 1)
                                    .add("consolidated_file_reference", "File Name 2"),
                                new ReportLinkedMap<String, Object>()
                                    .add("creation_date",
                                        DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(9)))
                                    .add("total_amount", 13.15)
                                    .add("payments", 1)
                                    .add("consolidated_file_reference", "File Name 1"),
                                new ReportLinkedMap<String, Object>()
                                    .add("creation_date",
                                        DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(8)))
                                    .add("total_amount", 12.16)
                                    .add("payments", 1)
                                    .add("consolidated_file_reference", "File Name 1"),
                                new ReportLinkedMap<String, Object>()
                                    .add("creation_date",
                                        DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(8)))
                                    .add("total_amount", 14.14)
                                    .add("payments", 1)
                                    .add("consolidated_file_reference", "File Name 2")))
                            .add("Approved but not yet sent for payment", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("creation_date",
                                        DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(10)))
                                    .add("total_amount", 39.36)
                                    .add("payments", 3),
                                new ReportLinkedMap<String, Object>()
                                    .add("creation_date",
                                        DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(9)))
                                    .add("total_amount", 48.49)
                                    .add("payments", 3))))
                        .build())
                .build());
    }

    @Test
    void negativeUnauthorisedBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }
}
