package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportTestSupport;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JurorAmendmentByJurorReportTest extends AbstractJurorAmendmentReportTestSupport<JurorAmendmentByJurorReport> {


    public JurorAmendmentByJurorReportTest() {
        super(JurorAmendmentByJurorReport.RequestValidator.class);
    }


    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(JurorAmendmentByJurorReport.class.getName())
            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
            .build();
    }

    @Override
    public JurorAmendmentByJurorReport createReport() {
        return new JurorAmendmentByJurorReport(jurorAuditService, jurorService, userService);
    }

    @Test
    void positiveGetHeadings() {
        StandardReportRequest request = getValidRequest();
        String jurorName = "Test Name 123";
        JurorAmendmentByJurorReport report = createReport();

        Map<String, AbstractReportResponse.DataTypeValue> headings = report
            .getHeadings(request, jurorName);

        validateReportCreated(headings);

        assertThat(headings).hasSize(2);
        assertThat(headings).containsExactlyInAnyOrderEntriesOf(
            Map.of("juror_number", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Juror Number")
                    .value(TestConstants.VALID_JUROR_NUMBER)
                    .dataType("String")
                    .build(),
                "juror_name", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Juror Name")
                    .value(jurorName)
                    .dataType("String")
                    .build()
            )
        );
    }

    @Test
    void negativeRequestMissingJurorNumber() {
        StandardReportRequest request = getValidRequest();
        request.setJurorNumber(null);
        assertValidationFails(request,
            new AbstractReportTestSupport.ValidationFailure("jurorNumber", "must not be null"));
    }

    @Test
    void negativeRequestInvalidJurorNumber() {
        StandardReportRequest request = getValidRequest();
        request.setJurorNumber(TestConstants.INVALID_JUROR_NUMBER);
        assertValidationFails(request,
            new AbstractReportTestSupport.ValidationFailure("jurorNumber", "must match \"^\\d{9}$\""));
    }
}
