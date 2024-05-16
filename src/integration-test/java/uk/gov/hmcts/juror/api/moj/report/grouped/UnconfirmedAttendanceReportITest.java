package uk.gov.hmcts.juror.api.moj.report.grouped;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;

import java.time.LocalDate;
import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/UnconfirmedAttendanceReportITest_typical.sql"
})
@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.JUnitTestsShouldIncludeAssert"
})
class UnconfirmedAttendanceReportITest extends AbstractGroupedReportControllerITest {

    @Autowired
    public UnconfirmedAttendanceReportITest(TestRestTemplate template) {
        super(template, UnconfirmedAttendanceReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2024, 5, 1))
            .toDate(LocalDate.of(2024, 5, 31))
            .build());
    }

    @Test
    void positiveNoData() {
        StandardReportRequest request = getValidPayload();
        request.setFromDate(LocalDate.now().minusDays(15));
        request.setToDate(LocalDate.now().minusDays(10));

        testBuilder()
            .payload(addReportType(request))
            .jwt(getValidJwt())
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(GroupedReportResponse.builder()
                .groupBy(getTypicalGroupBy())
                .headings(getResponseHeadings(0))
                .tableData(AbstractReportResponse.TableData.<GroupedTableData>builder()
                    .headings(getListOfTableHeadings())
                    .data(new GroupedTableData())
                    .build())
                .build());
    }

    @Test
    void positiveTypical() {
        testBuilder()
            .payload(getValidPayload())
            .jwt(getValidJwt())
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(GroupedReportResponse.builder()
                .groupBy(getTypicalGroupBy())
                .headings(getResponseHeadings(20))
                .tableData(AbstractReportResponse.TableData.<GroupedTableData>builder()
                    .headings(getListOfTableHeadings())
                    .data(new GroupedTableData())
                    .build())
                .build());
    }

    public GroupByResponse getTypicalGroupBy() {
        return GroupByResponse.builder()
            .name(DataType.APPEARANCE_DATE_AND_POOL_TYPE.name())
            .nested(null)
            .build();
    }

    private ReportHashMap<String, StandardReportResponse.DataTypeValue> getResponseHeadings(int attendances) {
        ReportHashMap<String, StandardReportResponse.DataTypeValue> headingsMap = new ReportHashMap<>();

        headingsMap.add("total_unconfirmed_attendances", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total unconfirmed attendances")
            .dataType("Long")
            .value(attendances)
            .build());
        headingsMap.add("court_name", StandardReportResponse.DataTypeValue.builder()
            .displayName("Court name")
            .dataType("String")
            .value("CHESTER (415)")
            .build());

        return headingsMap;
    }

    private List<AbstractReportResponse.TableData.Heading> getListOfTableHeadings() {
        return List.of(
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
                .id("pool_number")
                .name("Pool Number")
                .dataType("String")
                .headings(null)
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("trial_number")
                .name("Trial Number")
                .dataType("String")
                .headings(null)
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("checked_in")
                .name("Checked in")
                .dataType("LocalTime")
                .headings(null)
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("checked_out")
                .name("Checked out")
                .dataType("LocalTime")
                .headings(null)
                .build()
        );
    }

//    private GroupedReportResponse getTypicalResponse() {
//        return GroupedReportResponse.builder()
//                   .groupBy(getTypicalGroupByResponse())
//                   .headings(getResponseHeadings(1, "CHESTER (415)"))
//    }
}
