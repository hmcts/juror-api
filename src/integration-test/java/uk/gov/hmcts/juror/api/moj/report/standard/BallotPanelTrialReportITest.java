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
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
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
    void negativeInvalidPayload() {
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

    private StandardReportResponse getTypicalResponse() {
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
                        ))
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
                            .add("juror_postcode", "AD3 2HP")
                    ))
                    .build())
            .build();
    }
}
