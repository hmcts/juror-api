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

import java.util.List;

@Sql({
    "/db/mod/truncate.sql",
    "/db/administration/createJudges.sql",
    "/db/administration/createCourtRooms.sql",
    "/db/mod/reports/PoolAttendanceAuditReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class PoolAttendanceAuditReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    public PoolAttendanceAuditReportITest(TestRestTemplate template) {
        super(template, PoolAttendanceAuditReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .poolAuditNumber("P12345678")
            .build());
    }

    @Test
    void positiveTypical() {
        testBuilder().triggerInvalid().printResponse();
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(StandardReportResponse.builder()
                .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                    .add("total", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Total")
                        .dataType("Long")
                        .value(6)
                        .build())
                    .add("audit_number", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Audit number")
                        .dataType("String")
                        .value("P12345678")
                        .build())
                    .add("attendance_date", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Attendance date")
                        .dataType("LocalDate")
                        .value("2024-01-01")
                        .build())
                    .add("court_name", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Court Name")
                        .dataType("String")
                        .value("CHESTER (415)")
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
                                .id("appearance_checked_in")
                                .name("Checked In")
                                .dataType("LocalTime")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("appearance_checked_out")
                                .name("Checked Out")
                                .dataType("LocalTime")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("pool_number_by_appearance")
                                .name("Pool Number")
                                .dataType("String")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("appearance_trial_number")
                                .name("Trial Number")
                                .dataType("String")
                                .headings(null)
                                .build()))
                        .data(StandardTableData.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "100000000")
                                .add("first_name", "FName 0")
                                .add("last_name", "LName 0")
                                .add("appearance_checked_in", "08:30:00")
                                .add("appearance_checked_out", "15:30:00")
                                .add("pool_number_by_appearance", "200000000")
                                .add("appearance_trial_number", "T123"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "100000001")
                                .add("first_name", "FName 1")
                                .add("last_name", "LName 1")
                                .add("appearance_checked_in", "08:30:00")
                                .add("appearance_checked_out", "17:30:00")
                                .add("pool_number_by_appearance", "200000000")
                                .add("appearance_trial_number", "T123"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "100000002")
                                .add("first_name", "FName 2")
                                .add("last_name", "LName 2")
                                .add("appearance_checked_in", "07:30:00")
                                .add("appearance_checked_out", "17:30:00")
                                .add("pool_number_by_appearance", "200000001"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "100000003")
                                .add("first_name", "FName 3")
                                .add("last_name", "LName 3")
                                .add("appearance_checked_in", "08:20:00")
                                .add("appearance_checked_out", "19:30:00")
                                .add("pool_number_by_appearance", "200000000")
                                .add("appearance_trial_number", "T123"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "100000004")
                                .add("first_name", "FName 4")
                                .add("last_name", "LName 4")
                                .add("appearance_checked_in", "08:30:00")
                                .add("appearance_checked_out", "17:30:00")
                                .add("pool_number_by_appearance", "200000002"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "100000005")
                                .add("first_name", "FName 5")
                                .add("last_name", "LName 5")
                                .add("appearance_checked_in", "08:30:00")
                                .add("appearance_checked_out", "17:30:00")
                                .add("pool_number_by_appearance", "200000000")
                                .add("appearance_trial_number", "T123")))
                        .build())
                .build())
        ;
    }

    @Test
    void negativeNotFound() {
        StandardReportRequest request = getValidPayload();
        request.setPoolAuditNumber("P123");
        testBuilder()
            .payload(request)
            .triggerInvalid()
            .assertNotFound("Audit Number not found");
    }

    @Test
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }
}
