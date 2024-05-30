package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/TrialAttendanceReportITest.sql"
})
@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert"//False positive
})
class JuryCostBillITest extends AbstractStandardReportControllerITest {
    @Autowired
    public JuryCostBillITest(TestRestTemplate template) {
        super(template, JuryCostBill.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
                                 .trialNumber("T100000002")
                                 .build());
    }

    @Test
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse("T100000002"));
    }

    @Test
    void negativeNoRecordsFound() {
        StandardReportRequest request = getValidPayload();
        request.setTrialNumber("T100000005");
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getNoRecordsResponse("T100000005"));
    }

    @Test
    void negativeInvalidPayloadTrialNumberMissing() {
        StandardReportRequest request = getValidPayload();
        request.setTrialNumber(null);
        testBuilder()
            .payload(request)
            .triggerInvalid()
            .assertInvalidPathParam("trialNumber: must not be blank");

    }

    @Test
    void negativeTrialNotOwned() {
        StandardReportRequest request = getValidPayload();
        request.setTrialNumber("T100000003");
        testBuilder()
            .payload(request)
            .triggerInvalid()
            .assertNotFound("Trial not found");
    }

    @Test
    void negativeUnauthorisedBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private StandardReportResponse buildResponse(StandardTableData data, String trialNumber) {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                          .add("trial_number", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Trial Number")
                              .dataType("String")
                              .value(trialNumber)
                              .build())
                          .add("names", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Names")
                              .dataType("String")
                              .value("TEST DEFENDANT")
                              .build())
                          .add("trial_type", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Trial type")
                              .dataType("String")
                              .value("Civil")
                              .build())
                          .add("trial_start_date", StandardReportResponse.DataTypeValue.builder()
                              .displayName("Trial start date")
                              .dataType("LocalDate")
                              .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
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
                StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("attendance_date")
                            .name("Attendance date")
                            .dataType("LocalDate")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("financial_loss_due_sum")
                            .name("Financial loss")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("travel_due_sum")
                            .name("Travel")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("subsistence_due_sum")
                            .name("Food and drink")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("smartcard_due_sum")
                            .name("Smartcard")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_due_sum")
                            .name("Total due")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_paid_sum")
                            .name("Paid")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build()))
                    .data(data)
                    .build())
            .build();
    }

    private StandardReportResponse getTypicalResponse(String trialNumber) {
        return buildResponse(StandardTableData.of(
            new ReportLinkedMap<String, Object>()
                .add("attendance_date", "2024-05-06")
                .add("financial_loss_due_sum", 12.0)
                .add("travel_due_sum", 7.63)
                .add("subsistence_due_sum", 0.0)
                .add("smartcard_due_sum", 0)
                .add("total_due_sum", 19.63)
                .add("total_paid_sum", 19.63),
            new ReportLinkedMap<String, Object>()
                .add("attendance_date", "2024-05-07")
                .add("financial_loss_due_sum", 16.0)
                .add("travel_due_sum", 4.0)
                .add("subsistence_due_sum", 0.0)
                .add("smartcard_due_sum", 0)
                .add("total_due_sum", 20.0)
                .add("total_paid_sum", 20.0)
        ), trialNumber);
    }

    private StandardReportResponse getNoRecordsResponse(String trialNumber) {
        return buildResponse(new StandardTableData(), trialNumber);
    }
}
