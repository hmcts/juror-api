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
import java.util.List;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/PoolStatisticsReportITest_typical.sql"
})
@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert"//False positive
})
class PoolStatisticsReportITest extends AbstractGroupedReportControllerITest {

    @Autowired
    public PoolStatisticsReportITest(TestRestTemplate template) {
        super(template, PoolStatisticsReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getBureauJwt();
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2023, 1, 1))
            .toDate(LocalDate.of(2023, 1, 31))
            .build());
    }

    @Test
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildGroupedReportResponse(
                new GroupedTableData()
                    .add("2023-01-01",
                        new ReportLinkedMap<String, Object>()
                            .add("200000000", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Awaiting Info")
                                    .add("juror_pool_count", 2),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Completed")
                                    .add("juror_pool_count", 2),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "FailedToAttend")
                                    .add("juror_pool_count", 2),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Juror")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Panel")
                                    .add("juror_pool_count", 3),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Responded")
                                    .add("juror_pool_count", 4),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Summoned")
                                    .add("juror_pool_count", 3),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Transferred")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Undeliverable")
                                    .add("juror_pool_count", 1))))
                    .add("2023-01-07",
                        new ReportLinkedMap<String, Object>()
                            .add("200000001", List.of(
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
                                    .add("status", "Juror")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Reassigned")
                                    .add("juror_pool_count", 1)))
                            .add("200000004", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Responded")
                                    .add("juror_pool_count", 2))))
                    .add("2023-01-30",
                        new ReportLinkedMap<String, Object>()
                            .add("200000002", List.of(
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
                                    .add("status", "Reassigned")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Transferred")
                                    .add("juror_pool_count", 1),
                                new ReportLinkedMap<String, Object>()
                                    .add("status", "Undeliverable")
                                    .add("juror_pool_count", 1)
                            )))));
    }

    @Test
    void positiveNotFound() {
        testBuilder()
            .payload(addReportType(StandardReportRequest.builder()
                .fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 1, 31))
                .build()))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildGroupedReportResponse(new GroupedTableData()));
    }

    @Test
    void negativeUnauthorisedCourtUser() {
        testBuilder()
            .jwt(getCourtJwt("415"))
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private GroupedReportResponse buildGroupedReportResponse(GroupedTableData tableData) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name("POOL_RETURN_DATE_BY_JP")
                .nested(GroupByResponse.builder().name("POOL_NUMBER_BY_JP").build())
                .build())
            .headings(new ReportHashMap<>())
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
                            .name("Count")
                            .dataType("Long")
                            .headings(null)
                            .build()))
                    .data(tableData)
                    .build())
            .build();
    }
}
