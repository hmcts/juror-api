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

import java.util.List;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/PoolStatusAndGraphReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class PoolStatusAndGraphReportITest extends AbstractGroupedReportControllerITest {
    @Autowired
    public PoolStatusAndGraphReportITest(TestRestTemplate template) {
        super(template, PoolStatusAndGraphReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getBureauJwt();
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .poolNumber("200000000")
            .build());
    }


    @Test
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(GroupedReportResponse.builder()
                .groupBy(GroupByResponse.builder().name("IS_ACTIVE").nested(null).build())
                .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                    .add("originally_requested_by_court", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Originally requested by court")
                        .dataType("Long")
                        .value(5)
                        .build())
                    .add("total_pool_members", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Total pool members")
                        .dataType("Long")
                        .value(30)
                        .build())
                    .add("pool_number", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Pool number")
                        .dataType("String")
                        .value("200000000")
                        .build()))
                .tableData(
                    AbstractReportResponse.TableData.<GroupedTableData>builder()
                        .headings(List.of(
                            StandardReportResponse.TableData.Heading.builder()
                                .id("status")
                                .name("Status")
                                .dataType("String")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("juror_pool_count")
                                .name("Juror Pool Count")
                                .dataType("Long")
                                .headings(null)
                                .build()))
                        .data(new GroupedTableData()
                            .add("Inactive pool members", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Awaiting Info")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Completed")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Deferred")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Disqualified")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Excused")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "FailedToAttend")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Juror")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Panel")
                                    .add("juror_pool_count", 2),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Reassigned")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Responded")
                                    .add("juror_pool_count", 2),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Summoned")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Transferred")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Undeliverable")
                                    .add("juror_pool_count", 1)))
                            .add("Active pool members", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Awaiting Info")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Completed")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Deferred")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Disqualified")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Excused")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "FailedToAttend")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Juror")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Panel")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Reassigned")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Responded")
                                    .add("juror_pool_count", 2),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Summoned")
                                    .add("juror_pool_count", 2),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Transferred")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Undeliverable")
                                    .add("juror_pool_count", 1))))
                        .build())
                .build());
    }

    @Test
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getCourtJwt("415"))
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }
}
