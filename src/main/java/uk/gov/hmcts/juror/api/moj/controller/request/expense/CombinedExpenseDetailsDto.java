package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.domain.HasTotals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
