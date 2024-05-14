package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/IncompleteServiceReportITest_typical.sql"
})
@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.JUnitTestsShouldIncludeAssert"//False positive
})
class IncompleteServiceReportITest extends AbstractStandardReportControllerITest {
    @Autowired
    public IncompleteServiceReportITest(TestRestTemplate template) {
        super(template, IncompleteServiceReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .reportType("IncompleteServiceReport")
            .date(LocalDate.now())
            .locCode("415")
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
        request.setDate(LocalDate.now().minusDays(100));
        testBuilder()
            .payload(addReportType(request))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getNoRecordsResponse());
    }

    @Test
    void negativeInvalidPayloadLocCodeMissing() {
        StandardReportRequest request = getValidPayload();
        request.setLocCode(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .assertInvalidPathParam("locCode: must not be null");
    }

    @Test
    void negativeInvalidPayloadCutOffDateMissing() {
        StandardReportRequest request = getValidPayload();
        request.setDate(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .assertInvalidPathParam("date: must not be null");
    }

    @Test
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this court");
    }

    @Test
    void negativeUnauthorisedBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private StandardReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("cut_off_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Cut-off Date")
                    .dataType("LocalDate")
                    .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                    .build())
                .add("total_incomplete_service", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total incomplete service")
                    .dataType("Integer")
                    .value(3)
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
                            .id("pool_number_by_jp")
                            .name("Pool Number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("last_attendance_date")
                            .name("Last attended on")
                            .dataType("LocalDate")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("next_attendance_date")
                            .name("Next attendance date")
                            .dataType("LocalDate")
                            .headings(null)
                            .build()))
                    .data(List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500003")
                            .add("first_name", "FNAMETHREE")
                            .add("last_name", "LNAMETHREE")
                            .add("pool_number_by_jp", "415240601")
                            .add("last_attendance_date",
                                DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(1)))
                            .add("next_attendance_date",
                                DateTimeFormatter.ISO_DATE.format(LocalDate.now().plusDays(1))),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500011")
                            .add("first_name", "FNAMEONEONE")
                            .add("last_name", "LNAMEONEONE")
                            .add("pool_number_by_jp", "415240601")
                            .add("last_attendance_date",
                                DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(2)))
                            .add("next_attendance_date",
                                DateTimeFormatter.ISO_DATE.format(LocalDate.now().plusDays(1))),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500021")
                            .add("first_name", "FNAMETWOONE")
                            .add("last_name", "LNAMETWOONE")
                            .add("pool_number_by_jp", "415240601")
                            .add("next_attendance_date",
                                DateTimeFormatter.ISO_DATE.format(LocalDate.now().plusDays(1)))))
                    .build())
            .build();
    }

    private StandardReportResponse getNoRecordsResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("cut_off_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Cut-off Date")
                    .dataType("LocalDate")
                    .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(100)))
                    .build())
                .add("total_incomplete_service", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total incomplete service")
                    .dataType("Integer")
                    .value(0)
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
                            .id("pool_number_by_jp")
                            .name("Pool Number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("last_attendance_date")
                            .name("Last attended on")
                            .dataType("LocalDate")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("next_attendance_date")
                            .name("Next attendance date")
                            .dataType("LocalDate")
                            .headings(null)
                            .build()))
                    .data(List.of())
                    .build())
            .build();
    }
}
