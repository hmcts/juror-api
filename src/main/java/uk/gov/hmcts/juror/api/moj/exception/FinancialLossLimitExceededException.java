package uk.gov.hmcts.juror.api.moj.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFinancialLoss;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;

import java.math.BigDecimal;
import java.time.LocalDate;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
@Getter
public class FinancialLossLimitExceededException extends MojException.BusinessRuleViolation {

    private final transient RequestBody responseBody;

    public FinancialLossLimitExceededException(RequestBody body) {
        super("Juror's financial loss is over the daily limit", null);
        this.responseBody = body;
    }

    @Data
    @AllArgsConstructor
    public static class RequestBody {
        @JsonProperty("date")
        private LocalDate date;
        @JsonProperty("juror_loss")
        private BigDecimal jurorsLoss;
        @JsonProperty("limit")
        private BigDecimal limit;
        @JsonProperty("attendance_type")
        private AttendanceType attendanceType;
        @JsonProperty("message")
        private String message;
        @JsonProperty("recalculated")
        private DailyExpenseFinancialLoss recalculated;
    }
}
