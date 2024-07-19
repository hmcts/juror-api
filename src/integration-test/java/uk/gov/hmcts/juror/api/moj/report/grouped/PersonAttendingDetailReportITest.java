package uk.gov.hmcts.juror.api.moj.report.grouped;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/PersonAttendingSummaryReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class PersonAttendingDetailReportITest extends AbstractGroupedReportControllerITest {
    @Autowired
    public PersonAttendingDetailReportITest(TestRestTemplate template) {
        super(template, PersonAttendingDetailReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .date(LocalDate.now().plusDays(1))
            .locCode("415")
            .includeSummoned(false)
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
    void positiveIncludeSummons() {
        StandardReportRequest request = getValidPayload();
        request.setIncludeSummoned(true);
        request.setDate(LocalDate.now().plusDays(2));
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getIncludeSummonsResponse());
    }

    @Test
    void negativeNoRecordsFound() {
        StandardReportRequest request = getValidPayload();
        request.setDate(LocalDate.now().minusDays(100));
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getNoRecordsResponse());
    }

    @Test
    void negativeInvalidPayloadAttendanceDateMissing() {
        StandardReportRequest request = getValidPayload();
        request.setDate(null);
        testBuilder()
            .payload(request)
            .triggerInvalid()
            .assertInvalidPathParam("date: must not be null");
    }

    @Test
    void negativeUnauthorisedBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private GroupedReportResponse buildResponse(GroupedTableData data, Integer plusDays) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder()
                         .name("POOL_NUMBER")
                         .build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                          .add("attendance_date", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Attendance date")
                              .dataType("LocalDate")
                              .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now().plusDays(plusDays)))
                              .build())
                          .add("total_due", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Total due to attend")
                              .dataType("Integer")
                              .value(data.getSize().intValue())
                              .build())
                          .add("court_name", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Court Name")
                              .dataType("String")
                              .value("CHESTER (415)")
                              .build()))
            .tableData(
                GroupedReportResponse.TableData.<GroupedTableData>builder()
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
                            .id("juror_postal_address")
                            .name("Address")
                            .dataType("List")
                            .headings(List.of(
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("juror_address_line_1")
                                    .name("Address Line 1")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("juror_address_line_2")
                                    .name("Address Line 2")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("juror_address_line_3")
                                    .name("Address Line 3")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("juror_address_line_4")
                                    .name("Address Line 4")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("juror_address_line_5")
                                    .name("Address Line 5")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("juror_postcode")
                                    .name("Postcode")
                                    .dataType("String")
                                    .headings(null)
                                    .build()))
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
                    .data(data)
                    .build())
            .build();
    }

    private GroupedReportResponse getTypicalResponse() {
        return buildResponse(new GroupedTableData()
                                 .add("415240601", List.of(
                                     new ReportLinkedMap<String, Object>()
                                         .add("juror_number", "641500011")
                                         .add("first_name", "FNAMEONEONE")
                                         .add("last_name", "LNAMEONEONE")
                                         .add("juror_postal_address", new ReportLinkedMap<String, Object>()
                                             .add("juror_address_line_1", "11 STREET NAME")
                                             .add("juror_address_line_2", "ANYTOWN")
                                             .add("juror_postcode", "CH2 2AB")),
                                     new ReportLinkedMap<String, Object>()
                                         .add("juror_number", "641500003")
                                         .add("first_name", "FNAMETHREE")
                                         .add("last_name", "LNAMETHREE")
                                         .add("juror_postal_address", new ReportLinkedMap<String, Object>()
                                             .add("juror_address_line_1", "3 STREET NAME")
                                             .add("juror_address_line_2", "ANYTOWN")
                                             .add("juror_address_line_3", "")
                                             .add("juror_address_line_4", "London")
                                             .add("juror_address_line_5", "")
                                             .add("juror_postcode", "CH1 2AN"))
                                         .add("contact_details", new ReportLinkedMap<String, Object>()
                                             .add("email", "")),
                                     new ReportLinkedMap<String, Object>()
                                         .add("juror_number", "641500021")
                                         .add("first_name", "FNAMETWOONE")
                                         .add("last_name", "LNAMETWOONE")
                                         .add("juror_postal_address", new ReportLinkedMap<String, Object>()
                                             .add("juror_address_line_1", "21 STREET NAME")
                                             .add("juror_address_line_2", "ANYTOWN")
                                             .add("juror_postcode", "CH1 2AN")))), 1);
    }

    private GroupedReportResponse getIncludeSummonsResponse() {
        return buildResponse(new GroupedTableData()
                                 .add("415240601", List.of(
                                     new ReportLinkedMap<String, Object>()
                                         .add("juror_number", "641500004")
                                         .add("first_name", "FNAMEFOUR")
                                         .add("last_name", "LNAMEFOUR")
                                         .add("juror_postal_address", new ReportLinkedMap<String, Object>()
                                             .add("juror_address_line_1", "4 STREET NAME")
                                             .add("juror_address_line_2", "ANYTOWN")
                                             .add("juror_address_line_3", "")
                                             .add("juror_address_line_4", "London")
                                             .add("juror_address_line_5", "")
                                             .add("juror_postcode", "CH1 2AN"))
                                         .add("contact_details", new ReportLinkedMap<String, Object>()
                                             .add("email", "")),
                                     new ReportLinkedMap<String, Object>()
                                         .add("juror_number", "641500007")
                                         .add("first_name", "FNAMESEVEN")
                                         .add("last_name", "LNAMESEVEN")
                                         .add("juror_postal_address", new ReportLinkedMap<String, Object>()
                                             .add("juror_address_line_1", "7 STREET NAME")
                                             .add("juror_address_line_2", "ANYTOWN")
                                             .add("juror_address_line_3", "")
                                             .add("juror_address_line_4", "TOWN")
                                             .add("juror_address_line_5", "")
                                             .add("juror_postcode", "CH1 2AN"))
                                         .add("contact_details", new ReportLinkedMap<String, Object>()
                                             .add("email", "")))), 2);
    }

    private GroupedReportResponse getNoRecordsResponse() {
        return buildResponse(new GroupedTableData(), -100);
    }
}
