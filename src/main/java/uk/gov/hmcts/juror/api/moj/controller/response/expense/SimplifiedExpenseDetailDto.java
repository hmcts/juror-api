package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@SuppressWarnings({
    "java:S1068"
})
public class SimplifiedExpenseDetailDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;

    private String financialAuditNumber;

    private AttendanceType attendanceType;

    private BigDecimal financialLoss;

    private BigDecimal travel;

    private BigDecimal foodAndDrink;

    private BigDecimal smartcard;

    private BigDecimal totalDue;

    private BigDecimal totalPaid;

    private BigDecimal balanceToPay;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditCreatedOn;

    public BigDecimal getFinancialLoss() {
        return Optional.ofNullable(financialLoss).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTravel() {
        return Optional.ofNullable(travel).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getFoodAndDrink() {
        return Optional.ofNullable(foodAndDrink).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getSmartcard() {
        return Optional.ofNullable(smartcard).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalDue() {
        return Optional.ofNullable(totalDue).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalPaid() {
        return Optional.ofNullable(totalPaid).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getBalanceToPay() {
        return Optional.ofNullable(balanceToPay).orElse(BigDecimal.ZERO);
    }
}
