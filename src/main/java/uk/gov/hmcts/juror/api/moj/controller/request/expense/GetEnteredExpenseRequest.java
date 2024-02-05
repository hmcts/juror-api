package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.time.LocalDate;

@Data
@Builder
public class GetEnteredExpenseRequest {

    @JsonProperty("date_of_expense")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate dateOfExpense;

    @JurorNumber
    @JsonProperty("juror_number")
    @NotBlank
    private String jurorNumber;

    @PoolNumber
    @JsonProperty("pool_number")
    @NotBlank
    private String poolNumber;
}
