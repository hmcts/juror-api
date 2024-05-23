package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.grouped.groupby.GroupByPaymentType;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts;

public abstract class AbstractJurorExpenditureReportTestSupport<R extends AbstractGroupedReport>
    extends AbstractGroupedReportTestSupport<R> {

    protected CourtLocationService courtLocationService;
    protected MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public AbstractJurorExpenditureReportTestSupport(
        boolean includeNested,
        IDataType... dataTypes) {
        super(
            lowLevelFinancialAuditDetailsIncludingApprovedAmounts,
            AbstractJurorExpenditureReport.RequestValidator.class,
            new GroupByPaymentType(includeNested),
            dataTypes);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(TestConstants.VALID_COURT_LOCATION);
        this.courtLocationService = mock(CourtLocationService.class);
        super.beforeEach();
    }

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(LocalDate.of(2020, 1, 1))
            .toDate(LocalDate.of(2020, 1, 2))
            .build();
    }

    @Test
    void negativeMissingFromDate() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }

    @Test
    void negativeMissingToDate() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }


    protected void verifyStandardPreProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        verify(query, times(1)).where(
            lowLevelFinancialAuditDetailsIncludingApprovedAmounts.locCode.eq(
                TestConstants.VALID_COURT_LOCATION));
        verify(query, times(1)).where(
            lowLevelFinancialAuditDetailsIncludingApprovedAmounts.type.in(
                FinancialAuditDetails.Type.APPROVED_BACS,
                FinancialAuditDetails.Type.APPROVED_CASH,
                FinancialAuditDetails.Type.REAPPROVED_BACS,
                FinancialAuditDetails.Type.REAPPROVED_CASH
            ));
        verify(query, times(1)).where(
            lowLevelFinancialAuditDetailsIncludingApprovedAmounts.createdOn.between(
                LocalDateTime.of(request.getFromDate(), LocalTime.MIN),
                LocalDateTime.of(request.getToDate(), LocalTime.MAX)));
    }
}
