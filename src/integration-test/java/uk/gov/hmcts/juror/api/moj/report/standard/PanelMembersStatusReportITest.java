package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/PanelMembersStatusReportITest_Typical.sql"
})
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
class PanelMembersStatusReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    public PanelMembersStatusReportITest(TestRestTemplate template) {
        super(template, PanelMembersStatusReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .trialNumber("111111")
            .locCode(TestConstants.VALID_COURT_LOCATION)
            .build());
    }

    @Test
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    void negativeUnauthorisedBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    @Test
    void negativeMissingTrialNumber() {
        StandardReportRequest request = getValidPayload();
        request.setTrialNumber(null);

        testBuilder()
            .payload(addReportType(request))
            .jwt(getValidJwt())
            .triggerInvalid()
            .assertInvalidPathParam("trialNumber: must not be blank");
    }

    @Test
    void positiveNoData() {
        StandardReportRequest request = getValidPayload();
        request.setTrialNumber("111112");

        AbstractReportResponse.TableData<StandardTableData> tableData = new StandardReportResponse.TableData<>();

        tableData.setHeadings(List.of(
            StandardReportResponse.TableData.Heading.builder()
                .id("juror_number_from_trial")
                .name("Juror Number")
                .dataType("String")
                .headings(null)
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("panel_status")
                .name("Panel Status")
                .dataType("String")
                .headings(null)
                .build()
        ));
        tableData.setData(new StandardTableData());

        testBuilder()
            .payload(addReportType(request))
            .jwt(getValidJwt())
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(StandardReportResponse.builder()
                .headings(getResponseHeadings("111112"))
                .tableData(tableData)
                .build());
    }

    private StandardReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(getResponseHeadings("111111"))
            .tableData(getResponseTableData())
            .build();
    }

    private ReportHashMap<String, StandardReportResponse.DataTypeValue> getResponseHeadings(String trialNumber) {
        ReportHashMap<String, StandardReportResponse.DataTypeValue> headingsMap = new ReportHashMap<>();

        headingsMap.add("names", StandardReportResponse.DataTypeValue.builder()
            .displayName("Names")
            .dataType("String")
            .value("CName1, et el")
            .build());
        headingsMap.add("trial_number", StandardReportResponse.DataTypeValue.builder()
            .displayName("Trial Number")
            .dataType("String")
            .value(trialNumber)
            .build());
        headingsMap.add("court_room", StandardReportResponse.DataTypeValue.builder()
            .displayName("Court Room")
            .dataType("String")
            .value("The Courtroom")
            .build());
        headingsMap.add("judge", StandardReportResponse.DataTypeValue.builder()
            .displayName("Judge")
            .dataType("String")
            .value("The Judge")
            .build());
        headingsMap.add("court_name", StandardReportResponse.DataTypeValue.builder()
            .displayName("Court Name")
            .dataType("String")
            .value("CHESTER (415)")
            .build());

        return headingsMap;
    }

    private StandardReportResponse.TableData<StandardTableData> getResponseTableData() {
        return StandardReportResponse.TableData.<StandardTableData>builder()
            .headings(List.of(
                StandardReportResponse.TableData.Heading.builder()
                    .id("juror_number_from_trial")
                    .name("Juror Number")
                    .dataType("String")
                    .headings(null)
                    .build(),
                StandardReportResponse.TableData.Heading.builder()
                    .id("panel_status")
                    .name("Panel Status")
                    .dataType("String")
                    .headings(null)
                    .build()
            ))
                .data(new StandardTableData(
                    List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500001")
                            .add("panel_status", "Challenged"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500002")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500003")
                            .add("panel_status", "Not Used"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500004")
                            .add("panel_status", "Challenged"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500005")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500006")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500007")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500008")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500009")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500010")
                            .add("panel_status", "Returned Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500011")
                            .add("panel_status", "Returned Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500012")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500013")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500014")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500015")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500016")
                            .add("panel_status", "Returned"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500017")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500018")
                            .add("panel_status", "Juror"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500019")
                            .add("panel_status", "Not Used"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number_from_trial", "041500020")
                            .add("panel_status", "Challenged")
                    )
                )).build();
    }
}
