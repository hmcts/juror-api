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
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
            .toDate(LocalDate.of(2024, 7, 31))
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
                .headings(getResponseHeadings(24))
                .tableData(AbstractReportResponse.TableData.<GroupedTableData>builder()
                    .headings(getListOfTableHeadings())
                    .data(getTypicalResponseData())
                    .build())
            .build());
    }

    @Test
    void negativeUnauthorisedBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    @Test
    void negativeInvalidPayloadFromDateMissing() {
        StandardReportRequest request = getValidPayload();
        request.setFromDate(null);

        testBuilder()
            .payload(request)
            .triggerInvalid()
            .assertInvalidPathParam("fromDate: must not be null");
    }

    @Test
    void negativeInvalidPayloadToDateMissing() {
        StandardReportRequest request = getValidPayload();
        request.setToDate(null);

        testBuilder()
            .payload(request)
            .triggerInvalid()
            .assertInvalidPathParam("toDate: must not be null");
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
            .displayName("Court Name")
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
                .id("appearance_pool_number")
                .name("Pool Number")
                .dataType("String")
                .headings(null)
                .build(),
            StandardReportResponse.TableData.Heading.builder()
                .id("appearance_trial_number")
                .name("Trial Number")
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
                .build()
        );
    }

    private GroupedTableData getTypicalResponseData() {
        String today = DateTimeFormatter.ISO_DATE.format(LocalDate.of(2024, 5, 13));
        String yesterday = DateTimeFormatter.ISO_DATE.format(LocalDate.of(2024, 5, 12));

        return new GroupedTableData()
            .add(today.concat(",").concat("CROWN COURT"), List.of(
                getTableDataItem("041500001", "CName1", "CSurname1", "415240101", "111111", "09:00:00", "17:30:00"),
                getTableDataItem("041500004", "CName4", "CSurname4", "415240101", "111111", "09:00:00", "17:30:00"),
                getTableDataItem("041500005", "CName5", "CSurname5", "415240101", "111111", "09:00:00", "17:30:00"),
                getTableDataItem("041500006", "CName6", "CSurname6", "415240101", null, "09:00:00", null),
                getTableDataItem("041500007", "CName7", "CSurname7", "415240101", "111111", "09:00:00", null),
                getTableDataItem("041500009", "CName9", "CSurname9", "415240101", "111111", "09:00:00", "17:30:00"),
                getTableDataItem("041500017", "CName17", "CSurname17", "415240103", "111111", "09:00:00", null),
                getTableDataItem("041500018", "CName18", "CSurname18", "415240103", "111111", "09:00:00", null),
                getTableDataItem("041500019", "CName19", "CSurname19", "415240103", "111111", "09:00:00", null),
                getTableDataItem("041500020", "CName20", "CSurname20", "415240103", "111111", "09:00:00", null)
            ))
            .add(today.concat(",").concat("CIVIL COURT"), List.of(
                getTableDataItem("041500011", "CName11", "CSurname11", "415240102", "111111", "09:00:00", "17:30:00"),
                getTableDataItem("041500013", "CName13", "CSurname13", "415240102", "111111", "09:00:00", null),
                getTableDataItem("041500014", "CName14", "CSurname14", "415240102", "111111", "09:00:00", null)
            ))
            .add(yesterday.concat(",").concat("CROWN COURT"), List.of(
                getTableDataItem("041500001", "CName1", "CSurname1", "415240101", "111111", "09:30:00", "17:30:00"),
                getTableDataItem("041500002", "CName2", "CSurname2", "415240101", "111111", "08:00:00", "17:30:00"),
                getTableDataItem("041500003", "CName3", "CSurname3", "415240101", "111111", "09:00:00", "17:30:00"),
                getTableDataItem("041500004", "CName4", "CSurname4", "415240101", "111111", "09:00:00", "17:30:00"),
                getTableDataItem("041500017", "CName17", "CSurname17", "415240103", "111111", "11:20:00", "17:30:00"),
                getTableDataItem("041500018", "CName18", "CSurname18", "415240103", "111111", "09:00:00", "17:30:00"),
                getTableDataItem("041500019", "CName19", "CSurname19", "415240103", "111111", "09:00:00", "17:30:00"),
                getTableDataItem("041500020", "CName20", "CSurname20", "415240103", "111111", "09:00:00", "17:30:00")
            ))
            .add(yesterday.concat(",").concat("CIVIL COURT"), List.of(
                getTableDataItem("041500011", "CName11", "CSurname11", "415240102", "111111", "09:30:00", "17:30:00"),
                getTableDataItem("041500012", "CName12", "CSurname12", "415240102", "111111", "09:30:00", "17:30:00"),
                getTableDataItem("041500013", "CName13", "CSurname13", "415240102", "111111", "06:30:00", "17:30:00")
            ));
    }

    private ReportLinkedMap<String, Object> getTableDataItem(
        String jurorNumber,
        String firstName,
        String lastName,
        String poolNumber,
        String trialNumber,
        String checkedIn,
        String checkedOut) {

        ReportLinkedMap<String, Object> map = new ReportLinkedMap<String, Object>()
            .add("juror_number", jurorNumber)
            .add("first_name", firstName)
            .add("last_name", lastName)
            .add("appearance_pool_number", poolNumber);

        if (trialNumber != null) {
            map.add("appearance_trial_number", trialNumber);
        }
        map.add("appearance_checked_in", checkedIn);
        if (checkedOut != null) {
            map.add("appearance_checked_out", checkedOut);
        }

        return map;
    }
}
