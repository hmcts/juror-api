package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
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

    @JsonProperty("message")
    @NotNull
    private String message;
}