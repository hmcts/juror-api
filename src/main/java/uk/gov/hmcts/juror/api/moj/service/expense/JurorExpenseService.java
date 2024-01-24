package uk.gov.hmcts.juror.api.moj.service.expense;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.request.DefaultExpenseSummaryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;

import java.time.LocalDate;

public interface JurorExpenseService {

    @Transactional(readOnly = true)
    BulkExpenseDto getBulkDraftExpense(String jurorNumber, String poolNumber);

    @Transactional(readOnly = true)
    BulkExpenseDto getBulkExpense(String jurorNumber, long financialAuditNumber);

    Page<UnpaidExpenseSummaryResponseDto> getUnpaidExpensesForCourtLocation(String locCode, LocalDate minDate,
                                                                            LocalDate maxDate, int pageNumber,
                                                                            String sortBy, SortDirection sortOrder);

    void setDefaultExpensesForJuror(DefaultExpenseSummaryDto dto);

    void submitDraftExpensesForApproval(ExpenseItemsDto dto);

}
