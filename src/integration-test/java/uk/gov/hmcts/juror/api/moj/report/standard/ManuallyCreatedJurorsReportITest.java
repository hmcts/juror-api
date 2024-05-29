package uk.gov.hmcts.juror.api.moj.report.standard;

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
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/ManuallyCreatedJurorsReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class ManuallyCreatedJurorsReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    public ManuallyCreatedJurorsReportITest(TestRestTemplate template) {
        super(template, ManuallyCreatedJurorsReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2024, 1, 1))
            .toDate(LocalDate.of(2024, 1, 31))
            .build());
    }

    @Test
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardReportResponse(5, StandardTableData.of(
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500001")
                    .add("created_on", "2024-01-01")
                    .add("created_by", "Court Standard")
                    .add("first_name", "CName1")
                    .add("last_name", "CSurname1")
                    .add("address_combined",
                        new ReportLinkedMap<String, Object>()
                            .add("address_line_1", "1 addressLine1")
                            .add("address_line_2", "1 addressLine2")
                            .add("address_line_3", "1 addressLine3")
                            .add("address_line_4", "1 addressLine4")
                            .add("postcode", "CH1 1AN"))
                    .add("status", "Authorised")
                    .add("pool_number", "415240101")
                    .add("service_completed", "2024-01-15"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500002")
                    .add("created_on", "2024-01-02")
                    .add("created_by", "Court Standard")
                    .add("first_name", "CName2")
                    .add("last_name", "CSurname2")
                    .add("address_combined",
                        new ReportLinkedMap<String, Object>()
                            .add("address_line_1", "2 addressLine1")
                            .add("address_line_2", "2 addressLine2")
                            .add("address_line_3", "2 addressLine3")
                            .add("address_line_4", "2 addressLine4")
                            .add("postcode", "CH1 2AN"))
                    .add("status", "Authorised")
                    .add("pool_number", "415240101"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500003")
                    .add("created_on", "2024-01-20")
                    .add("created_by", "Court Standard")
                    .add("first_name", "CName3")
                    .add("last_name", "CSurname3")
                    .add("address_combined",
                        new ReportLinkedMap<String, Object>()
                            .add("address_line_1", "3 addressLine1")
                            .add("address_line_2", "3 addressLine2")
                            .add("address_line_3", "3 addressLine3")
                            .add("address_line_4", "3 addressLine4")
                            .add("postcode", "CH1 3AN"))
                    .add("status", "Queued")
                    .add("pool_number", "415240102"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500004")
                    .add("created_on", "2024-01-21")
                    .add("created_by", "Court Standard")
                    .add("first_name", "CName4")
                    .add("last_name", "CSurname4")
                    .add("address_combined",
                        new ReportLinkedMap<String, Object>()
                            .add("address_line_1", "4 addressLine1")
                            .add("address_line_2", "4 addressLine2")
                            .add("address_line_3", " 4addressLine3")
                            .add("address_line_4", "4 addressLine4")
                            .add("postcode", "CH1 4AN"))
                    .add("status", "Queued")
                    .add("pool_number", "415240103"),
                new ReportLinkedMap<String, Object>()
                    .add("juror_number", "041500005")
                    .add("created_on", "2024-01-01")
                    .add("created_by", "Court Standard")
                    .add("first_name", "CName5")
                    .add("last_name", "CSurname5")
                    .add("address_combined",
                        new ReportLinkedMap<String, Object>()
                            .add("address_line_1", "5 addressLine1")
                            .add("address_line_2", "5 addressLine2")
                            .add("address_line_3", " 5addressLine3")
                            .add("address_line_4", "5 addressLine4")
                            .add("postcode", "CH1 5AN"))
                    .add("status", "Rejected")
                    .add("pool_number", "415240103")
            )));
    }

    @Test
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private StandardReportResponse buildStandardReportResponse(int count, StandardTableData tableData) {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_manually_created_jurors", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total manually-created jurors")
                    .dataType("Long")
                    .value(count)
                    .build())
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date to")
                    .dataType("LocalDate")
                    .value("2024-01-31")
                    .build())
                .add("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date from")
                    .dataType("LocalDate")
                    .value("2024-01-01")
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
                            .id("created_on")
                            .name("Created On")
                            .dataType("LocalDate")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("created_by")
                            .name("Created by")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("first_name")
                            .name("First name")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("last_name")
                            .name("Last name")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("address_combined")
                            .name("Address")
                            .dataType("List")
                            .headings(List.of(
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("address_line_1")
                                    .name("Address Line 1")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("address_line_2")
                                    .name("Address Line 2")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("address_line_3")
                                    .name("Address Line 3")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("address_line_4")
                                    .name("Address Line 4")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("address_line_5")
                                    .name("Address Line 5")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("postcode")
                                    .name("Postcode")
                                    .dataType("String")
                                    .headings(null)
                                    .build()))
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("status")
                            .name("status")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("notes")
                            .name("Notes")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("pool_number")
                            .name("Pool number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("service_completed")
                            .name("Service completed")
                            .dataType("LocalDate")
                            .headings(null)
                            .build()))
                    .data(tableData)
                    .build())
            .build();
    }
}
