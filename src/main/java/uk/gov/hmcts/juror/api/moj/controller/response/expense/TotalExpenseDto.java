package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TotalExpenseDto extends ExpenseDto {

    @JsonProperty("total_amount")
    @Builder.Default
    @NotNull
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @JsonProperty("total_amount_paid_to_date")
    @Builder.Default
    @NotNull
    private BigDecimal totalAmountPaidToDate = BigDecimal.ZERO;

    @JsonProperty("balance_to_pay")
    @Builder.Default
    @NotNull
    private BigDecimal balanceToPay = BigDecimal.ZERO;

    @JsonProperty("total_days")
    @Builder.Default
    @NotNull
    private Integer totalDays = 0;
}
