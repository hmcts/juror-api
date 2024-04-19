package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@SuppressWarnings({
    "PMD.LawOfDemeter"
})
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

    @JsonProperty("total_due")
    @Override
    public BigDecimal getTotalDue() {
        return Optional.ofNullable(totalDue).orElse(BigDecimal.ZERO);
    }

    @JsonProperty("total_paid")
    @Override
    public BigDecimal getTotalPaid() {
        return Optional.ofNullable(totalPaid).orElse(BigDecimal.ZERO);
    }

    @JsonProperty("total_outstanding")
    @NotNull
    public BigDecimal getTotalOutstanding() {
        return getTotalDue()
            .subtract(getTotalPaid());
    }
}
