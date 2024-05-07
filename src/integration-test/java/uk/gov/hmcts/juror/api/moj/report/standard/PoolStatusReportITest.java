package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.util.LinkedHashMap;
import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/reports/poolstatus.sql"
})
@SuppressWarnings({"PMD.LawOfDemeter"})
class PoolStatusReportITest extends AbstractStandardReportControllerITest {
    @Autowired
    PoolStatusReportITest(TestRestTemplate template) {
        super(template, PoolStatusReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getBureauJwt();
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .reportType("PoolStatusReport")
            .poolNumber("416220901")
            .build());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void positiveTypicalBureau() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    //False positive
    void negativeInvalidPayload() {
        StandardReportRequest request = getValidPayload();
        request.setPoolNumber(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .assertInvalidPathParam("poolNumber: must not be null");
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    //False positive
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    StandardReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("pool_number", AbstractReportResponse.DataTypeValue.builder()
                    .value("416220901")
                    .dataType("String")
                    .displayName("Pool number")
                    .build())
                .add("total_pool_members", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Total pool members")
                    .dataType("Long")
                    .value(10)
                    .build())
                .add("total_requested_by_court", AbstractReportResponse.DataTypeValue.builder()
                    .dataType("Long")
                    .value(5)
                    .displayName("Originally requested by court")
                    .build())
            )
            .tableData(StandardReportResponse.TableData.<List<LinkedHashMap<String, Object>>>builder()
                .headings(List.of(StandardReportResponse.TableData.Heading.builder()
                        .id("summons_total")
                        .name("Summoned")
                        .dataType("Integer")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("responded_total")
                        .name("Responded")
                        .dataType("Integer")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("panel_total")
                        .name("Panel")
                        .dataType("Integer")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("juror_total")
                        .name("Juror")
                        .dataType("Integer")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("excused_total")
                        .name("Excused")
                        .dataType("Integer")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("disqualified_total")
                        .name("Disqualified")
                        .dataType("Integer")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("deferred_total")
                        .name("Deferred")
                        .dataType("Integer")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("reassigned_total")
                        .name("Reassigned")
                        .dataType("Integer")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("undeliverable_total")
                        .name("Undeliverable")
                        .dataType("Integer")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("transferred_total")
                        .name("Transferred")
                        .dataType("Integer")
                        .build()))
                .data(
                    List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("summons_total", 1)
                            .add("responded_total", 2)
                            .add("panel_total", 2)
                            .add("juror_total", 0)
                            .add("excused_total", 0)
                            .add("disqualified_total", 3)
                            .add("deferred_total", 0)
                            .add("reassigned_total", 0)
                            .add("undeliverable_total", 2)
                            .add("transferred_total", 0)
                    )
                )
                .build())
            .build();
    }
}
