package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.HasTotals;

import java.math.BigDecimal;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExpenseDetailsWithTotalsDto extends ExpenseDetailsDto implements HasTotals {

    @NotNull
    private BigDecimal totalDue;
    @NotNull
    private BigDecimal totalPaid;

    public ExpenseDetailsWithTotalsDto(Appearance appearance) {
        super(appearance);
        this.totalDue = appearance.getTotalDue();
        this.totalPaid = appearance.getTotalPaid();
    }


    @Override
    public BigDecimal getTotalDue() {
        return Optional.ofNullable(totalDue).orElse(BigDecimal.ZERO);
    }


    @Override
    public BigDecimal getTotalPaid() {
        return Optional.ofNullable(totalPaid).orElse(BigDecimal.ZERO);
    }


    @NotNull
    public BigDecimal getTotalOutstanding() {
        return getTotalDue()
            .subtract(getTotalPaid());
    }
}
