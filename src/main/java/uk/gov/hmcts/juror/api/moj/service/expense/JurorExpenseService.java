package uk.gov.hmcts.juror.api.moj.service.expense;

import org.springframework.data.domain.Page;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApproveExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CalculateTotalExpenseRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.CombinedSimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseCount;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDetailsForTotals;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.FinancialLossWarning;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.PendingApproval;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;

import java.time.LocalDate;
import java.util.List;

public interface JurorExpenseService {

    Page<UnpaidExpenseSummaryResponseDto> getUnpaidExpensesForCourtLocation(String locCode, LocalDate minDate,
                                                                            LocalDate maxDate, int pageNumber,
                                                                            String sortBy, SortDirection sortOrder);

    DefaultExpenseResponseDto getDefaultExpensesForJuror(String jurorNumber);


    void applyDefaultExpenses(Appearance appearance, Juror juror);

    void applyDefaultExpenses(List<Appearance> appearances);

    void setDefaultExpensesForJuror(RequestDefaultExpensesDto dto);

    void submitDraftExpensesForApproval(ExpenseItemsDto dto);

    DailyExpenseResponse updateDraftExpense(String jurorNumber, DailyExpense dailyExpenseRequest);

    GetEnteredExpenseResponse getEnteredExpense(String jurorNumber, String poolNumber, LocalDate dateOfExpense);

    //Must be called after jurors taken by X and juror traveled by X are set
    void updateMilesTraveledAndTravelDue(Appearance appearance, Integer milesTraveled);

    void updateFoodDrinkClaimType(Appearance appearance, FoodDrinkClaimType claimType);

    FinancialLossWarning validateAndUpdateFinancialLossExpenseLimit(Appearance appearance);

    CombinedSimplifiedExpenseDetailDto getSimplifiedExpense(JurorNumberAndPoolNumberDto request, ExpenseType type);

    void approveExpenses(ApproveExpenseDto dto);

    CombinedExpenseDetailsDto<ExpenseDetailsDto> getDraftExpenses(String jurorNumber, String poolNumber);

    CombinedExpenseDetailsDto<ExpenseDetailsForTotals> calculateTotals(CalculateTotalExpenseRequestDto dto);

    ExpenseCount countExpenseTypes(String jurorNumber, String poolNumber);

    List<PendingApproval> getExpensesForApproval(String locCode, PaymentMethod paymentMethod,
                                                 LocalDate fromInclusive, LocalDate toInclusive);

    void updateExpense(String jurorNumber, ExpenseType type, List<DailyExpense> request);
}
