package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/DeferredListByDateReportITest_typical.sql"
})
class DeferredListByDateReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    public DeferredListByDateReportITest(TestRestTemplate template) {
        super(template, DeferredListByDateReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .build());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponseCourt());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
    void positiveTypicalBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponseBureau());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
    void positiveNotFound() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(StandardReportResponse.builder()
                .headings(new ReportLinkedMap<String, StandardReportResponse.DataTypeValue>()
                    .add("total_deferred", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Total deferred")
                        .dataType("Long")
                        .value(0)
                        .build()))
                .tableData(
                    StandardReportResponse.TableData.<StandardTableData>builder()
                        .headings(List.of(
                            StandardReportResponse.TableData.Heading.builder()
                                .id("deferred_to")
                                .name("Deferred to")
                                .dataType("LocalDate")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("number_deferred")
                                .name("Number Deferred")
                                .dataType("Long")
                                .headings(null)
                                .build()))
                        .data(StandardTableData.of())
                        .build())
                .build());
    }

    private StandardReportResponse getTypicalResponseBureau() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_deferred", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total deferred")
                    .dataType("Long")
                    .value(7)
                    .build()))
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("deferred_to")
                            .name("Deferred to")
                            .dataType("LocalDate")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("number_deferred")
                            .name("Number Deferred")
                            .dataType("Long")
                            .headings(null)
                            .build()))
                    .data(StandardTableData.of(
                        new ReportLinkedMap<String, Object>()
                            .add("deferred_to", "3023-01-05")
                            .add("number_deferred", 2),
                        new ReportLinkedMap<String, Object>()
                            .add("deferred_to", "3023-01-06")
                            .add("number_deferred", 2),
                        new ReportLinkedMap<String, Object>()
                            .add("deferred_to", "3023-01-07")
                            .add("number_deferred", 2),
                        new ReportLinkedMap<String, Object>()
                            .add("deferred_to", "3023-01-08")
                            .add("number_deferred", 1)))
                    .build())
            .build();
    }

    private StandardReportResponse getTypicalResponseCourt() {
        return StandardReportResponse.builder()
            .headings(new ReportLinkedMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_deferred", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total deferred")
                    .dataType("Long")
                    .value(4)
                    .build()))
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("deferred_to")
                            .name("Deferred to")
                            .dataType("LocalDate")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("number_deferred")
                            .name("Number Deferred")
                            .dataType("Long")
                            .headings(null)
                            .build()))
                    .data(StandardTableData.of(
                        new ReportLinkedMap<String, Object>()
                            .add("deferred_to", "3023-01-06")
                            .add("number_deferred", 1),
                        new ReportLinkedMap<String, Object>()
                            .add("deferred_to", "3023-01-07")
                            .add("number_deferred", 2),
                        new ReportLinkedMap<String, Object>()
                            .add("deferred_to", "3023-01-08")
                            .add("number_deferred", 1)))
                    .build())
            .build();
    }
}
