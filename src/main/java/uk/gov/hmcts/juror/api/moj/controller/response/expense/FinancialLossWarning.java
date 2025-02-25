package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialLossWarning {
    @JsonProperty("date")
    @NotNull
    private LocalDate date;

    @JsonProperty("juror_loss")
    @NotNull
    private BigDecimal jurorsLoss;

    @JsonProperty("limit")
    @NotNull
    private BigDecimal limit;

    @JsonProperty("attendance_type")
    @NotNull
    private PayAttendanceType attendanceType;

    @JsonProperty("is_long_trial_day")
    @NotNull
    private Boolean isLongTrialDay;

    @JsonProperty("is_extra_long_trial_day")
    @NotNull
    private Boolean isExtraLongTrialDay;

    @JsonProperty("message")
    @NotNull
    private String message;
}
