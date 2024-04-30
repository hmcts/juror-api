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
    "/db/mod/reports/PanelListDetailedReportITest_typical.sql"
})
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.JUnitTestsShouldIncludeAssert"})
class PanelListDetailedReportITest extends AbstractStandardReportControllerITest {
    @Autowired
    public PanelListDetailedReportITest(TestRestTemplate template) {
        super(template, PanelListDetailedReport.class);
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
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("juror_postcode")
                            .name("Postcode")
                            .dataType("String")
                            .headings(null)
                            .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("contact_details")
                                .name("Contact Details")
                                .dataType("List")
                                .headings(List.of(
                                    StandardReportResponse.TableData.Heading.builder()
                                        .id("main_phone")
                                        .name("Main Phone")
                                        .dataType("String")
                                        .headings(null)
                                        .build(),
                                    StandardReportResponse.TableData.Heading.builder()
                                        .id("other_phone")
                                        .name("Other Phone")
                                        .dataType("String")
                                        .headings(null)
                                        .build(),
                                    StandardReportResponse.TableData.Heading.builder()
                                        .id("work_phone")
                                        .name("Work Phone")
                                        .dataType("String")
                                        .headings(null)
                                        .build(),
                                    StandardReportResponse.TableData.Heading.builder()
                                        .id("email")
                                        .name("Email")
                                        .dataType("String")
                                        .headings(null)
                                        .build()))
                                .build()))
                    .data(List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000001")
                            .add("first_name", "Jenna")
                            .add("last_name", "Magura")
                            .add("juror_postcode", "G46 6JF")
                            .add("contact_details",  new ReportLinkedMap<String, Object>()
                                .add("main_phone", "44141101-1110")
                                .add("other_phone", "44776-301-1110")
                                .add("work_phone", "44141201-1110")
                                .add("email", "Magura0@email.com")),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000002")
                            .add("first_name", "Rhonda")
                            .add("last_name", "Lovejoy")
                            .add("juror_postcode", "G46 6JF")
                            .add("contact_details",  new ReportLinkedMap<String, Object>()
                                .add("main_phone", "44141101-1111")
                                .add("other_phone", "44776-301-1111")
                                .add("work_phone", "44141201-1111")
                                .add("email", "Lovejoy1@email.com")),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000003")
                            .add("first_name", "Clarine")
                            .add("last_name", "Rowsey")
                            .add("juror_postcode", "G466JF")
                            .add("contact_details",  new ReportLinkedMap<String, Object>()
                                .add("main_phone", "44141101-1112")
                                .add("other_phone", "44776-301-1112")
                                .add("work_phone", "44141201-1112")
                                .add("email", "Rowsey2@email.com"))))
                    .build())
            .build();
    }
}