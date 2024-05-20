package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportTestSupport;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JurorAmendmentByDateReportTest
    extends AbstractJurorAmendmentReportTestSupport<JurorAmendmentByDateReport> {


    public JurorAmendmentByDateReportTest() {
        super(JurorAmendmentByDateReport.RequestValidator.class);
    }


    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(JurorAmendmentByJurorReport.class.getName())
            .fromDate(LocalDate.of(2024, 1, 1))
            .toDate(LocalDate.of(2024, 1, 30))
            .build();
    }

    @Override
    public JurorAmendmentByDateReport createReport() {
        return new JurorAmendmentByDateReport(jurorAuditService, jurorService, userService);
    }

    @Test
    void positiveGetHeadings() {
        StandardReportRequest request = getValidRequest();
        JurorAmendmentByDateReport report = createReport();

        Map<String, AbstractReportResponse.DataTypeValue> headings = report
            .getHeadings(request);

        validateReportCreated(headings);

        assertThat(headings).hasSize(2);
        assertThat(headings).containsExactlyInAnyOrderEntriesOf(
            Map.of("date_from", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Date from")
                    .value("2024-01-01")
                    .dataType("LocalDate")
                    .build(),
                "date_to", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Date to")
                    .value("2024-01-30")
                    .dataType("LocalDate")
                    .build()
            )
        );
    }

    @Test
    void negativeRequestMissingFromDate() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request,
            new AbstractReportTestSupport.ValidationFailure("fromDate", "must not be null"));
    }


    @Test
    void negativeRequestMissingToDate() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request,
            new AbstractReportTestSupport.ValidationFailure("toDate", "must not be null"));
    }
}