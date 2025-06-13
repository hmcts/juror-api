package uk.gov.hmcts.juror.api.moj.report.bespoke;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class JurorAmendmentByJurorReportITest extends AbstractJurorAmendmentReportITest {
    @Autowired
    public JurorAmendmentByJurorReportITest(TestRestTemplate template) {
        super(template, JurorAmendmentByJurorReport.class);
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .jurorNumber("200160029")
            .build());
    }

    @Test
    @SneakyThrows
    void positiveTypical() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(
                JurorAmendmentReportResponse.builder()
                    .headings(new ReportHashMap<String, AbstractReportResponse.DataTypeValue>()
                        .add("juror_number", StandardReportResponse.DataTypeValue.builder()
                            .displayName("Juror Number")
                            .dataType("String")
                            .value("200160029")
                            .build())
                        .add("juror_name", StandardReportResponse.DataTypeValue.builder()
                            .displayName("Juror Name")
                            .dataType("String")
                            .value("Ms Norma3 Adeniran")
                            .build()))
                    .tableData(
                        AbstractReportResponse.TableData.builder()
                            .headings(List.of(
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("changed")
                                    .name("Changed")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("from")
                                    .name("Changed from")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("changed_on")
                                    .name("Changed on")
                                    .dataType("LocalDateTime")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("changed_by")
                                    .name("Changed By")
                                    .dataType("String")
                                    .headings(null)
                                    .build()))
                            .data(List.of(
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.BANK_ACCOUNT_HOLDER_NAME)
                                    .from("Test Name 2")
                                    .changedOn(LocalDateTime.parse("2024-05-24T17:21:50.787"))
                                    .changedBy("COURT.999")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.SORT_CODE)
                                    .from("123456")
                                    .changedOn(LocalDateTime.parse("2024-05-24T17:21:50.787"))
                                    .changedBy("COURT.999")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.BANK_ACCOUNT_NUMBER)
                                    .from("12345678")
                                    .changedOn(LocalDateTime.parse("2024-05-24T17:21:50.787"))
                                    .changedBy("COURT.999")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.BANK_ACCOUNT_HOLDER_NAME)
                                    .from("Test Name")
                                    .changedOn(LocalDateTime.parse("2024-05-22T16:20:02.677"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.SORT_CODE)
                                    .from("608407")
                                    .changedOn(LocalDateTime.parse("2024-05-22T16:20:02.677"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.BANK_ACCOUNT_NUMBER)
                                    .from("30459873")
                                    .changedOn(LocalDateTime.parse("2024-05-22T16:20:02.677"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.DATE_OF_BIRTH)
                                    .from("1997-08-17")
                                    .changedOn(LocalDateTime.parse("2024-05-22T16:19:52.587"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.ADDRESS)
                                    .from(" AIRLL Nook,Box Number 74,Swansea,Special post town,")
                                    .changedOn(LocalDateTime.parse("2024-05-22T16:19:52.587"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.BANK_ACCOUNT_HOLDER_NAME)
                                    .from(null)
                                    .changedOn(LocalDateTime.parse("2024-05-22T16:19:52.587"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.SORT_CODE)
                                    .from(null)
                                    .changedOn(LocalDateTime.parse("2024-05-22T16:19:52.587"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.BANK_ACCOUNT_NUMBER)
                                    .from(null)
                                    .changedOn(LocalDateTime.parse("2024-05-22T16:19:52.587"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.FIRST_NAME)
                                    .from("Norma")
                                    .changedOn(LocalDateTime.parse("2024-05-22T16:19:52.587"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.POSTCODE)
                                    .from("BD2 9BN")
                                    .changedOn(LocalDateTime.parse("2024-05-22T16:19:52.587"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.DATE_OF_BIRTH)
                                    .from("1997-08-26")
                                    .changedOn(LocalDateTime.parse("2024-05-21T12:56:53.630"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.FIRST_NAME)
                                    .from("Norma2")
                                    .changedOn(LocalDateTime.parse("2024-05-21T12:56:53.630"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.FIRST_NAME)
                                    .from("Norma")
                                    .changedOn(LocalDateTime.parse("2024-05-20T13:09:16.638"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.ADDRESS)
                                    .from(" AIRLL Nook 3,Box Number 74,Swansea,Special post town 2,")
                                    .changedOn(LocalDateTime.parse("2024-05-20T13:07:38.620"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.FIRST_NAME)
                                    .from("Norma2")
                                    .changedOn(LocalDateTime.parse("2024-05-20T13:07:38.620"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.POSTCODE)
                                    .from("BD2 7BN")
                                    .changedOn(LocalDateTime.parse("2024-05-20T13:07:38.620"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.ADDRESS)
                                    .from(" AIRLL Nook,Box Number 74,Swansea,Special post town 2,")
                                    .changedOn(LocalDateTime.parse("2024-05-19T13:09:31.692"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.DATE_OF_BIRTH)
                                    .from("1996-08-26")
                                    .changedOn(LocalDateTime.parse("2024-05-19T13:09:16.695"))
                                    .changedBy("COURT 469 2")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.ADDRESS)
                                    .from(" AIRLL Nook 3,Box Number 74,Swansea,Special post town 2,")
                                    .changedOn(LocalDateTime.parse("2024-05-19T13:09:16.695"))
                                    .changedBy("COURT 469 2")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.FIRST_NAME)
                                    .from("Norma3")
                                    .changedOn(LocalDateTime.parse("2024-05-19T13:09:16.695"))
                                    .changedBy("COURT 469 2")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .changed(AbstractJurorAmendmentReport.Changed.DATE_OF_BIRTH)
                                    .from("1997-08-26")
                                    .changedOn(LocalDateTime.parse("2024-05-18T13:09:45.443"))
                                    .changedBy("COURT 469")
                                    .build()))
                            .build())
                    .build()
            );
    }
}
