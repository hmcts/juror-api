package uk.gov.hmcts.juror.api.moj.report.grouped;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/TrialAttendanceReportITest.sql"
})
class TrialAttendanceReportITest extends AbstractGroupedReportControllerITest {
    @Autowired
    public TrialAttendanceReportITest(TestRestTemplate template) {
        super(template, TrialAttendanceReport.class);
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
    void negativeUnauthorisedBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private GroupedReportResponse buildResponse(GroupedTableData data, String trialNumber) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder()
                         .name("ATTENDANCE_DATE")
                         .build())
            .headings(new ReportHashMap<String, GroupedReportResponse.DataTypeValue>()
                          .add("trial_number", GroupedReportResponse.DataTypeValue.builder()
                              .displayName("Trial Number")
                              .dataType("String")
                              .value(trialNumber)
                              .build())
                          .add("names", GroupedReportResponse.DataTypeValue.builder()
                              .displayName("Names")
                              .dataType("String")
                              .value("TEST DEFENDANT")
                              .build())
                          .add("trial_type", GroupedReportResponse.DataTypeValue.builder()
                              .displayName("Trial type")
                              .dataType("String")
                              .value("Civil")
                              .build())
                          .add("trial_start_date", GroupedReportResponse.DataTypeValue.builder()
                              .displayName("Trial start date")
                              .dataType("LocalDate")
                              .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                              .build())
                          .add("court_room", GroupedReportResponse.DataTypeValue.builder()
                              .displayName("Court Room")
                              .dataType("String")
                              .value("large room fits 100 people")
                              .build())
                          .add("judge", GroupedReportResponse.DataTypeValue.builder()
                              .displayName("Judge")
                              .dataType("String")
                              .value("Test judge")
                              .build())
                          .add("court_name", GroupedReportResponse.DataTypeValue.builder()
                              .displayName("Court Name")
                              .dataType("String")
                              .value("CHESTER (415)")
                              .build()))
            .tableData(
                GroupedReportResponse.TableData.<GroupedTableData>builder()
                    .headings(List.of(
                        GroupedReportResponse.TableData.Heading.builder()
                            .id("juror_number")
                            .name("Juror number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        GroupedReportResponse.TableData.Heading.builder()
                            .id("first_name")
                            .name("First name")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        GroupedReportResponse.TableData.Heading.builder()
                            .id("last_name")
                            .name("Last name")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        GroupedReportResponse.TableData.Heading.builder()
                            .id("checked_in")
                            .name("Checked in")
                            .dataType("LocalTime")
                            .headings(null)
                            .build(),
                        GroupedReportResponse.TableData.Heading.builder()
                            .id("checked_out")
                            .name("Checked out")
                            .dataType("LocalTime")
                            .headings(null)
                            .build(),
                        GroupedReportResponse.TableData.Heading.builder()
                            .id("hours_attended")
                            .name("Hours attended")
                            .dataType("LocalTime")
                            .headings(null)
                            .build(),
                        GroupedReportResponse.TableData.Heading.builder()
                            .id("attendance_audit")
                            .name("Attendance audit")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        GroupedReportResponse.TableData.Heading.builder()
                            .id("payment_audit")
                            .name("Payment audit")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        GroupedReportResponse.TableData.Heading.builder()
                            .id("total_due")
                            .name("Total due")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build(),
                        GroupedReportResponse.TableData.Heading.builder()
                            .id("total_paid")
                            .name("Paid")
                            .dataType("BigDecimal")
                            .headings(null)
                            .build()))
                    .data(data)
                    .build())
            .build();
    }

    private GroupedReportResponse getTypicalResponse(String trialNumber) {
        return buildResponse(new GroupedTableData()
                                 .add("2024-05-06", List.of(
                                     new ReportLinkedMap<String, Object>()
                                         .add("juror_number", "200956973")
                                         .add("first_name", "Alyce")
                                         .add("last_name", "Almoney")
                                         .add("checked_in", "08:30:00")
                                         .add("checked_out", "16:30:00")
                                         .add("hours_attended", "08:00:00")
                                         .add("attendance_audit", "P10011777")
                                         .add("payment_audit", "F12")
                                         .add("total_due", 19.63)
                                         .add("total_paid", 19.63)))
                                 .add("2024-05-07", List.of(
                                     new ReportLinkedMap<String, Object>()
                                         .add("juror_number", "200956973")
                                         .add("first_name", "Alyce")
                                         .add("last_name", "Almoney")
                                         .add("checked_in", "08:30:00")
                                         .add("checked_out", "16:30:00")
                                         .add("hours_attended", "08:00:00")
                                         .add("attendance_audit", "P10012682")
                                         .add("payment_audit", "F16")
                                         .add("total_due", 20.0)
                                         .add("total_paid", 20.0))),
                             trialNumber);
    }

    private GroupedReportResponse getNoRecordsResponse(String trialNumber) {
        return buildResponse(new GroupedTableData(), trialNumber);
    }
}
