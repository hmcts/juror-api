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
import java.util.List;

import static uk.gov.hmcts.juror.api.TestConstants.COURT_NAME_DISPLAY_NAME;
import static uk.gov.hmcts.juror.api.TestConstants.COURT_NAME_KEY;
import static uk.gov.hmcts.juror.api.TestConstants.DATE_FROM_DISPLAY_NAME;
import static uk.gov.hmcts.juror.api.TestConstants.DATE_FROM_KEY;
import static uk.gov.hmcts.juror.api.TestConstants.DATE_TO_DISPLAY_NAME;
import static uk.gov.hmcts.juror.api.TestConstants.DATE_TO_KEY;
import static uk.gov.hmcts.juror.api.TestConstants.FIRST_NAME_DISPLAY_NAME;
import static uk.gov.hmcts.juror.api.TestConstants.FIRST_NAME_KEY;
import static uk.gov.hmcts.juror.api.TestConstants.JUROR_NUMBER_KEY;
import static uk.gov.hmcts.juror.api.TestConstants.LAST_NAME_DISPLAY_NAME;
import static uk.gov.hmcts.juror.api.TestConstants.LAST_NAME_KEY;
import static uk.gov.hmcts.juror.api.TestConstants.VALID_COURT_LOCATION;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/mod/reports/CompletionOfServiceReportITest_typical.sql"
})
@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert"//False positive
})
class CompletionOfServiceReportITest extends AbstractGroupedReportControllerITest {

    public static final String COMPLETION_DATE_KEY = "completion_date";

    @Autowired
    public CompletionOfServiceReportITest(TestRestTemplate template) {
        super(template, CompletionOfServiceReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt(VALID_COURT_LOCATION);
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2023, 1, 1))
            .toDate(LocalDate.of(2023, 2, 1))
            .build());
    }

    @Test
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(createResponse("CHESTER (415)", 8,
                true));
    }

    @Test
    void positiveNoCompletionDataFoundForLocation() {
        testBuilder()
            .jwt(getCourtJwt("417"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(createResponse("COVENTRY (417)", 0, false));
    }

    @Test
    void negativeTypicalIsBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    @Test
    void negativeInvalidPayloadMissingDate() {
        StandardReportRequest request = getValidPayload();
        request.setToDate(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .assertInvalidPathParam("toDate: must not be null");
    }

    private GroupedReportResponse createResponse(String courtLocation, int totalPoolMembersCompleted,
                                                 boolean populate) {
        return GroupedReportResponse.builder()
            .groupBy(GroupByResponse.builder().name(DataType.POOL_NUMBER_AND_COURT_TYPE.name()).build())
            .headings(new ReportHashMap<String, AbstractReportResponse.DataTypeValue>()
                .add("total_pool_members_completed", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Total pool members completed")
                    .dataType("Long")
                    .value(totalPoolMembersCompleted)
                    .build())
                .add(DATE_TO_KEY, StandardReportResponse.DataTypeValue.builder()
                    .displayName(DATE_TO_DISPLAY_NAME)
                    .dataType("LocalDate")
                    .value("2023-02-01")
                    .build())
                .add(DATE_FROM_KEY, StandardReportResponse.DataTypeValue.builder()
                    .displayName(DATE_FROM_DISPLAY_NAME)
                    .dataType("LocalDate")
                    .value("2023-01-01")
                    .build())
                .add(COURT_NAME_KEY, StandardReportResponse.DataTypeValue.builder()
                    .displayName(COURT_NAME_DISPLAY_NAME)
                    .dataType("String")
                    .value(courtLocation)
                    .build()))
            .tableData(
                AbstractReportResponse.TableData.<GroupedTableData>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id(JUROR_NUMBER_KEY)
                            .name("Juror Number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id(FIRST_NAME_KEY)
                            .name(FIRST_NAME_DISPLAY_NAME)
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id(LAST_NAME_KEY)
                            .name(LAST_NAME_DISPLAY_NAME)
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id(COMPLETION_DATE_KEY)
                            .name("Completion date")
                            .dataType("LocalDate")
                            .headings(null)
                            .build()))
                    .data(createMockTableData(populate))
                    .build())
            .build();
    }

    private GroupedTableData createMockTableData(boolean populate) {
        if (populate) {
            return new GroupedTableData()
                .add("416220901,CROWN COURT", List.of(
                    new ReportLinkedMap<String, Object>()
                        .add(JUROR_NUMBER_KEY, "415000001")
                        .add(FIRST_NAME_KEY, "FIRSTNAMEONE")
                        .add(LAST_NAME_KEY, "LASTNAMEONE")
                        .add(COMPLETION_DATE_KEY, "2023-01-01"),
                    new ReportLinkedMap<String, Object>()
                        .add(JUROR_NUMBER_KEY, "415000002")
                        .add(FIRST_NAME_KEY, "FIRSTNAMETWO")
                        .add(LAST_NAME_KEY, "LASTNAMETWO")
                        .add(COMPLETION_DATE_KEY, "2023-01-30"),
                    new ReportLinkedMap<String, Object>()
                        .add(JUROR_NUMBER_KEY, "415000003")
                        .add(FIRST_NAME_KEY, "FIRSTNAMETHREE")
                        .add(LAST_NAME_KEY, "LASTNAMETHREE")
                        .add(COMPLETION_DATE_KEY, "2023-01-28"),
                    new ReportLinkedMap<String, Object>()
                        .add(JUROR_NUMBER_KEY, "415000004")
                        .add(FIRST_NAME_KEY, "FIRSTNAMEFOUR")
                        .add(LAST_NAME_KEY, "LASTNAMEFOUR")
                        .add(COMPLETION_DATE_KEY, "2023-01-30"),
                    new ReportLinkedMap<String, Object>()
                        .add(JUROR_NUMBER_KEY, "415000005")
                        .add(FIRST_NAME_KEY, "FIRSTNAMEFIVE")
                        .add(LAST_NAME_KEY, "LASTNAMEFIVE")
                        .add(COMPLETION_DATE_KEY, "2023-01-30")))
                .add("416220902,CROWN COURT", List.of(
                    new ReportLinkedMap<>()
                        .add(JUROR_NUMBER_KEY, "415000004")
                        .add(FIRST_NAME_KEY, "FIRSTNAMEFOUR")
                        .add(LAST_NAME_KEY, "LASTNAMEFOUR")
                        .add(COMPLETION_DATE_KEY, "2023-01-30"),
                    new ReportLinkedMap<String, Object>()
                        .add(JUROR_NUMBER_KEY, "415000006")
                        .add(FIRST_NAME_KEY, "FIRSTNAMESIX")
                        .add(LAST_NAME_KEY, "LASTNAMESIX")
                        .add(COMPLETION_DATE_KEY, "2023-02-01"),
                    new ReportLinkedMap<String, Object>()
                        .add(JUROR_NUMBER_KEY, "415000007")
                        .add(FIRST_NAME_KEY, "FIRSTNAMESEVEN")
                        .add(LAST_NAME_KEY, "LASTNAMESEVEN")
                        .add(COMPLETION_DATE_KEY, "2023-01-30")));

        } else {
            return new GroupedTableData();
        }
    }
}
