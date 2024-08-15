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
    "/db/mod/reports/ExcusedAndDisqualifiedListReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class ExcusedAndDisqualifiedListReportITest extends AbstractGroupedReportControllerITest {

    @Autowired
    public ExcusedAndDisqualifiedListReportITest(TestRestTemplate template) {
        super(template, ExcusedAndDisqualifiedListReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
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
            .assertEquals(
                buildGroupedReportResponse(9, "200000000", new GroupedTableData()
                    .add("Excused", List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "100000000")
                            .add("first_name", "FName 0")
                            .add("last_name", "LName 0")
                            .add("excusal_disqual_code", "I - Ill")
                            .add("excusal_disqual_decision_date", "2024-01-01"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "100000001")
                            .add("first_name", "FName 1")
                            .add("last_name", "LName 1")
                            .add("excusal_disqual_code", "G - Financial hardship")
                            .add("excusal_disqual_decision_date", "2024-01-01"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "100000002")
                            .add("first_name", "FName 2")
                            .add("last_name", "LName 2")
                            .add("excusal_disqual_code", "B - Student")
                            .add("excusal_disqual_decision_date", "2024-01-02"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "100000003")
                            .add("first_name", "FName 3")
                            .add("last_name", "LName 3")
                            .add("excusal_disqual_code", "W - Work related")
                            .add("excusal_disqual_decision_date", "2024-01-02"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "100000004")
                            .add("first_name", "FName 4")
                            .add("last_name", "LName 4")
                            .add("excusal_disqual_code", "CE - CJS employee (unable to transfer")
                            .add("excusal_disqual_decision_date", "2024-01-03")))
                    .add("Disqualified", List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "100000006")
                            .add("first_name", "FName 6")
                            .add("last_name", "LName 6")
                            .add("excusal_disqual_code", "A - Age")
                            .add("excusal_disqual_decision_date", "2024-01-01"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "100000007")
                            .add("first_name", "FName 7")
                            .add("last_name", "LName 7")
                            .add("excusal_disqual_code", "B - Bail")
                            .add("excusal_disqual_decision_date", "2024-01-02"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "100000008")
                            .add("first_name", "FName 8")
                            .add("last_name", "LName 8")
                            .add("excusal_disqual_code", "A - Age")
                            .add("excusal_disqual_decision_date", "2024-01-01"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "100000009")
                            .add("first_name", "FName 9")
                            .add("last_name", "LName 9")
                            .add("excusal_disqual_code", "E - Electronic Police Check Failure")
                            .add("excusal_disqual_decision_date", "2024-01-03"))))
            );
    }

    @Test
    void positiveNoData() {
        StandardReportRequest request = getValidPayload();
        request.setPoolNumber("200000002");
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildGroupedReportResponse(0, "200000002", new GroupedTableData()));
    }

    @Test
    void invalidUnauthorized() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this pool");
    }


    private GroupedReportResponse buildGroupedReportResponse(int count, String poolNumber, GroupedTableData data) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name("EXCUSAL_DISQUAL_TYPE").nested(null).build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("service_start_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Service Start Date")
                    .dataType("LocalDate")
                    .value("2023-01-05")
                    .build())
                .add("total_excused_and_disqualified", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total excused and disqualified")
                    .dataType("Long")
                    .value(count)
                    .build())
                .add("pool_number", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Number")
                    .dataType("String")
                    .value(poolNumber)
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build())
                .add("pool_type", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Type")
                    .dataType("String")
                    .value("CROWN COURT")
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
                            .id("excusal_disqual_code")
                            .name("Reason for excusal or disqualification")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("excusal_disqual_decision_date")
                            .name("Decision date")
                            .dataType("LocalDate")
                            .headings(null)
                            .build()))
                    .data(data)
                    .build())
            .build();
    }
}
