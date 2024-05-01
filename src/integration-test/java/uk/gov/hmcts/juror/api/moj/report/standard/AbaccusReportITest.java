package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/letter/LetterController_initSummonsReminderLetter.sql"
})
public class AbaccusReportITest extends AbstractStandardReportControllerITest {
    @Autowired
    public AbaccusReportITest(TestRestTemplate template) {
        super(template, AbaccusReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getBureauJwt();
    }

    @Autowired
    private Clock clock;


    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(LocalDate.of(2024, 1, 1))
            .toDate(LocalDate.of(2024, 1, 31))
            .build());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void positiveTypicalBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    private StandardReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("time_created", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Time created")
                    .dataType(LocalTime.class.getSimpleName())
                    .value(LocalTime.now(clock).format(DateTimeFormatter.ISO_LOCAL_TIME))
                    .build())
                .add("date_to", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Date to")
                    .dataType(LocalDate.class.getSimpleName())
                    .value(LocalDate.of(2024, 1, 31).format(DateTimeFormatter.ISO_DATE))
                    .build())
                .add("date_from", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Date from")
                    .dataType(LocalDate.class.getSimpleName())
                    .value(LocalDate.of(2024, 1, 1).format(DateTimeFormatter.ISO_DATE))
                    .build())
            ).tableData(StandardReportResponse.TableData.<List<LinkedHashMap<String, Object>>>builder()
                .headings(List.of(StandardReportResponse.TableData.Heading.builder()
                        .id("document_code")
                        .name("Document code")
                        .dataType("String")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("total_sent_for_printing")
                        .name("Sent for printing")
                        .dataType("Long")
                        .build(),
                    StandardReportResponse.TableData.Heading.builder()
                        .id("date_sent")
                        .name("Date sent")
                        .dataType("LocalDate")
                        .build()
                ))
                .data(
                    List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("document_code", "5224AC")
                            .add("total_sent_for_printing", 1)
                            .add("date_sent", "2024-01-27"),
                        new ReportLinkedMap<String, Object>()
                            .add("document_code", "5228")
                            .add("total_sent_for_printing", 1)
                            .add("date_sent", "2024-01-20"),
                        new ReportLinkedMap<String, Object>()
                            .add("document_code", "5228")
                            .add("total_sent_for_printing", 1)
                            .add("date_sent", "2024-01-25"),
                        new ReportLinkedMap<String, Object>()
                            .add("document_code", "5228")
                            .add("total_sent_for_printing", 2)
                            .add("date_sent", "2024-01-27"),
                        new ReportLinkedMap<String, Object>()
                            .add("document_code", "5228")
                            .add("total_sent_for_printing", 4)
                            .add("date_sent", "2024-01-31"),
                        new ReportLinkedMap<String, Object>()
                            .add("document_code", "5228C")
                            .add("total_sent_for_printing", 4)
                            .add("date_sent", "2024-01-31")
                    )
                ).build()).build();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
    void negativeInvalidPayload() {
        StandardReportRequest request = getValidPayload();
        request.setToDate(null);
        request.setFromDate(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .assertInvalidPathParam("toDate: must not be null, fromDate: must not be null");
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

}
