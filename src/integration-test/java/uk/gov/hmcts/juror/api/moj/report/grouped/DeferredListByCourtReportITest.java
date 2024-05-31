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
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/DeferredListByCourtReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class DeferredListByCourtReportITest extends AbstractGroupedReportControllerITest {

    @Autowired
    public DeferredListByCourtReportITest(TestRestTemplate template) {
        super(template, DeferredListByCourtReport.class);
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
    void positiveTypicalNotFound() {
        testBuilder()
            .jwt(getCourtJwt("475"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(createResponse(0, new GroupedTableData()));
    }

    @Test
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    void positiveTypicalBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponseBureau());
    }

    private GroupedReportResponse getTypicalResponse() {
        return createResponse(4,
            new GroupedTableData()
                .add("CHESTER (415)", List.of(
                    new ReportLinkedMap<String, Object>()
                        .add("deferred_to", "2023-01-06")
                        .add("number_deferred", 1),
                    new ReportLinkedMap<String, Object>()
                        .add("deferred_to", "2023-01-07")
                        .add("number_deferred", 2),
                    new ReportLinkedMap<String, Object>()
                        .add("deferred_to", "2023-01-08")
                        .add("number_deferred", 1)))
                .add("WARRINGTON (462)", List.of(
                    new ReportLinkedMap<String, Object>()
                        .add("deferred_to", "2023-01-09")
                        .add("number_deferred", 1))));
    }

    private GroupedReportResponse getTypicalResponseBureau() {
        return createResponse(4,
            new GroupedTableData()
                .add("CHESTER (415)", List.of(
                    new ReportLinkedMap<String, Object>()
                        .add("deferred_to", "2023-01-05")
                        .add("number_deferred", 2),
                    new ReportLinkedMap<String, Object>()
                        .add("deferred_to", "2023-01-06")
                        .add("number_deferred", 2),
                    new ReportLinkedMap<String, Object>()
                        .add("deferred_to", "2023-01-07")
                        .add("number_deferred", 2),
                    new ReportLinkedMap<String, Object>()
                        .add("deferred_to", "2023-01-08")
                        .add("number_deferred", 1)
                )));
    }

    private GroupedReportResponse createResponse(int count, GroupedTableData groupedTableData) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder()
                .name("COURT_LOCATION_NAME_AND_CODE")
                .build())
            .headings(new ReportLinkedMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_deferred", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total deferred")
                    .dataType("Long")
                    .value(count)
                    .build()))
            .tableData(
                AbstractReportResponse.TableData.<GroupedTableData>builder()
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
                    .data(groupedTableData)
                    .build())
            .build();
    }
}
