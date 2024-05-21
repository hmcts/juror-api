package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.FinancialAuditReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.request.FilterableJurorDetailsRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsWithOriginalDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsWithOriginalTotalsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseTotal;
import uk.gov.hmcts.juror.api.moj.controller.response.FilterableJurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsSimpleDto;
import uk.gov.hmcts.juror.api.moj.service.FinancialAuditService;
import uk.gov.hmcts.juror.api.moj.service.JurorRecordService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FinancialAuditReportServiceImpl implements FinancialAuditReportService {
    private final FinancialAuditService financialAuditService;
    private final JurorRecordService jurorRecordService;

    @Override
    @Transactional(readOnly = true)
    public FinancialAuditReportResponse viewFinancialAuditReport(String financialAuditNumberString) {
        long financialAuditNumber =
            Long.parseLong(financialAuditNumberString.substring(FinancialAuditDetails.F_AUDIT_PREFIX.length()));
        FinancialAuditDetails financialAuditDetails = financialAuditService
            .getFinancialAuditDetails(financialAuditNumber, SecurityUtil.getLocCode());

        SecurityUtil.validateCourtLocationPermitted(financialAuditDetails.getLocCode());

        FinancialAuditDetails forApprovalFinancialAuditDetails =
            financialAuditService.getLastFinancialAuditDetailsWithType(
                financialAuditDetails, FinancialAuditDetails.Type.GenericType.FOR_APPROVAL);

        FinancialAuditReportResponse.FinancialAuditReportResponseBuilder builder =
            FinancialAuditReportResponse.builder()
                .financialAuditNumber(financialAuditDetails.getFinancialAuditNumber())
                .auditType(financialAuditDetails.getType())
                .submittedAt(forApprovalFinancialAuditDetails.getCreatedOn())
                .submittedBy(new UserDetailsSimpleDto(forApprovalFinancialAuditDetails.getCreatedBy()))
                .expenses(getExpenses(financialAuditDetails))
                .jurorDetails(getJurorDetails(financialAuditDetails));

        if (FinancialAuditDetails.Type.GenericType.EDIT.equals(financialAuditDetails.getType().getGenericType())) {
            builder.originalJurorDetails(getJurorDetails(forApprovalFinancialAuditDetails));
        }

        if (FinancialAuditDetails.Type.GenericType.APPROVED.equals(financialAuditDetails.getType().getGenericType())) {
            builder.approvedAt(financialAuditDetails.getCreatedOn())
                .approvedBy(new UserDetailsSimpleDto(financialAuditDetails.getCreatedBy()));
        }

        return builder.build();
    }

    private FilterableJurorDetailsResponseDto getJurorDetails(FinancialAuditDetails financialAuditDetails) {
        return jurorRecordService.getJurorDetails(FilterableJurorDetailsRequestDto.builder()
            .jurorNumber(financialAuditDetails.getJurorNumber())
            .jurorVersion(financialAuditDetails.getJurorRevision())
            .include(List.of(FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS,
                FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS,
                FilterableJurorDetailsRequestDto.IncludeType.PAYMENT_DETAILS,
                FilterableJurorDetailsRequestDto.IncludeType.MILEAGE
            ))
            .build());
    }

    private CombinedExpenseDetailsDto<ExpenseDetailsWithOriginalDto> getExpenses(
        FinancialAuditDetails financialAuditDetails) {

        final Function<Appearance, Appearance> origionalAppearanceFunction;
        ExpenseTotal<ExpenseDetailsWithOriginalDto> expenseTotal = new ExpenseTotal<>(true);
        //If this is an edit report get the original values
        if (FinancialAuditDetails.Type.GenericType.EDIT.equals(financialAuditDetails.getType().getGenericType())) {
            origionalAppearanceFunction =
                appearance -> financialAuditService.getPreviousAppearance(financialAuditDetails, appearance);
        } else if (Set.of(FinancialAuditDetails.Type.REAPPROVED_BACS,
            FinancialAuditDetails.Type.REAPPROVED_CASH).contains(financialAuditDetails.getType())) {
            origionalAppearanceFunction = appearance -> financialAuditService
                .getPreviousApprovedValue(financialAuditDetails, appearance);
            expenseTotal = new ExpenseDetailsWithOriginalTotalsDto();
        } else {
            origionalAppearanceFunction = appearance -> null;
        }

        CombinedExpenseDetailsDto<ExpenseDetailsWithOriginalDto> combinedExpenseDetailsDto =
            new CombinedExpenseDetailsDto<>(expenseTotal);

        financialAuditService.getAppearances(financialAuditDetails)
            .stream()
            .sorted(Comparator.comparing(Appearance::getAttendanceDate))
            .forEach(appearance -> combinedExpenseDetailsDto.addExpenseDetail(
                new ExpenseDetailsWithOriginalDto(appearance,
                    origionalAppearanceFunction.apply(appearance))));
        return combinedExpenseDetailsDto;
    }
}
