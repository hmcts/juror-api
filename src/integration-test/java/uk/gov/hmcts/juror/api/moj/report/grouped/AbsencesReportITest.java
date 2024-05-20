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
    "/db/mod/reports/AbsencesReportITest.sql"
})
@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.JUnitTestsShouldIncludeAssert"//False positive
})
class AbsencesReportITest extends AbstractGroupedReportControllerITest {
    @Autowired
    public AbsencesReportITest(TestRestTemplate template) {
        super(template, AbsencesReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.now().minusDays(10))
            .toDate(LocalDate.now())
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
    void negativeNoRecordsFound() {
        StandardReportRequest request = getValidPayload();
        request.setFromDate(LocalDate.now().minusDays(0));
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getNoRecordsResponse());
    }

    @Test
    void negativeInvalidPayloadToDateMissing() {
        StandardReportRequest request = getValidPayload();
        request.setToDate(null);
        testBuilder()
            .payload(request)
            .triggerInvalid()
            .assertInvalidPathParam("toDate: must not be null");
    }

    @Test
    void negativeInvalidPayloadFromDateMissing() {
        StandardReportRequest request = getValidPayload();
        request.setFromDate(null);
        testBuilder()
            .payload(request)
            .triggerInvalid()
            .assertInvalidPathParam("fromDate: must not be null");
    }

    @Test
    void negativeUnauthorisedBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private GroupedReportResponse buildResponse(GroupedTableData data, Integer minusDays) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder()
                         .name("POOL_NUMBER_AND_COURT_TYPE")
                         .build())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                          .add("date_from", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Date from")
                              .dataType("LocalDate")
                              .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(minusDays)))
                              .build())
                          .add("date_to", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Date to")
                              .dataType("LocalDate")
                              .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                              .build())
                          .add("total_due", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Total absences")
                              .dataType("Integer")
                              .value(data.getSize())
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
                            .id("date_of_absence")
                            .name("Date of absence")
                            .dataType("LocalDate")
                            .headings(null)
                            .build()))
                    .data(data)
                    .build())
            .build();
    }

    private GroupedReportResponse getTypicalResponse() {
        return buildResponse(new GroupedTableData()
                                 .add("415240601,CROWN COURT", List.of(
                                     new ReportLinkedMap<String, Object>()
                                         .add("juror_number", "641500021")
                                         .add("first_name", "FNAMETWOONE")
                                         .add("last_name", "LNAMETWOONE")
                                         .add("juror_postal_address", new ReportLinkedMap<String, Object>()
                                             .add("juror_address_line_1", "21 STREET NAME")
                                             .add("juror_address_line_2", "ANYTOWN")
                                             .add("juror_postcode", "CH1 2AN"))
                                         .add("date_of_absence",
                                              DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(5))),
                                     new ReportLinkedMap<String, Object>()
                                         .add("juror_number", "641500021")
                                         .add("first_name", "FNAMETWOONE")
                                         .add("last_name", "LNAMETWOONE")
                                         .add("juror_postal_address", new ReportLinkedMap<String, Object>()
                                             .add("juror_address_line_1", "21 STREET NAME")
                                             .add("juror_address_line_2", "ANYTOWN")
                                             .add("juror_postcode", "CH1 2AN"))
                                         .add("date_of_absence",
                                              DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(4)))
                                     )).add("415240602,HIGH COURT", List.of(
                                         new ReportLinkedMap<String, Object>()
                                             .add("juror_number", "641500009")
                                             .add("first_name", "FNAMENINE")
                                             .add("last_name", "LNAMENINE")
                                             .add("juror_postal_address", new ReportLinkedMap<String, Object>()
                                                 .add("juror_address_line_1", "9 STREET NAME")
                                                 .add("juror_address_line_2", "ANYTOWN")
                                                 .add("juror_address_line_3", "")
                                                 .add("juror_address_line_4", "TOWN")
                                                 .add("juror_address_line_5", "")
                                                 .add("juror_postcode", "CH1 2AN"))
                                             .add("date_of_absence",
                                                  DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(4)))
                )), 10);
    }

    private GroupedReportResponse getNoRecordsResponse() {
        return buildResponse(new GroupedTableData(), 0);
    }
}
