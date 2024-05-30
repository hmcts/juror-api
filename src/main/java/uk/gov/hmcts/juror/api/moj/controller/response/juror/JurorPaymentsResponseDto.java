package uk.gov.hmcts.juror.api.moj.controller.response.juror;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Juror appearance and payments log DTO")
public class JurorPaymentsResponseDto {
    @JsonProperty("payment_days")
    @Schema(description = "List of appearance and payments per day")
    private List<PaymentDayDto> data;

    @JsonProperty("attendances")
    @Schema(description = "Number of attendances")
    private long attendances;

    @JsonProperty("non_attendances")
    @Schema(description = "Number of non-attendances")
    private long nonAttendances;

    @JsonProperty("financial_loss")
    @Schema(description = "Total financial loss")
    private BigDecimal financialLoss;

    @JsonProperty("travel")
    @Schema(description = "Total travel cost")
    private BigDecimal travel;

    @JsonProperty("subsistence")
    @Schema(description = "Total food and drink cost")
    private BigDecimal subsistence;

    @JsonProperty("total_paid")
    @Schema(description = "Total paid")
    private BigDecimal totalPaid;

    @AllArgsConstructor
    @Builder
    @Getter
    @Schema(description = "Appearance and payments row")
    @ToString
    public static class PaymentDayDto {
        @JsonProperty("attendance_date")
        @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
        @Schema(description = "Attendance date")
        private LocalDate attendanceDate;

        @JsonProperty("attendance_audit")
        @Schema(description = "Attendance audit number")
        private String attendanceAudit;

        @JsonProperty("payment_audit")
        @Schema(description = "Financial audit number for the payment made")
        private String paymentAudit;

        @JsonProperty("date_paid")
        @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
        @Schema(description = "Date payment was made")
        private LocalDate datePaid;

        @JsonProperty("time_paid")
        @JsonFormat(pattern = ValidationConstants.TIME_FORMAT)
        @Schema(description = "Time payment was made")
        private LocalTime timePaid;

        @JsonProperty("travel")
        @Schema(description = "Travel cost for the day")
        private BigDecimal travel;

        @JsonProperty("financial_loss")
        @Schema(description = "Financial loss for the day")
        private BigDecimal financialLoss;

        @JsonProperty("subsistence")
        @Schema(description = "Food and drink cost for the day")
        private BigDecimal subsistence;

        @JsonProperty("smartcard")
        @Schema(description = "Smartcard spend for the day")
        private BigDecimal smartcard;

        @JsonProperty("total_due")
        @Schema(description = "Total due to the juror for the day")
        private BigDecimal totalDue;

        @JsonProperty("total_paid")
        @Schema(description = "Total paid")
        private BigDecimal totalPaid;
    }
}
