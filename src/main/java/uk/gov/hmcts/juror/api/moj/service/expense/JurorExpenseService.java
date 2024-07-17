package uk.gov.hmcts.juror.api.moj.service.expense;

import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApportionSmartCardRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApproveExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CalculateTotalExpenseRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.UnpaidExpenseSummaryRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.CombinedSimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseCount;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDetailsForTotals;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.FinancialLossWarning;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.PendingApprovalList;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.SummaryExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRatesDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;

import java.time.LocalDate;
import java.util.List;

public interface JurorExpenseService {

    void applyDefaultExpenses(Appearance appearance, Juror juror);

    void applyDefaultExpenses(List<Appearance> appearances);

    DefaultExpenseResponseDto getDefaultExpensesForJuror(String jurorNumber);

    void setDefaultExpensesForJuror(String jurorNumber, RequestDefaultExpensesDto dto);

    void submitDraftExpensesForApproval(String locCode, String jurorNumber, List<LocalDate> attendanceDates);

    DailyExpenseResponse updateDraftExpense(String locCode, String jurorNumber, DailyExpense dailyExpenseRequest);

    GetEnteredExpenseResponse getEnteredExpense(String locCode, String jurorNumber, LocalDate dateOfExpense);

    FinancialLossWarning validateAndUpdateFinancialLossExpenseLimit(Appearance appearance);

    CombinedSimplifiedExpenseDetailDto getSimplifiedExpense(String locCode, String jurorNumber, ExpenseType type);

    void approveExpenses(String locCode, PaymentMethod paymentMethod, ApproveExpenseDto dto);

    CombinedExpenseDetailsDto<ExpenseDetailsDto> getDraftExpenses(String locCode, String jurorNumber);

    CombinedExpenseDetailsDto<ExpenseDetailsForTotals> calculateTotals(
        String locCode, String jurorNumber, CalculateTotalExpenseRequestDto dto);

    ExpenseCount countExpenseTypes(String locCode, String jurorNumber);

    PendingApprovalList getExpensesForApproval(String locCode, PaymentMethod paymentMethod,
                                               LocalDate fromInclusive, LocalDate toInclusive);

    void updateExpense(String locCode, String jurorNumber, ExpenseType type, List<DailyExpense> request);


    boolean isLongTrialDay(String jurorNumber, LocalDate localDate);

    void apportionSmartCard(String locCode, String jurorNumber, ApportionSmartCardRequest request);

    ExpenseRates getCurrentExpenseRates(boolean onlyFinancialLossLimit);

    void updateExpenseRates(ExpenseRatesDto expenseRatesDto);

    CombinedExpenseDetailsDto<ExpenseDetailsDto> getExpenses(
        String locCode, String jurorNumber, List<LocalDate> dates);

    SummaryExpenseDetailsDto calculateSummaryTotals(String locCode, String jurorNumber);

    void realignExpenseDetails(Appearance appearance, boolean isDeleted);

    PaginatedList<UnpaidExpenseSummaryResponseDto> getUnpaidExpensesForCourtLocation(
        String locCode, UnpaidExpenseSummaryRequestDto search);
}
