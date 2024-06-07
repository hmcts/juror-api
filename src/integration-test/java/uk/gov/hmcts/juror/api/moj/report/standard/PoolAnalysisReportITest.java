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
import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/mod/reports/PoolAnalysisReportITest_Typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class PoolAnalysisReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    public PoolAnalysisReportITest(TestRestTemplate template) {
        super(template, PoolAnalysisReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2024, 1, 15))
            .toDate(LocalDate.of(2024, 1, 16))
            .build());
    }

    @Test
    void positiveNoDataCourtOwner() {
        StandardReportRequest request = getValidPayload();
        request.setFromDate(LocalDate.of(2024, 1, 17));
        request.setToDate(LocalDate.of(2024, 1, 18));

        testBuilder()
            .payload(addReportType(request))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(StandardReportResponse.builder()
                .headings(getResponseHeadings("CHESTER (415)", "2024-01-17", "2024-01-18"))
                .tableData(StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(getTableHeadings())
                    .data(new StandardTableData()).build())
                .build());
    }

    @Test
    void positiveTypicalCourtOwner() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(StandardReportResponse.builder()
                .headings(getResponseHeadings("CHESTER (415)", "2024-01-15", "2024-01-16"))
                .tableData(StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(getTableHeadings())
                    .data(getCourtOwnerTableData()).build())
                .build());
    }

    @Test
    void positiveTypicalCourtSatellite() {
        testBuilder()
            .jwt(getSatelliteCourtJwt("415", "767"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(StandardReportResponse.builder()
                .headings(getResponseHeadings("KNUTSFORD (767)", "2024-01-15", "2024-01-16"))
                .tableData(StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(getTableHeadings())
                    .data(new StandardTableData(List.of(
                        getCourtSatelliteTableData())
                    )).build())
                .build());
    }

    @Test
    void positiveTypicalBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(StandardReportResponse.builder()
                .headings(getResponseHeadings(null, "2024-01-15", "2024-01-16"))
                .tableData(StandardReportResponse.TableData.<StandardTableData>builder()
                    .headings(getTableHeadings())
                    .data(getBureauTableData()).build())
                .build());
    }

    private ReportHashMap<String, StandardReportResponse.DataTypeValue> getResponseHeadings(
        String courtName,
        String fromDate,
        String toDate) {

        ReportHashMap<String, StandardReportResponse.DataTypeValue> headingsMap = new ReportHashMap<>();

        headingsMap.add("date_from", StandardReportResponse.DataTypeValue.builder()
            .displayName("Date From")
            .dataType("LocalDate")
            .value(fromDate)
            .build());
        headingsMap.add("date_to", StandardReportResponse.DataTypeValue.builder()
            .displayName("Date To")
            .dataType("LocalDate")
            .value(toDate)
            .build());

        if (courtName != null) {
            headingsMap.add("court_name", StandardReportResponse.DataTypeValue.builder()
                .displayName("Court Name")
                .dataType("String")
                .value(courtName)
                .build());
        }

        return headingsMap;
    }

    private List<StandardReportResponse.TableData.Heading> getTableHeadings() {
        return List.of(
            StandardReportResponse.TableData.Heading.builder()
                .id("pool_number_by_jp")
                .name("Pool Number")
                .dataType("String")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("service_start_date")
                .name("Service Start Date")
                .dataType("LocalDate")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("jurors_summoned_total")
                .name("Summoned")
                .dataType("Long")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("responded_total")
                .name("Responded")
                .dataType("Integer")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("attended_total")
                .name("Attended")
                .dataType("Integer")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("panel_total")
                .name("Panel")
                .dataType("Integer")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("juror_total")
                .name("Juror")
                .dataType("Integer")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("excused_total")
                .name("Excused")
                .dataType("Integer")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("disqualified_total")
                .name("Disqualified")
                .dataType("Integer")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("deferred_total")
                .name("Deferred")
                .dataType("Integer")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("reassigned_total")
                .name("Reassigned")
                .dataType("Integer")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("undeliverable_total")
                .name("Undeliverable")
                .dataType("Integer")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("transferred_total")
                .name("Transferred")
                .dataType("Integer")
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("failed_to_attend_total")
                .name("FTA")
                .dataType("Integer")
                .build()
        );
    }

    private StandardTableData getCourtOwnerTableData() {
        return new StandardTableData(
            List.of(
                new ReportLinkedMap<String, Object>()
                    .add("pool_number_by_jp", "415240102")
                    .add("service_start_date", "2024-01-15")
                    .add("jurors_summoned_total", 18)
                    .add("responded_total", 5)
                    .add("attended_total", 2)
                    .add("panel_total", 0)
                    .add("juror_total", 0)
                    .add("excused_total", 1)
                    .add("disqualified_total", 1)
                    .add("deferred_total", 3)
                    .add("reassigned_total", 1)
                    .add("undeliverable_total", 2)
                    .add("transferred_total", 4)
                    .add("failed_to_attend_total", 1)
                    .add("responded_total_percentage", 27.77778)
                    .add("attended_total_percentage", 11.11111)
                    .add("panel_total_percentage", 0.0)
                    .add("juror_total_percentage", 0.0)
                    .add("excused_total_percentage", 5.55556)
                    .add("disqualified_total_percentage", 5.55556)
                    .add("deferred_total_percentage", 16.66667)
                    .add("reassigned_total_percentage", 5.55556)
                    .add("undeliverable_total_percentage", 11.11111)
                    .add("transferred_total_percentage", 22.22222)
                    .add("failed_to_attend_total_percentage", 5.55556),
                getCourtSatelliteTableData()
            )
        );
    }

    private ReportLinkedMap<String, Object> getCourtSatelliteTableData() {
        return new ReportLinkedMap<String, Object>()
            .add("pool_number_by_jp", "767240101")
            .add("service_start_date", "2024-01-16")
            .add("jurors_summoned_total", 5)
            .add("responded_total", 2)
            .add("attended_total", 2)
            .add("panel_total", 1)
            .add("juror_total", 1)
            .add("excused_total", 0)
            .add("disqualified_total", 0)
            .add("deferred_total", 0)
            .add("reassigned_total", 0)
            .add("undeliverable_total", 0)
            .add("transferred_total", 0)
            .add("failed_to_attend_total", 0)
            .add("responded_total_percentage", 40.0)
            .add("attended_total_percentage", 40.0)
            .add("panel_total_percentage", 20.0)
            .add("juror_total_percentage", 20.0)
            .add("excused_total_percentage", 0.0)
            .add("disqualified_total_percentage", 0.0)
            .add("deferred_total_percentage", 0.0)
            .add("reassigned_total_percentage", 0.0)
            .add("undeliverable_total_percentage", 0.0)
            .add("transferred_total_percentage", 0.0)
            .add("failed_to_attend_total_percentage", 0.0);
    }

    private StandardTableData getBureauTableData() {
        return new StandardTableData(List.of(
            new ReportLinkedMap<String, Object>()
                .add("pool_number_by_jp", "415240101")
                .add("service_start_date", "2024-01-15")
                .add("jurors_summoned_total", 18)
                .add("responded_total", 5)
                .add("attended_total", 0)
                .add("panel_total", 0)
                .add("juror_total", 0)
                .add("excused_total", 1)
                .add("disqualified_total", 1)
                .add("deferred_total", 3)
                .add("reassigned_total", 1)
                .add("undeliverable_total", 2)
                .add("transferred_total", 4)
                .add("failed_to_attend_total", 1)
                .add("responded_total_percentage", 27.77778)
                .add("attended_total_percentage", 0.0)
                .add("panel_total_percentage", 0.0)
                .add("juror_total_percentage", 0.0)
                .add("excused_total_percentage", 5.55556)
                .add("disqualified_total_percentage", 5.55556)
                .add("deferred_total_percentage", 16.66667)
                .add("reassigned_total_percentage", 5.55556)
                .add("undeliverable_total_percentage", 11.11111)
                .add("transferred_total_percentage", 22.22222)
                .add("failed_to_attend_total_percentage", 5.55556)
        ));
    }
}
