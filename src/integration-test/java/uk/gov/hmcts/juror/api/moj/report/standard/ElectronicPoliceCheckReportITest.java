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
    "/db/mod/reports/ElectronicPoliceCheckReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class ElectronicPoliceCheckReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    public ElectronicPoliceCheckReportITest(TestRestTemplate template) {
        super(template, ElectronicPoliceCheckReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getBureauJwt();
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2024, 1, 1))
            .toDate(LocalDate.of(2024, 1, 30))
            .build());
    }

    @Test
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(
                buildResponse("2024-01-01", "2024-01-30",
                    StandardTableData.of(
                        new ReportLinkedMap<String, Object>()
                            .add("pool_number_jp", "200000000")
                            .add("police_check_responded", 2)
                            .add("police_check_submitted", 2)
                            .add("police_check_complete", 2)
                            .add("police_check_timed_out", 0)
                            .add("police_check_disqualified", 1),
                        new ReportLinkedMap<String, Object>()
                            .add("pool_number_jp", "200000001")
                            .add("police_check_responded", 3)
                            .add("police_check_submitted", 3)
                            .add("police_check_complete", 1)
                            .add("police_check_timed_out", 0)
                            .add("police_check_disqualified", 1),
                        new ReportLinkedMap<String, Object>()
                            .add("pool_number_jp", "200000002")
                            .add("police_check_responded", 3)
                            .add("police_check_submitted", 3)
                            .add("police_check_complete", 0)
                            .add("police_check_timed_out", 0)
                            .add("police_check_disqualified", 0),
                        new ReportLinkedMap<String, Object>()
                            .add("pool_number_jp", "200000003")
                            .add("police_check_responded", 2)
                            .add("police_check_submitted", 1)
                            .add("police_check_complete", 1)
                            .add("police_check_timed_out", 1)
                            .add("police_check_disqualified", 0))
                )
            );
    }

    @Test
    void positiveNotFound() {
        StandardReportRequest request = getValidPayload();
        request.setFromDate(LocalDate.of(2025, 1, 1));
        request.setToDate(LocalDate.of(2025, 1, 30));
        testBuilder()
            .payload(request)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(
                buildResponse("2025-01-01", "2025-01-30",
                    StandardTableData.of()
                )
            );
    }

    @Test
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getCourtJwt("415"))
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private StandardReportResponse buildResponse(String fromDate, String toDate, StandardTableData data) {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date from")
                    .dataType("LocalDate")
                    .value(fromDate)
                    .build())
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date to")
                    .dataType("LocalDate")
                    .value(toDate)
                    .build())
            )
            .tableData(
                AbstractReportResponse.TableData.<StandardTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("pool_number_jp")
                            .name("Pool Number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("police_check_responded")
                            .name("Responded jurors")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("police_check_submitted")
                            .name("Checks submitted")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("police_check_complete")
                            .name("Checks completed")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("police_check_timed_out")
                            .name("Checks timed out")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("police_check_disqualified")
                            .name("Jurors disqualified")
                            .dataType("Long")
                            .headings(null)
                            .build()))
                    .data(data)
                    .build())
            .build();
    }
}
