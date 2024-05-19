package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportTestSupport;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JurorAmendmentByPoolReportTest
    extends AbstractJurorAmendmentReportTestSupport<JurorAmendmentByPoolReport> {

    private JurorPoolService jurorPoolService;

    public JurorAmendmentByPoolReportTest() {
        super(JurorAmendmentByPoolReport.RequestValidator.class);
    }


    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        this.jurorPoolService = mock(JurorPoolService.class);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(JurorAmendmentByJurorReport.class.getName())
            .poolNumber(TestConstants.VALID_POOL_NUMBER)
            .build();
    }

    @Override
    public JurorAmendmentByPoolReport createReport() {
        return new JurorAmendmentByPoolReport(jurorPoolService, jurorAuditService, jurorService, userService);
    }

    @Test
    void positiveGetHeadings() {
        StandardReportRequest request = getValidRequest();
        PoolRequest poolRequest = mock(PoolRequest.class);
        when(poolRequest.getReturnDate()).thenReturn(LocalDate.of(2024,1,1));
        PoolType poolType = mock(PoolType.class);
        when(poolRequest.getPoolType()).thenReturn(poolType);
        when(poolType.getDescription()).thenReturn("Pool Type Desc");

        CourtLocation courtLocation = mock(CourtLocation.class);
        when(poolRequest.getCourtLocation()).thenReturn(courtLocation);
        when(courtLocation.getNameWithLocCode()).thenReturn("Test (123)");
        JurorAmendmentByPoolReport report = createReport();

        Map<String, AbstractReportResponse.DataTypeValue> headings = report
            .getHeadings(request, poolRequest);


        validateReportCreated(headings);

        assertThat(headings).hasSize(4);
        assertThat(headings).containsExactlyInAnyOrderEntriesOf(
            Map.of("pool_number", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Pool Number")
                .value(TestConstants.VALID_POOL_NUMBER)
                .dataType("String")
                .build(),
                "pool_type", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Pool type")
                    .value("Pool Type Desc")
                    .dataType("String")
                    .build(),
                "service_start_date", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Service Start Date")
                    .value("2024-01-01")
                    .dataType("LocalDate")
                    .build(),
                "court_name", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .value("Test (123)")
                    .dataType("String")
                    .build()
            )
        );
    }


    @Test
    void negativeRequestMissingPoolNumber() {
        StandardReportRequest request = getValidRequest();
        request.setPoolNumber(null);
        assertValidationFails(request,
            new AbstractReportTestSupport.ValidationFailure("poolNumber", "must not be null"));
    }

    @Test
    void negativeRequestInvalidPoolNumber() {
        StandardReportRequest request = getValidRequest();
        request.setPoolNumber(TestConstants.INVALID_POOL_NUMBER);
        assertValidationFails(request,
            new AbstractReportTestSupport.ValidationFailure("poolNumber", "must match \"^\\d{9}$\""));
    }
}