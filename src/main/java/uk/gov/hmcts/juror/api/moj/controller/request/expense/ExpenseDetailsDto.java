package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExpenseDetailsDto extends ExpenseValuesDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    protected LocalDate attendanceDate;

    protected AttendanceType attendanceType;


    protected PaymentMethod paymentMethod;

    public ExpenseDetailsDto(Appearance appearance) {
        super(appearance);
        this.attendanceDate = appearance.getAttendanceDate();
        this.attendanceType = appearance.getAttendanceType();
        this.paymentMethod = appearance.isPayCash() ? PaymentMethod.CASH : PaymentMethod.BACS;
    }
}
