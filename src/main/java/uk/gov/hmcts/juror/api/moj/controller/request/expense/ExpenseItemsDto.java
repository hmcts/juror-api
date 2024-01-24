package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class ExpenseItemsDto {

    @JsonProperty("juror_number")
    @JurorNumber
    @NotNull
    private String jurorNumber;

    @JsonProperty("pool_number")
    @PoolNumber
    @NotNull
    private String poolNumber;

    @JsonProperty("attendance_dates")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    @NotEmpty
    private List<LocalDate> attendanceDates;

}
