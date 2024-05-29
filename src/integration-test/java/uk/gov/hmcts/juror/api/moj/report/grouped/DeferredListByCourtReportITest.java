package uk.gov.hmcts.juror.api.moj.report.grouped;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.util.LinkedHashMap;
import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/DeferredListByCourtReportITest_typical.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
class DeferredListByCourtReportITest extends AbstractGroupedReportControllerITest {

    @Autowired
    public DeferredListByCourtReportITest(TestRestTemplate template) {
        super(template, DeferredListByCourtReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .build());
    }

    @Test
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponseCourt());
    }

    @Test
    void positiveNotFound() {
        testBuilder()
            .jwt(getCourtJwt("400"))
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(StandardReportResponse.builder()
                .headings(new ReportLinkedMap<String, StandardReportResponse.DataTypeValue>()
                    .add("total_deferred", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Total deferred")
                        .dataType("Long")
                        .value(0)
                        .build()))
                .tableData(
                    StandardReportResponse.TableData.<List<LinkedHashMap<String, Object>>>builder()
                        .headings(List.of(
                            StandardReportResponse.TableData.Heading.builder()
                                .id("deferred_to")
                                .name("Deferred to")
                                .dataType("LocalDate")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("number_deferred")
                                .name("Number Deferred")
                                .dataType("Long")
                                .headings(null)
                                .build()))
                        .data(List.of())
                        .build())
                .build());
    }

    private StandardReportResponse getTypicalResponseCourt() {
        return StandardReportResponse.builder()
            .headings(new ReportLinkedMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_deferred", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total deferred")
                    .dataType("Long")
                    .value(4)
                    .build()))
            .tableData(
                StandardReportResponse.TableData.<List<LinkedHashMap<String, Object>>>builder()
                    .headings(List.of(
                        StandardReportResponse.TableData.Heading.builder()
                            .id("deferred_to")
                            .name("Deferred to")
                            .dataType("LocalDate")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("number_deferred")
                            .name("Number Deferred")
                            .dataType("Long")
                            .headings(null)
                            .build()))
                    .data(List.of(
                        new GroupedTableData()
                            .add("CHESTER (415)", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                            ))
                            .add("MANCHESTER CROWN SQUARE (415)", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                            ))
                            .add("MANCHESTER MINSHULL STREET (415)", List.of(
                                new ReportLinkedMap<String, Object>()
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                                    .add("deferred_to", "Fri 4 Nov 2024")
                            ))
                    ))
                    .build())
            .build();
    }
}
