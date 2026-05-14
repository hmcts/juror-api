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

import java.util.List;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/BallotPanelTrIalITest.sql"
})
class BallotPanelTrialReportITest extends AbstractStandardReportControllerITest {
    @Autowired
    public BallotPanelTrialReportITest(TestRestTemplate template) {
        super(template, BallotPanelTrialReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .trialNumber(TestConstants.VALID_TRIAL_NUMBER)
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
    void positiveAnonymousCourt() {
        StandardReportRequest request = getValidPayload();
        request.setTrialNumber("T000000002");
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getAnonymousResponse());
    }

    @Test
    void negativeInvalidPayload() {
        StandardReportRequest request = getValidPayload();
        request.setTrialNumber(null);
        testBuilder()
            .payload(request)
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

    private StandardReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<>())
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(getStandardHeadings())
                    .data(StandardTableData.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000001")
                            .add("first_name", "John1")
                            .add("last_name", "Smith1")
                            .add("juror_postcode", "AD1 2HP"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000002")
                            .add("first_name", "John2")
                            .add("last_name", "Smith2")
                            .add("juror_postcode", "AD2 2HP"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000003")
                            .add("first_name", "John3")
                            .add("last_name", "Smith3")
                            .add("juror_postcode", "AD3 2HP"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000007")
                            .add("first_name", "John7")
                            .add("last_name", "Smith7")
                            .add("juror_postcode", "AD7 2HP"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000008")
                            .add("first_name", "John8")
                            .add("last_name", "Smith8")
                            .add("juror_postcode", "AD8 2HP"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000009")
                            .add("first_name", "John9")
                            .add("last_name", "Smith9")
                            .add("juror_postcode", "AD9 2HP")
                    ))
                    .build())
            .build();
    }

    private StandardReportResponse getCurrentJurorsOnlyResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<>())
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(getStandardHeadings())
                    .data(StandardTableData.of(
                        // 200000001, 200000002, 200000003 — result J, empanelled, no return date
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000001")
                            .add("first_name", "John1")
                            .add("last_name", "Smith1")
                            .add("juror_postcode", "AD1 2HP"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000002")
                            .add("first_name", "John2")
                            .add("last_name", "Smith2")
                            .add("juror_postcode", "AD2 2HP"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000003")
                            .add("first_name", "John3")
                            .add("last_name", "Smith3")
                            .add("juror_postcode", "AD3 2HP")
                    // 200000007 excluded — result NU
                    // 200000008 excluded — result CD
                    // 200000009 excluded — result J but return_date set
                    ))
                    .build())
            .build();
    }

    private StandardReportResponse getAnonymousResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<>())
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(
                        List.of(
                            StandardReportResponse.TableData.Heading.builder()
                                .id("juror_number")
                                .name("Juror Number")
                                .dataType("String")
                                .headings(null)
                                .build()
                        ))
                    .data(StandardTableData.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000004"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000005"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "200000006")
                    ))
                    .build())
            .build();
    }

    private List<StandardReportResponse.TableData.Heading> getStandardHeadings() {
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
                .build()
        );
    }
}
