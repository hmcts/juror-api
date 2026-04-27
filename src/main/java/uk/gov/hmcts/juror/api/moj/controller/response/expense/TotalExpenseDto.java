package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TotalExpenseDto extends ExpenseDto {

    @Builder.Default
    @NotNull
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    private BigDecimal totalAmountPaidToDate = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    private BigDecimal balanceToPay = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    private Integer totalDays = 0;
}
