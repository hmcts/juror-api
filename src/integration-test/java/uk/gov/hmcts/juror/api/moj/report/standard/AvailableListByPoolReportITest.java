package uk.gov.hmcts.juror.api.moj.report.standard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;

import java.util.List;

import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_03;
import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_04;
import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_05;
import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_06;
import static uk.gov.hmcts.juror.api.moj.report.AvailableListReportUtil.JUROR_07;


@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/AvailableListReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class AvailableListByPoolReportITest extends AbstractStandardReportControllerITest {


    @Autowired
    public AvailableListByPoolReportITest(TestRestTemplate template) {
        super(template, AvailableListByPoolReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .poolNumber("415230103")
            .includePanelMembers(false)
            .includeJurorsOnCall(false)
            .respondedJurorsOnly(false)
            .build());
    }


    @Test
    @SneakyThrows
    void positiveCourtAllFalse() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(StandardTableData.of(
                JUROR_03, JUROR_07), 2));
    }

    @Test
    void positiveCourtIncludePanelMembers() {
        StandardReportRequest request = getValidPayload();
        request.setIncludePanelMembers(true);
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(StandardTableData.of(
                JUROR_03, JUROR_05, JUROR_07), 3));
    }

    @Test
    void positiveCourtIncludeOnCall() {
        StandardReportRequest request = getValidPayload();
        request.setIncludeJurorsOnCall(true);
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(StandardTableData.of(
                JUROR_03, JUROR_04, JUROR_07), 3));
    }

    @Test
    void positiveCourtRespondedOnly() {
        StandardReportRequest request = getValidPayload();
        request.setRespondedJurorsOnly(true);
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(
                StandardTableData.of(JUROR_07), 1));
    }

    @Test
    void positiveCourtIncludeJurorsOnCallAndPanelMembers() {
        StandardReportRequest request = getValidPayload();
        request.setIncludeJurorsOnCall(true);
        request.setIncludePanelMembers(true);
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(StandardTableData.of(
                JUROR_03, JUROR_04, JUROR_05, JUROR_06, JUROR_07
            ), 5));
    }

    @Test
    void positiveBureauIncludeJurorsOnCallAndPanelMembers() {
        StandardReportRequest request = getValidPayload();
        request.setIncludeJurorsOnCall(true);
        request.setIncludePanelMembers(true);
        testBuilder()
            .payload(request)
            .jwt(getBureauJwt())
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardResponse(StandardTableData.of(
                JUROR_03, JUROR_04, JUROR_05, JUROR_06, JUROR_07
            ), 5));
    }

    @Test
    void negativeTypicalUnauthorised() {
        StandardReportRequest request = getValidPayload();
        request.setPoolNumber("415230101");
        testBuilder()
            .payload(request)
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this pool");
    }

    private StandardReportResponse buildStandardResponse(StandardTableData data, int count) {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("service_start_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Service Start Date")
                    .dataType("LocalDate")
                    .value("2023-01-07")
                    .build())
                .add("pool_number", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Number")
                    .dataType("String")
                    .value("415230103")
                    .build())
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
                .add("pool_type", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Type")
                    .dataType("String")
                    .value("CROWN COURT")
                    .build()))
            .tableData(
                AbstractReportResponse.TableData.<StandardTableData>builder()
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
