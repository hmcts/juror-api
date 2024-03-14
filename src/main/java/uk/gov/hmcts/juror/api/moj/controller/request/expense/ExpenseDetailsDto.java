package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@SuppressWarnings({
    "PMD.LawOfDemeter"
})
public class ExpenseDetailsDto extends ExpenseValuesDto {

    @JsonProperty("attendance_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;

    @JsonProperty("attendance_type")
    private AttendanceType attendanceType;


    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;

    public ExpenseDetailsDto(Appearance appearance) {
        super(appearance);
        this.attendanceDate = appearance.getAttendanceDate();
        this.attendanceType = appearance.getAttendanceType();
        this.paymentMethod = appearance.isPayCash() ? PaymentMethod.CASH : PaymentMethod.BACS;
    }
}
