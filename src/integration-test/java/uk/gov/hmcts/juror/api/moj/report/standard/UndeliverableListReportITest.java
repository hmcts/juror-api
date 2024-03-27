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

import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/UndeliverableListReportITest_typical.sql"
})
@SuppressWarnings("PMD.LawOfDemeter")
public class UndeliverableListReportITest extends AbstractReportControllerITest {

    @Autowired
    public UndeliverableListReportITest(TestRestTemplate template) {
        super(template, UndeliverableListReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("400");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .poolNumber("415230103")
            .build());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void positiveTypicalBureau() {
        testBuilder()
            .triggerValid()
            .printResponse()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    private StandardReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_undelivered", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total undelivered")
                    .dataType("Long")
                    .value(2)
                    .build())
                .add("service_start_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Service Start Date")
                    .dataType("LocalDate")
                    .value("2023-01-05")
                    .build())
                .add("pool_number", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Number")
                    .dataType("String")
                    .value("415230103")
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build())
                .add("pool_type", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Type")
                    .dataType("String")
                    .value("CROWN COURT")
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
                            .id("address")
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
                            .build()))
                    .data(List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500001")
                            .add("first_name", "FirstName1")
                            .add("last_name", "LastName1")
                            .add("address", new ReportLinkedMap<String, Object>()
                                .add("address_line_1", "1 TheAddressLine1")
                                .add("address_line_2", "2 TheAddressLine1")
                                .add("address_line_3", "3 TheAddressLine1")
                                .add("address_line_4", "4 TheAddressLine1")
                                .add("address_line_5", "5 TheAddressLine1")
                                .add("postcode", "MK1 1LA")),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500002")
                            .add("first_name", "FirstName2")
                            .add("last_name", "LastName2")
                            .add("address", new ReportLinkedMap<String, Object>()
                                .add("address_line_1", "1 TheAddressLine2")
                                .add("address_line_2", "2 TheAddressLine2")
                                .add("address_line_3", "3 TheAddressLine2")
                                .add("address_line_4", "4 TheAddressLine2")
                                .add("address_line_5", "5 TheAddressLine2")
                                .add("postcode", "MK1 1LA"))))
                    .build())
            .build();
    }
}
