package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Optional;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@ToString(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExpenseDetailsWithOriginalTotalsDto extends ExpenseTotal<ExpenseDetailsWithOriginalDto> {

    @JsonProperty("original_total_paid")
    private BigDecimal originalTotalPaid;

    public ExpenseDetailsWithOriginalTotalsDto() {
        super(true);
        originalTotalPaid = BigDecimal.ZERO;
    }

    @Override
    public void add(ExpenseDetailsWithOriginalDto expenseDetailsDto) {
        super.add(expenseDetailsDto);
        originalTotalPaid = originalTotalPaid.add(expenseDetailsDto.getOriginal().getTotalPaid());
    }

    @JsonProperty("change_from_original")
    public BigDecimal getOriginalTotalPaid() {
        return Optional.ofNullable(this.getTotalPaid())
            .orElse(BigDecimal.ZERO)
            .subtract(this.originalTotalPaid);
    }
}
