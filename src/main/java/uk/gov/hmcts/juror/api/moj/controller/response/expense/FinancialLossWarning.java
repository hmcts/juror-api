package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FinancialLossWarning {
    @NotNull
    private LocalDate date;

    @NotNull
    private BigDecimal jurorsLoss;

    @NotNull
    private BigDecimal limit;

    @NotNull
    private PayAttendanceType attendanceType;

    @NotNull
    private Boolean isLongTrialDay;

    @NotNull
    private Boolean isExtraLongTrialDay;

    @NotNull
    private String message;
}
