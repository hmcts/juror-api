package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;

import java.time.LocalDate;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GetEnteredExpenseRequest {

    @JsonProperty("expense_dates")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotEmpty
    private List<@NotNull LocalDate> expenseDates;
}
