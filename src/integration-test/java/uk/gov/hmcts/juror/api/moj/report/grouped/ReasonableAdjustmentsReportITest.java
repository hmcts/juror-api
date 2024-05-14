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
import uk.gov.hmcts.juror.api.moj.report.*;

import java.time.LocalDate;
import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/ReasonableAdjustmentsReportITest_typical.sql"
})
@SuppressWarnings("PMD.LawOfDemeter")
class ReasonableAdjustmentsReportITest extends AbstractGroupedReportControllerITest {

    @Autowired
    public ReasonableAdjustmentsReportITest(TestRestTemplate template) {
        super(template, ReasonableAdjustmentsReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2024, 1, 1))
            .toDate(LocalDate.of(2024, 1, 2))
            .build());
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void positiveTypicalCourt() {
        testBuilder()
            .jwt(getCourtJwt("415"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponseCourt());
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void positiveTypicalBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponseBureau());
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void positiveNotFound() {
        testBuilder()
            .jwt(getCourtJwt("408"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(GroupedReportResponse.builder()
                .groupBy(getTypicalGroupByResponse())
                .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_reasonable_adjustments", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total jurors with reasonable adjustments")
                    .dataType("Long")
                    .value(0)
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court name")
                    .dataType("String")
                    .value("BRISTOL (408)")
                    .build()))
                .tableData(
                    AbstractReportResponse.TableData.<GroupedTableData>builder()
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
                                .id("pool_number_by_jp")
                                .name("Pool Number")
                                .dataType("String")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("contact_details")
                                .name("Contact Details")
                                .dataType("List")
                                .headings(List.of(
                                    StandardReportResponse.TableData.Heading.builder()
                                        .id("main_phone")
                                        .name("Main Phone")
                                        .dataType("String")
                                        .headings(null)
                                        .build(),
                                    StandardReportResponse.TableData.Heading.builder()
                                        .id("other_phone")
                                        .name("Other Phone")
                                        .dataType("String")
                                        .headings(null)
                                        .build(),
                                    StandardReportResponse.TableData.Heading.builder()
                                        .id("work_phone")
                                        .name("Work Phone")
                                        .dataType("String")
                                        .headings(null)
                                        .build(),
                                    StandardReportResponse.TableData.Heading.builder()
                                        .id("email")
                                        .name("Email")
                                        .dataType("String")
                                        .headings(null)
                                        .build())
                                ).build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("next_attendance_date")
                                .name("Next attendance")
                                .dataType("LocalDate")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("juror_reasonable_adjustment_with_message")
                                .name("Reasonable Adjustments")
                                .dataType("List")
                                .headings(List.of(
                                    StandardReportResponse.TableData.Heading.builder()
                                        .id("reasonable_adjustment_code_with_description")
                                        .name("Reasonable Adjustment Code With Description")
                                        .dataType("String")
                                        .headings(null)
                                        .build(),
                                    StandardReportResponse.TableData.Heading.builder()
                                        .id("juror_reasonable_adjustment_message")
                                        .name("Juror Reasonable Adjustment Message")
                                        .dataType("String")
                                        .headings(null)
                                        .build())
                                ).build()))
                        .data(new GroupedTableData())
                        .build())
                    .build());
    }

    @Test
    void negativeInvalidPayloadFromDateMissing() {
        StandardReportRequest request = getValidPayload();
        request.setFromDate(null);
        testBuilder()
            .jwt(getBureauJwt())
            .payload(request)
            .triggerInvalid()
            .assertInvalidPathParam("fromDate: must not be null");
    }

    @Test
    void negativeInvalidPayloadToDateMissing() {
        StandardReportRequest request = getValidPayload();
        request.setToDate(null);
        testBuilder()
            .jwt(getBureauJwt())
            .payload(request)
            .triggerInvalid()
            .assertInvalidPathParam("toDate: must not be null");
    }

    private GroupedReportResponse getTypicalResponseCourt() {
        return GroupedReportResponse.builder()
            .groupBy(getTypicalGroupByResponse())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
            .add("total_reasonable_adjustments", StandardReportResponse.DataTypeValue.builder()
                .displayName("Total jurors with reasonable adjustments")
                .dataType("Long")
                .value(1)
                .build())
            .add("court_name", StandardReportResponse.DataTypeValue.builder()
                .displayName("Court name")
                .dataType("String")
                .value("CHESTER (415)")
                .build()))
            .tableData(
                AbstractReportResponse.TableData.<GroupedTableData>builder()
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
                            .id("pool_number_by_jp")
                            .name("Pool Number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("contact_details")
                            .name("Contact Details")
                            .dataType("List")
                            .headings(List.of(
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("main_phone")
                                    .name("Main Phone")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("other_phone")
                                    .name("Other Phone")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("work_phone")
                                    .name("Work Phone")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("email")
                                    .name("Email")
                                    .dataType("String")
                                    .headings(null)
                                    .build())
                            ).build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("next_attendance_date")
                            .name("Next attendance")
                            .dataType("LocalDate")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("juror_reasonable_adjustment_with_message")
                            .name("Reasonable Adjustments")
                            .dataType("List")
                            .headings(List.of(
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("reasonable_adjustment_code_with_description")
                                    .name("Reasonable Adjustment Code With Description")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("juror_reasonable_adjustment_message")
                                    .name("Juror Reasonable Adjustment Message")
                                    .dataType("String")
                                    .headings(null)
                                    .build())
                            ).build()))
                    .data(new GroupedTableData()
                              .add("CHESTER (415)", List.of(
                                  new ReportLinkedMap<String, Object>()
                                      .add("juror_number", "041500005")
                                      .add("first_name", "CName5")
                                      .add("last_name", "CSurname5")
                                      .add("pool_number_by_jp", "415240101")
                                      .add("contact_details", new ReportLinkedMap<String, Object>()
                                          .add("main_phone", "400000001")
                                          .add("other_phone", "400000002")
                                          .add("work_phone", "400000003")
                                          .add("email", "041500005@email.gov.uk")
                                      )
                                      .add("next_attendance_date", "2024-01-01")
                                      .add("juror_reasonable_adjustment_with_message", new ReportLinkedMap<String, Object>()
                                          .add("reasonable_adjustment_code_with_description", "M - MULTIPLE")
                                          .add("juror_reasonable_adjustment_message", "multiple requests")
                                      )
                              ))).build())
            .build();
    }

    private GroupedReportResponse getTypicalResponseBureau() {
        return GroupedReportResponse.builder()
            .groupBy(getTypicalGroupByResponse())
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_reasonable_adjustments", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total jurors with reasonable adjustments")
                    .dataType("Long")
                    .value(7)
                    .build()))
            .tableData(
                AbstractReportResponse.TableData.<GroupedTableData>builder()
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
                            .id("pool_number_by_jp")
                            .name("Pool Number")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("contact_details")
                            .name("Contact Details")
                            .dataType("List")
                            .headings(List.of(
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("main_phone")
                                    .name("Main Phone")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("other_phone")
                                    .name("Other Phone")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("work_phone")
                                    .name("Work Phone")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("email")
                                    .name("Email")
                                    .dataType("String")
                                    .headings(null)
                                    .build())
                            ).build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("next_attendance_date")
                            .name("Next attendance")
                            .dataType("LocalDate")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("juror_reasonable_adjustment_with_message")
                            .name("Reasonable Adjustments")
                            .dataType("List")
                            .headings(List.of(
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("reasonable_adjustment_code_with_description")
                                    .name("Reasonable Adjustment Code With Description")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("juror_reasonable_adjustment_message")
                                    .name("Juror Reasonable Adjustment Message")
                                    .dataType("String")
                                    .headings(null)
                                    .build())
                            ).build()))
                    .data(new GroupedTableData()
                        .add("CHESTER (415)", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "041500001")
                                .add("first_name", "CName1")
                                .add("last_name", "CSurname1")
                                .add("pool_number_by_jp", "415240101")
                                .add("contact_details", new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "000000001")
                                    .add("other_phone", "000000002")
                                    .add("work_phone", "000000003")
                                    .add("email", "041500001@email.gov.uk")
                                )
                                .add("next_attendance_date", "2024-01-01")
                                .add("juror_reasonable_adjustment_with_message", new ReportLinkedMap<String, Object>()
                                    .add("reasonable_adjustment_code_with_description", "M - MULTIPLE")
                                    .add("juror_reasonable_adjustment_message", "multiple requests")
                                ),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "041500002")
                                .add("first_name", "CName2")
                                .add("last_name", "CSurname2")
                                .add("pool_number_by_jp", "415240101")
                                .add("contact_details", new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "100000001")
                                    .add("other_phone", "100000002")
                                    .add("work_phone", "100000003")
                                    .add("email", "041500002@email.gov.uk")
                                )
                                .add("next_attendance_date", "2024-01-01")
                                .add("juror_reasonable_adjustment_with_message", new ReportLinkedMap<String, Object>()
                                    .add("reasonable_adjustment_code_with_description", "M - MULTIPLE")
                                    .add("juror_reasonable_adjustment_message", "multiple requests")
                                ),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "041500003")
                                .add("first_name", "CName3")
                                .add("last_name", "CSurname3")
                                .add("pool_number_by_jp", "415240101")
                                .add("contact_details", new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "200000001")
                                    .add("other_phone", "200000002")
                                    .add("work_phone", "200000003")
                                    .add("email", "041500003@email.gov.uk")
                                )
                                .add("next_attendance_date", "2024-01-01")
                                .add("juror_reasonable_adjustment_with_message", new ReportLinkedMap<String, Object>()
                                    .add("reasonable_adjustment_code_with_description", "M - MULTIPLE")
                                    .add("juror_reasonable_adjustment_message", "multiple requests")
                                ),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "041500004")
                                .add("first_name", "CName4")
                                .add("last_name", "CSurname4")
                                .add("pool_number_by_jp", "415240101")
                                .add("contact_details", new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "300000001")
                                    .add("other_phone", "300000002")
                                    .add("work_phone", "300000003")
                                    .add("email", "041500004@email.gov.uk")
                                )
                                .add("next_attendance_date", "2024-01-01")
                                .add("juror_reasonable_adjustment_with_message", new ReportLinkedMap<String, Object>()
                                    .add("reasonable_adjustment_code_with_description", "M - MULTIPLE")
                                    .add("juror_reasonable_adjustment_message", "multiple requests")
                                ),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "041500005")
                                .add("first_name", "CName5")
                                .add("last_name", "CSurname5")
                                .add("pool_number_by_jp", "415240101")
                                .add("contact_details", new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "400000001")
                                    .add("other_phone", "400000002")
                                    .add("work_phone", "400000003")
                                    .add("email", "041500005@email.gov.uk")
                                )
                                .add("next_attendance_date", "2024-01-01")
                                .add("juror_reasonable_adjustment_with_message", new ReportLinkedMap<String, Object>()
                                    .add("reasonable_adjustment_code_with_description", "M - MULTIPLE")
                                    .add("juror_reasonable_adjustment_message", "multiple requests")
                                )
                        ))
                        .add("BRISTOL (408)", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "040800001")
                                .add("first_name", "BName1")
                                .add("last_name", "BSurname1")
                                .add("pool_number_by_jp", "408240101")
                                .add("contact_details", new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "500000001")
                                    .add("other_phone", "500000002")
                                    .add("work_phone", "500000003")
                                    .add("email", "040800001@email.gov.uk")
                                )
                                .add("next_attendance_date", "2024-01-02")
                                .add("juror_reasonable_adjustment_with_message", new ReportLinkedMap<String, Object>()
                                    .add("reasonable_adjustment_code_with_description", "M - MULTIPLE")
                                    .add("juror_reasonable_adjustment_message", "multiple requests")
                                ),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "040800002")
                                .add("first_name", "BName2")
                                .add("last_name", "BSurname2")
                                .add("pool_number_by_jp", "408240101")
                                .add("contact_details", new ReportLinkedMap<String, Object>()
                                    .add("main_phone", "600000001")
                                    .add("other_phone", "600000002")
                                    .add("work_phone", "600000003")
                                    .add("email", "040800002@email.gov.uk")
                                )
                                .add("next_attendance_date", "2024-01-02")
                                .add("juror_reasonable_adjustment_with_message", new ReportLinkedMap<String, Object>()
                                    .add("reasonable_adjustment_code_with_description", "M - MULTIPLE")
                                    .add("juror_reasonable_adjustment_message", "multiple requests")
                                )
                        ))).build())
            .build();
    }

    public GroupByResponse getTypicalGroupByResponse() {
        return GroupByResponse.builder()
            .name(DataType.COURT_LOCATION_NAME_AND_CODE.name())
            .nested(null)
            .build();
    }
}
