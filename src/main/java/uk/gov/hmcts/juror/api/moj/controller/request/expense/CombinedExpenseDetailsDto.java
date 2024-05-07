package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@SuppressWarnings("squid:NoSonar")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CombinedExpenseDetailsDto<T extends ExpenseDetailsDto> {


    private List<T> expenseDetails;

    private ExpenseTotal<T> total;

    public CombinedExpenseDetailsDto() {
        this(false);
    }

    public CombinedExpenseDetailsDto(boolean hasTotals) {
        this(new ExpenseTotal<>(hasTotals));
    }

    public CombinedExpenseDetailsDto(ExpenseTotal<T> expenseTotal) {
        expenseDetails = new ArrayList<>();
        total = expenseTotal;
    }

    public void addExpenseDetail(T expenseDetailsDto) {
        expenseDetails.add(expenseDetailsDto);
        total.add(expenseDetailsDto);
    }
}
