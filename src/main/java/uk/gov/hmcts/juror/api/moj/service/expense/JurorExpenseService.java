package uk.gov.hmcts.juror.api.moj.service.expense;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;

import java.time.LocalDate;

public interface JurorExpenseService {

    Page<UnpaidExpenseSummaryResponseDto> getUnpaidExpensesForCourtLocation(String locCode, LocalDate minDate,
                                                                            LocalDate maxDate, int pageNumber,
                                                                            String sortBy, SortDirection sortOrder);

    DefaultExpenseResponseDto getDefaultExpensesForJuror(String jurorNumber);

    void setDefaultExpensesForJuror(RequestDefaultExpensesDto dto);

    void submitDraftExpensesForApproval(ExpenseItemsDto dto);

    DailyExpenseResponse updateDraftExpense(String jurorNumber, DailyExpense dailyExpenseRequest);

    @Transactional(readOnly = true)
    GetEnteredExpenseResponse getEnteredExpense(String jurorNumber, String poolNumber, LocalDate dateOfExpense);

    //Must be called after jurors taken by X and juror traveled by X are set
    void updateMilesTraveledAndTravelDue(Appearance appearance, Integer milesTraveled);

    void updateFoodDrinkClaimType(Appearance appearance, FoodDrinkClaimType claimType);
}
