package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/PanelMembersStatusReportITest_Typical.sql"
})
public class PanelMembersStatusReportITest extends AbstractStandardReportControllerITest {

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
    void assertTotals() {
        StandardReportResponse report = testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated).body();

        long expectedPanelled = report.getTableData().getData().size();
        long expectedEmpanelled = report.getTableData().getData().stream()
            .filter(juror -> juror.get("panel_status").equals("Juror")).count();
        long expectedReturned = report.getTableData().getData().stream()
            .filter(juror -> juror.get("panel_status").equals("Returned")).count();
        long expectedNotUsed = report.getTableData().getData().stream()
            .filter(juror -> juror.get("panel_status").equals("Not Used")).count() + expectedReturned;
        long expectedChallenged = report.getTableData().getData().stream()
            .filter(juror -> juror.get("panel_status").equals("Challenged")).count();

        StandardReportResponse otherReport = getTypicalResponse();

        long actualPanelled = otherReport.getTableData().getData().size();
        long actualEmpanelled = otherReport.getTableData().getData().stream()
            .filter(juror -> juror.get("panel_status").equals("Juror")).count();
        long actualReturned = otherReport.getTableData().getData().stream()
            .filter(juror -> juror.get("panel_status").equals("Returned")).count();
        long actualNotUsed = otherReport.getTableData().getData().stream()
            .filter(juror -> juror.get("panel_status").equals("Not Used")).count() + actualReturned;
        long actualChallenged = otherReport.getTableData().getData().stream()
            .filter(juror -> juror.get("panel_status").equals("Challenged")).count();

        assertEquals(expectedPanelled, actualPanelled);
        assertEquals(expectedEmpanelled, actualEmpanelled);
        assertEquals(expectedReturned, actualReturned);
        assertEquals(expectedNotUsed, actualNotUsed);
        assertEquals(expectedChallenged, actualChallenged);
    }

    @Test
    void positiveNoData() {
        StandardReportRequest request = getValidPayload();
        request.setTrialNumber("111112");

        AbstractReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData = new StandardReportResponse
                                                                                                  .TableData<>();
        tableData.setHeadings(List.of(
            StandardReportResponse.TableData.Heading.builder()
                .id("juror_number")
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
        tableData.setData(List.of());

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
            .value("CName1, CName2, CName3, et al")
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

    private StandardReportResponse.TableData<List<LinkedHashMap<String, Object>>> getResponseTableData() {
        return StandardReportResponse.TableData.<List<LinkedHashMap<String, Object>>>builder()
            .headings(List.of(
                StandardReportResponse.TableData.Heading.builder()
                    .id("juror_number")
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
            .data(List.of(
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500001")
                    .add("panel_status", "Challenged"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500002")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500003")
                    .add("panel_status", "Not Used"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500004")
                    .add("panel_status", "Challenged"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500005")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500006")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500007")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500008")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500009")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500010")
                    .add("panel_status", "Returned"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500011")
                    .add("panel_status", "Returned"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500012")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500013")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500014")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500015")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500016")
                    .add("panel_status", "Returned"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500017")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500018")
                    .add("panel_status", "Juror"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500019")
                    .add("panel_status", "Not Used"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500020")
                    .add("panel_status", "Challenged")
            )).build();
    }
}
