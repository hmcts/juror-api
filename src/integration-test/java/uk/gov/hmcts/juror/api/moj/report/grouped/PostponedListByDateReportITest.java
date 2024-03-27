package uk.gov.hmcts.juror.api.moj.report.grouped;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/PostponedListByDateReportITest_typical.sql"
})
@SuppressWarnings("PMD.LawOfDemeter")
class PostponedListByDateReportITest extends AbstractGroupedReportControllerITest {
    @Autowired
    public PostponedListByDateReportITest(TestRestTemplate template) {
        super(template, PostponedListByDateReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2023, 1, 1))
            .toDate(LocalDate.of(2023, 1, 2))
            .build());
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void positiveTypicalCourt() {
        testBuilder()
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
            .jwt(getCourtJwt("414"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(GroupedReportResponse.builder()
                .groupBy(DataType.POOL_NUMBER)
                .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                    .add("total_postponed", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Total postponed")
                        .dataType("Long")
                        .value(0)
                        .build())
                    .add("date_to", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Date To")
                        .dataType("LocalDate")
                        .value("2023-01-02")
                        .build())
                    .add("court_name", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Court Name")
                        .dataType("String")
                        .value("Chelmsford  (414)")
                        .build())
                    .add("date_from", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Date From")
                        .dataType("LocalDate")
                        .value("2023-01-01")
                        .build()))
                .tableData(
                    AbstractReportResponse.TableData.<Map<String, List<LinkedHashMap<String, Object>>>>builder()
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
                                .id("postcode")
                                .name("Postcode")
                                .dataType("String")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("postponed_to")
                                .name("Postponed to")
                                .dataType("LocalDate")
                                .headings(null)
                                .build()))
                        .data(new ReportLinkedMap<>())
                        .build())
                .build());
    }


    private GroupedReportResponse getTypicalResponseCourt() {
        return GroupedReportResponse.builder()
            .groupBy(DataType.POOL_NUMBER)
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_postponed", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total postponed")
                    .dataType("Long")
                    .value(4)
                    .build())
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2023-01-02")
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build())
                .add("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2023-01-01")
                    .build()))
            .tableData(
                AbstractReportResponse.TableData.<Map<String, List<LinkedHashMap<String, Object>>>>builder()
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
                            .id("postcode")
                            .name("Postcode")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("postponed_to")
                            .name("Postponed to")
                            .dataType("LocalDate")
                            .headings(null)
                            .build()))
                    .data(new ReportLinkedMap<String, List<LinkedHashMap<String, Object>>>()
                        .add("415230103", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500023")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3")
                                .add("postcode", "AB1 3CD")
                                .add("postponed_to", "2023-01-01"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500024")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4")
                                .add("postcode", "AB1 4CD")
                                .add("postponed_to", "2023-01-02")))
                        .add("415230104", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500025")
                                .add("first_name", "John5")
                                .add("last_name", "Smith5")
                                .add("postcode", "AB1 5CD")
                                .add("postponed_to", "2023-01-01"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500026")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6")
                                .add("postcode", "AB1 6CD")
                                .add("postponed_to", "2023-01-02"))))
                    .build())
            .build();
    }

    private GroupedReportResponse getTypicalResponseBureau() {
        return GroupedReportResponse.builder()
            .groupBy(DataType.POOL_NUMBER)
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_postponed", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total postponed")
                    .dataType("Long")
                    .value(6)
                    .build())
                .add("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2023-01-02")
                    .build())
                .add("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2023-01-01")
                    .build()))
            .tableData(
                AbstractReportResponse.TableData.<Map<String, List<LinkedHashMap<String, Object>>>>builder()
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
                            .id("postcode")
                            .name("Postcode")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("postponed_to")
                            .name("Postponed to")
                            .dataType("LocalDate")
                            .headings(null)
                            .build()))
                    .data(new ReportLinkedMap<String, List<LinkedHashMap<String, Object>>>()
                        .add("415230101", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500020")
                                .add("first_name", "John0")
                                .add("last_name", "Smith0")
                                .add("postcode", "AB1 0CD")
                                .add("postponed_to", "2023-01-01"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500021")
                                .add("first_name", "John1")
                                .add("last_name", "Smith1")
                                .add("postcode", "AB1 1CD")
                                .add("postponed_to", "2023-01-01")))
                        .add("415230103", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500023")
                                .add("first_name", "John3")
                                .add("last_name", "Smith3")
                                .add("postcode", "AB1 3CD")
                                .add("postponed_to", "2023-01-01"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500024")
                                .add("first_name", "John4")
                                .add("last_name", "Smith4")
                                .add("postcode", "AB1 4CD")
                                .add("postponed_to", "2023-01-02")))
                        .add("415230104", List.of(
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500025")
                                .add("first_name", "John5")
                                .add("last_name", "Smith5")
                                .add("postcode", "AB1 5CD")
                                .add("postponed_to", "2023-01-01"),
                            new ReportLinkedMap<String, Object>()
                                .add("juror_number", "641500026")
                                .add("first_name", "John6")
                                .add("last_name", "Smith6")
                                .add("postcode", "AB1 6CD")
                                .add("postponed_to", "2023-01-02"))))
                    .build())
            .build();
    }
}
