package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/JuryListITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class JuryListReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    public JuryListReportITest(TestRestTemplate template) {
        super(template, JuryListReport.class);
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
    void positiveCurrentJurorsOnly() {
        StandardReportRequest request = getValidPayload();
        request.setCurrentJurorsOnly(true);
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getCurrentJurorsOnlyResponse());
    }

    @Test
    void positiveCurrentJurorsOnlyFalse() {
        StandardReportRequest request = getValidPayload();
        request.setCurrentJurorsOnly(false);
        testBuilder()
            .payload(request)
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
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private ReportHashMap<String, StandardReportResponse.DataTypeValue> getStandardResponseHeadings() {
        return new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
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
            .add("trial_start_date", StandardReportResponse.DataTypeValue.builder()
                .displayName("Trial Start Date")
                .dataType(LocalDate.class.getSimpleName())
                .value("2024-07-11")
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
                .build());
    }

    private List<StandardReportResponse.TableData.Heading> getStandardTableHeadings() {
        return List.of(
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
                .id("juror_postcode")
                .name("Postcode")
                .dataType("String")
                .headings(null)
                .build());
    }

    private StandardReportResponse getTypicalResponse() {
        // result = J filter already applied by report — CD/NU jurors never appear
        // 415000007 has result J but return_date set — appears here, excluded by currentJurorsOnly
        return StandardReportResponse.builder()
            .headings(getStandardResponseHeadings())
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(getStandardTableHeadings())
                    .data(StandardTableData.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000001")
                            .add("first_name", "FNAME1")
                            .add("last_name", "LNAME1")
                            .add("juror_postcode", "PO19 1SX"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000002")
                            .add("first_name", "FNAME2")
                            .add("last_name", "LNAME2")
                            .add("juror_postcode", "PO19 1SX"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000003")
                            .add("first_name", "FNAME3")
                            .add("last_name", "LNAME3")
                            .add("juror_postcode", "PO19 1SX"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000007")
                            .add("first_name", "FNAME7")
                            .add("last_name", "LNAME7")
                            .add("juror_postcode", "PO19 1SX")))
                    .build())
            .build();
    }

    private StandardReportResponse getCurrentJurorsOnlyResponse() {
        // 415000001, 415000002, 415000003 — result J, empanelled_date set, return_date NULL
        // 415000007 excluded — result J but return_date set (discharged mid-trial)
        // 415000004, 415000005 excluded — result CD (already excluded by base filter)
        // 415000006 excluded — result NU (already excluded by base filter)
        return StandardReportResponse.builder()
            .headings(getStandardResponseHeadings())
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(getStandardTableHeadings())
                    .data(StandardTableData.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000001")
                            .add("first_name", "FNAME1")
                            .add("last_name", "LNAME1")
                            .add("juror_postcode", "PO19 1SX"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000002")
                            .add("first_name", "FNAME2")
                            .add("last_name", "LNAME2")
                            .add("juror_postcode", "PO19 1SX"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000003")
                            .add("first_name", "FNAME3")
                            .add("last_name", "LNAME3")
                            .add("juror_postcode", "PO19 1SX")))
                    .build())
            .build();
    }
}
