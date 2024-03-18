package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;
import uk.gov.hmcts.juror.api.moj.report.TmpSupport;

import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/CurrentPoolStatusReportControllerITest_typical.sql"
})
class CurrentPoolStatusReportControllerITest extends AbstractReportControllerITest {

    @Autowired
    public CurrentPoolStatusReportControllerITest(TestRestTemplate template) {
        super(template, CurrentPoolStatusReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .poolNumber("415230103")
            .build());
    }


    @Test
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .printResponse()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    void positiveTypicalBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerValid()
            .printResponse()
            .responseConsumer(response -> {
                System.out.println(TmpSupport.asString(response));
            })
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    void negativeInvalidPayload() {
        StandardReportRequest request = getValidPayload();
        request.setPoolNumber(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .printResponse()
            .assertInvalidPathParam("poolNumber: must not be null");
    }

    @Test
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerInvalid()
            .printResponse()
            .assertMojForbiddenResponse("User not allowed to access this pool");
    }


    private StandardReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build())
                .add("service_start_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Service Start Date")
                    .dataType("LocalDate")
                    .value("2023-01-05")
                    .build())
                .add("total_pool_members", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total Pool Members")
                    .dataType("Long")
                    .value(4)
                    .build())
                .add("pool_type", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Type")
                    .dataType("String")
                    .value("CROWN COURT")
                    .build())
                .add("pool_number", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Number")
                    .dataType("String")
                    .value("415230103")
                    .build()))
            .tableData(
                StandardReportResponse.TableData.builder()
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
                            .id("deferrals")
                            .name("Deferrals")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("absences")
                            .name("Absences")
                            .dataType("Long")
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
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("warning")
                            .name("Warning")
                            .dataType("String")
                            .headings(null)
                            .build()))
                    .data(List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500023")
                            .add("first_name", "John3")
                            .add("last_name", "Smith3")
                            .add("status", "Juror")
                            .add("deferrals", 0)
                            .add("absences", 1)
                            .add("contact_details",
                                new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "300000003")
                                    .add("other_phone", "300000001")
                                    .add("work_phone", "300000002")
                                    .add("email", "641500023@email.gov.uk"))
                            .add("warning", ""),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500024")
                            .add("first_name", "John4")
                            .add("last_name", "Smith4")
                            .add("status", "Excused")
                            .add("deferrals", 0)
                            .add("absences", 0)
                            .add("contact_details",
                                new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "400000003")
                                    .add("other_phone", "400000001")
                                    .add("work_phone", "400000002")
                                    .add("email", "641500024@email.gov.uk"))
                            .add("warning", "Not police checked"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500025")
                            .add("first_name", "John5")
                            .add("last_name", "Smith5")
                            .add("status", "Disqualified")
                            .add("deferrals", 0)
                            .add("absences", 0)
                            .add("contact_details",
                                new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "500000003")
                                    .add("other_phone", "500000001")
                                    .add("work_phone", "500000002")
                                    .add("email", "641500025@email.gov.uk"))
                            .add("warning", "Failed police check"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500026")
                            .add("first_name", "John6")
                            .add("last_name", "Smith6")
                            .add("status", "Deferred")
                            .add("deferrals", 0)
                            .add("absences", 0)
                            .add("contact_details",
                                new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "600000003")
                                    .add("other_phone", "600000001")
                                    .add("work_phone", "600000002")
                                    .add("email", "641500026@email.gov.uk"))
                            .add("warning", "Not police checked")))
                    .build())
            .build();
    }
}
