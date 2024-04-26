package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
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
    "/db/mod/reports/PanelSummaryReportITest_typical.sql"
})
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.JUnitTestsShouldIncludeAssert"})
class PanelSummaryReportITest extends AbstractStandardReportControllerITest {
    @Autowired
    public PanelSummaryReportITest(TestRestTemplate template) {
        super(template, PanelSummaryReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .trialNumber("T100000001")
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
    void negativeInvalidPayloadMissingTrialNumber() {
        StandardReportRequest request = getValidPayload();
        request.setTrialNumber(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .assertInvalidPathParam("trialNumber: must not be blank");
    }

    @Test
    void negativeInvalidPayloadMissingLocCode() {
        StandardReportRequest request = getValidPayload();
        request.setLocCode(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .assertInvalidPathParam("locCode: must not be null");
    }

    @Test
    void negativeInvalidPayloadLocCodeInvalidAccess() {
        StandardReportRequest request = getValidPayload();
        request.setLocCode("475");
        testBuilder()
            .payload(addReportType(request))
            .jwt(getCourtJwt("415"))
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this court");
    }

    @Test
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private StandardReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("trial_number", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Trial Number")
                    .dataType("String")
                    .value("T100000001")
                    .build())
                .add("names", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Names")
                    .dataType("String")
                    .value("TEST DEFENDANT")
                    .build())
                .add("court_room", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Room")
                    .dataType("String")
                    .value("large room fits 100 people")
                    .build())
                .add("judge", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Judge")
                    .dataType("String")
                    .value("Test judge")
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build()))
            .tableData(
                StandardReportResponse.TableData.<List<LinkedHashMap<String, Object>>>builder()
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
                            .build()))
                    .data(List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000001")
                            .add("first_name", "FNAME1")
                            .add("last_name", "LNAME1"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000002")
                            .add("first_name", "FNAME2")
                            .add("last_name", "LNAME2"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000003")
                            .add("first_name", "FNAME3")
                            .add("last_name", "LNAME3")))
                    .build())
            .build();
    }
}