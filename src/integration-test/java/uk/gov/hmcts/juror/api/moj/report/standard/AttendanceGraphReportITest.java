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
    "/db/mod/truncate.sql",
    "/db/mod/reports/AttendanceGraphReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class AttendanceGraphReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    public AttendanceGraphReportITest(TestRestTemplate template) {
        super(template, AttendanceGraphReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2024, 1, 1))
            .toDate(LocalDate.of(2024, 1, 30))
            .build());
    }

    private StandardReportResponse buildStandardReportResponse(StandardTableData data) {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<>())
            .tableData(
                AbstractReportResponse.TableData.<StandardTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("attendance_date")
                            .name("Attendance Date")
                            .dataType("LocalDate")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("attendance_count")
                            .name("Attendance count")
                            .dataType("Long")
                            .headings(null)
                            .build()))
                    .data(data)
                    .build())
            .build();
    }

    @Test
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardReportResponse(StandardTableData.of(
                new ReportLinkedMap<String, Object>()
                    .add("attendance_date", "2024-01-01")
                    .add("attendance_count", 4),
                new ReportLinkedMap<String, Object>()
                    .add("attendance_date", "2024-01-02")
                    .add("attendance_count", 4),
                new ReportLinkedMap<String, Object>()
                    .add("attendance_date", "2024-01-03")
                    .add("attendance_count", 5))));
    }

    @Test
    void positiveNotFound() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardReportResponse(StandardTableData.of()));
    }

    @Test
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }
}
