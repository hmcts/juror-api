package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GetEnteredExpenseRequest extends JurorNumberAndPoolNumberDto {

    @JsonProperty("date_of_expense")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate dateOfExpense;
}
