package uk.gov.hmcts.juror.api.moj.report.bespoke;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.TmpSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class JurorAmendmentByDateReportITest extends AbstractJurorAmendmentReportITest {
    @Autowired
    public JurorAmendmentByDateReportITest(TestRestTemplate template) {
        super(template, JurorAmendmentByDateReport.class);
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2024,5,19))
            .toDate(LocalDate.of(2024,5,21))
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
                        .add("date_to", StandardReportResponse.DataTypeValue.builder()
                            .displayName("Date to")
                            .dataType("LocalDate")
                            .value("2024-05-21")
                            .build())
                        .add("date_from", StandardReportResponse.DataTypeValue.builder()
                            .displayName("Date from")
                            .dataType("LocalDate")
                            .value("2024-05-19")
                            .build()))
                    .tableData(
                        AbstractReportResponse.TableData.builder()
                            .headings(List.of(
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("juror_number")
                                    .name("Juror Number")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("changed")
                                    .name("Changed")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("from")
                                    .name("From")
                                    .dataType("String")
                                    .headings(null)
                                    .build(),
                                StandardReportResponse.TableData.Heading.builder()
                                    .id("to")
                                    .name("To")
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
                                    .jurorNumber("200160029")
                                    .changed(AbstractJurorAmendmentReport.Changed.DATE_OF_BIRTH)
                                    .from("1997-08-26")
                                    .to("1997-08-17")
                                    .changedOn(LocalDateTime.parse("2024-05-21T13:56:53.630"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200160029")
                                    .changed(AbstractJurorAmendmentReport.Changed.FIRST_NAME)
                                    .from("Norma2")
                                    .to("Norma")
                                    .changedOn(LocalDateTime.parse("2024-05-21T13:56:53.630"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200160029")
                                    .changed(AbstractJurorAmendmentReport.Changed.FIRST_NAME)
                                    .from("Norma")
                                    .to("Norma2")
                                    .changedOn(LocalDateTime.parse("2024-05-20T14:09:16.638"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200160029")
                                    .changed(AbstractJurorAmendmentReport.Changed.ADDRESS)
                                    .from(" AIRLL Nook 3,Box Number 74,Swansea,Special post town 2,")
                                    .to(" AIRLL Nook,Box Number 74,Swansea,Special post town,")
                                    .changedOn(LocalDateTime.parse("2024-05-20T14:07:38.620"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200160029")
                                    .changed(AbstractJurorAmendmentReport.Changed.FIRST_NAME)
                                    .from("Norma2")
                                    .to("Norma")
                                    .changedOn(LocalDateTime.parse("2024-05-20T14:07:38.620"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200160029")
                                    .changed(AbstractJurorAmendmentReport.Changed.POSTCODE)
                                    .from("BD2 7BN")
                                    .to("BD2 9BN")
                                    .changedOn(LocalDateTime.parse("2024-05-20T14:07:38.620"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200169805")
                                    .changed(AbstractJurorAmendmentReport.Changed.DATE_OF_BIRTH)
                                    .from("1954-03-10")
                                    .to("1955-03-10")
                                    .changedOn(LocalDateTime.parse("2024-05-19T18:11:12.886"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200169805")
                                    .changed(AbstractJurorAmendmentReport.Changed.ADDRESS)
                                    .from("75 HSDC Gardens,Room Number 15,Portsmouth 123,Redcar and Cleveland1,")
                                    .to("75 HSDC Gardens,Room Number 15,Portsmouth 123 3,Redcar and Cleveland1,")
                                    .changedOn(LocalDateTime.parse("2024-05-19T18:11:12.886"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200169805")
                                    .changed(AbstractJurorAmendmentReport.Changed.POSTCODE)
                                    .from("BD3 9BU")
                                    .to("BD4 9BU")
                                    .changedOn(LocalDateTime.parse("2024-05-19T18:11:12.886"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200169805")
                                    .changed(AbstractJurorAmendmentReport.Changed.ADDRESS)
                                    .from("75 HSDC Gardens,Room Number 15,Portsmouth 123,Redcar and Cleveland,")
                                    .to("75 HSDC Gardens,Room Number 15,Portsmouth 123,Redcar and Cleveland1,")
                                    .changedOn(LocalDateTime.parse("2024-05-19T18:01:52.114"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200160029")
                                    .changed(AbstractJurorAmendmentReport.Changed.ADDRESS)
                                    .from(" AIRLL Nook,Box Number 74,Swansea,Special post town 2,")
                                    .to(" AIRLL Nook 3,Box Number 74,Swansea,Special post town 2,")
                                    .changedOn(LocalDateTime.parse("2024-05-19T14:09:31.692"))
                                    .changedBy("COURT 469")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200160029")
                                    .changed(AbstractJurorAmendmentReport.Changed.DATE_OF_BIRTH)
                                    .from("1996-08-26")
                                    .to("1997-08-26")
                                    .changedOn(LocalDateTime.parse("2024-05-19T14:09:16.695"))
                                    .changedBy("COURT 469 2")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200160029")
                                    .changed(AbstractJurorAmendmentReport.Changed.ADDRESS)
                                    .from(" AIRLL Nook 3,Box Number 74,Swansea,Special post town 2,")
                                    .to(" AIRLL Nook,Box Number 74,Swansea,Special post town 2,")
                                    .changedOn(LocalDateTime.parse("2024-05-19T14:09:16.695"))
                                    .changedBy("COURT 469 2")
                                    .build(),
                                AbstractJurorAmendmentReport.JurorAmendmentReportRow.builder()
                                    .jurorNumber("200160029")
                                    .changed(AbstractJurorAmendmentReport.Changed.FIRST_NAME)
                                    .from("Norma3")
                                    .to("Norma2")
                                    .changedOn(LocalDateTime.parse("2024-05-19T14:09:16.695"))
                                    .changedBy("COURT 469 2")
                                    .build()))
                            .build())
                    .build()
            );
    }
}
