package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.validation.ExpenseNumericLimit;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApportionSmartCardRequest extends AttendanceDates {

    @NotNull
    @ExpenseNumericLimit
    private BigDecimal smartCardAmount;
}
