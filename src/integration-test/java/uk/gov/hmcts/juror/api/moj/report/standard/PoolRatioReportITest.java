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
    "/db/mod/reports/PoolRatioReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class PoolRatioReportITest extends AbstractStandardReportControllerITest {
    protected static final LocalDate DEFAULT_FROM_DATE = LocalDate.of(2024, 1, 1);
    protected static final LocalDate DEFAULT_TO_DATE = LocalDate.of(2024, 1, 30);
    protected static final List<String> DEFAULT_COURTS = List.of("415", "414", "413");

    @Autowired
    PoolRatioReportITest(TestRestTemplate template) {
        super(template, PoolRatioReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getBureauJwt();
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(DEFAULT_FROM_DATE)
            .toDate(DEFAULT_TO_DATE)
            .courts(DEFAULT_COURTS)
            .build());
    }

    @Test
    void positiveTypicalBureau() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(buildStandardReportResponse(StandardTableData.of(
                new ReportLinkedMap<String, Object>()
                    .add("court_location_name_and_code", "CHESTER (415)")
                    .add("total_requested", 15)
                    .add("total_deferred", 4)
                    .add("total_summoned", 12)
                    .add("total_supplied", 12)
                    .add("ratio_1", 0.7272727272727273)
                    .add("ratio_2", 1.0),
                new ReportLinkedMap<String, Object>()
                    .add("court_location_name_and_code", "Chelmsford  (414)")
                    .add("total_requested", 15)
                    .add("total_deferred", 5)
                    .add("total_summoned", 10)
                    .add("total_supplied", 8)
                    .add("ratio_1", 0.5)
                    .add("ratio_2", 1.6666666666666667),
                new ReportLinkedMap<String, Object>()
                    .add("court_location_name_and_code", "The Central Criminal Court (413)")
                    .add("total_requested", 10)
                    .add("total_deferred", 1)
                    .add("total_summoned", 9)
                    .add("total_supplied", 6)
                    .add("ratio_1", 0.8888888888888888)
                    .add("ratio_2", 1.6))));
    }


    @Test
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getCourtJwt("414"))
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
                            .id("court_location_name_and_code_jp")
                            .name("Court Location Name And Code")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_requested")
                            .name("Requested")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_deferred")
                            .name("Deferred")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_summoned")
                            .name("Summoned")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("total_supplied")
                            .name("Supplied")
                            .dataType("Long")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("ratio_1")
                            .name("Ratio 1")
                            .dataType("Double")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("ratio_2")
                            .name("Ratio 2")
                            .dataType("Double")
                            .headings(null)
                            .build()))
                    .data(tableData)
                    .build())
            .build();
    }
}
