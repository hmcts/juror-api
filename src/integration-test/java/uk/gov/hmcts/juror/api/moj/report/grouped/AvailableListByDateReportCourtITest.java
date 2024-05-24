package uk.gov.hmcts.juror.api.moj.report.grouped;

import lombok.SneakyThrows;
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

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_03;
import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_04;
import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_05;
import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_06;
import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_08;
import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_09;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/AvailableListReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class AvailableListByDateReportCourtITest extends AbstractGroupedReportControllerITest {

    @Autowired
    public AvailableListByDateReportCourtITest(TestRestTemplate template) {
        super(template, AvailableListByDateReportCourt.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .date(LocalDate.of(2024, 1, 1))
            .includePanelMembers(false)
            .includeJurorsOnCall(false)
            .respondedJurorsOnly(false)
            .build());
    }

    @Test
    @SneakyThrows
    void positiveAllFalse() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(new GroupedTableData()
                    .add("415230102,CROWN COURT",
                        List.of(JUROR_08, JUROR_09))
                    .add("415230103,CROWN COURT",
                        List.of(JUROR_03)),
                3));
    }

    @Test
    void positiveIncludePanelMembers() {
        StandardReportRequest request = getValidPayload();
        request.setIncludePanelMembers(true);
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(new GroupedTableData()
                    .add("415230102,CROWN COURT",
                        List.of(JUROR_08, JUROR_09))
                    .add("415230103,CROWN COURT",
                        List.of(JUROR_03, JUROR_05)),
                4));
    }

    @Test
    void positiveIncludeOnCall() {
        StandardReportRequest request = getValidPayload();
        request.setIncludeJurorsOnCall(true);
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(new GroupedTableData()
                    .add("415230102,CROWN COURT",
                        List.of(JUROR_08, JUROR_09))
                    .add("415230103,CROWN COURT",
                        List.of(JUROR_03, JUROR_04)),
                4));
    }

    @Test
    void positiveRespondedOnly() {
        StandardReportRequest request = getValidPayload();
        request.setRespondedJurorsOnly(true);
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(new GroupedTableData()
                    .add("415230102,CROWN COURT",
                        List.of(JUROR_08)),
                1));
    }

    @Test
    void positiveIncludeJurorsOnCallAndPanelMembers() {
        StandardReportRequest request = getValidPayload();
        request.setIncludeJurorsOnCall(true);
        request.setIncludePanelMembers(true);
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(new GroupedTableData()
                    .add("415230102,CROWN COURT",
                        List.of(JUROR_08, JUROR_09))
                    .add("415230103,CROWN COURT",
                        List.of(JUROR_03, JUROR_04, JUROR_05, JUROR_06)),
                6));
    }

    @Test
    void negativeTypicalUnauthorised() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private GroupedReportResponse buildStandardResponse(GroupedTableData data, int count) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name("POOL_NUMBER_AND_COURT_TYPE").nested(null).build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_available_pool_members", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total available pool members")
                    .dataType("Long")
                    .value(count)
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build())
                .add("attendance_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Attendance date")
                    .dataType("LocalDate")
                    .value("2024-01-01")
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
                            .id("status")
                            .name("Status")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("juror_reasonable_adjustment_with_message")
                            .name("Reasonable Adjustments")
                            .dataType("List")
                            .headings(List.of(
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("reasonable_adjustment_code_with_description")
                                    .name("Reasonable Adjustment Code With Description")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("juror_reasonable_adjustment_message")
                                    .name("Juror Reasonable Adjustment Message")
                                    .dataType("String")
                                    .headings(null)
                                    .build()))
                            .build()))
                    .data(data)
                    .build())
            .build();
    }
}
