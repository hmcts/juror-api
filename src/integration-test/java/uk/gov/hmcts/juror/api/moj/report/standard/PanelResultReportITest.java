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
    "/db/administration/createCourtRooms.sql",
    "/db/administration/createJudges.sql",
    "/db/mod/reports/TrialStatisticsReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class PanelResultReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    public PanelResultReportITest(TestRestTemplate template) {
        super(template, PanelResultReport.class);
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

    @Test
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardReportResponse(StandardTableData.of(
                new ReportLinkedMap<String, Object>()
                    .add("trial_number", "T123")
                    .add("trial_type", "CIV")
                    .add("trial_jurors_count", 6)
                    .add("trial_jurors_not_used", 4)
                    .add("trial_length_in_days", 24),
                new ReportLinkedMap<String, Object>()
                    .add("trial_number", "T1234")
                    .add("trial_type", "CIV")
                    .add("trial_jurors_count", 3)
                    .add("trial_jurors_not_used", 0)
                    .add("trial_length_in_days", 10),
                new ReportLinkedMap<String, Object>()
                    .add("trial_number", "T1235")
                    .add("trial_type", "CIV")
                    .add("trial_jurors_count", 4)
                    .add("trial_jurors_not_used", 1)
                    .add("trial_length_in_days", 22),
                new ReportLinkedMap<String, Object>()
                    .add("trial_number", "T1236")
                    .add("trial_type", "CRI")
                    .add("trial_jurors_count", 2)
                    .add("trial_jurors_not_used", 1)
                    .add("trial_length_in_days", 15))));
    }

    @Test
    void positiveNotFound() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardReportResponse(new StandardTableData()));
    }

    @Test
    void negativeUnauthorisedBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private StandardReportResponse buildStandardReportResponse(StandardTableData tableData) {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date to")
                    .dataType("LocalDate")
                    .value("2024-01-30")
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
                            .id("trial_number")
                            .name("Trial number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("trial_type")
                            .name("Trial Type")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("trial_jurors_count")
                            .name("Jurors")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("trial_jurors_not_used")
                            .name("Not used")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("trial_length_in_days")
                            .name("Trial length in days")
                            .dataType("Long")
                            .headings(null)
                            .build()))
                    .data(tableData)
                    .build())
            .build();
    }
}