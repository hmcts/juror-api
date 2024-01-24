package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;

import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils.getOrZero;

@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BulkExpenseEntryDto extends ExpenseDto {

    @JsonProperty("appearance_date")
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd"
    )
    @NotNull
    private LocalDate appearanceDate;

    @JsonProperty("attendance_type")
    @NotNull
    private AttendanceType attendanceType;

    @JsonProperty("payment_method")
    @NotNull
    private PaymentMethod paymentMethod;

    @JsonProperty("original_value")
    @Nullable
    private BulkExpenseEntryDto originalValue;

    public static BulkExpenseEntryDto fromAppearance(Appearance appearance) {
        return BulkExpenseEntryDto.builder()
            .publicTransport(appearance.getPublicTransportTotal())
            .taxi(appearance.getHiredVehicleTotal())
            .motorcycle(appearance.getMotorcycleTotal())
            .car(appearance.getCarTotal())
            .bicycle(appearance.getBicycleTotal())
            .parking(appearance.getParkingTotal())
            .foodAndDrink(appearance.getSubsistenceTotal())
            .lossOfEarnings(appearance.getLossOfEarningsTotal())
            .extraCare(appearance.getChildcareTotal())
            .other(appearance.getMiscAmountTotal())
            .smartCard(getOrZero(appearance.getSmartCardAmountTotal()))
            .appearanceDate(appearance.getAttendanceDate())
            .attendanceType(appearance.getAttendanceType())
            .paymentMethod(Boolean.TRUE.equals(appearance.getPayCash()) ? PaymentMethod.CASH : PaymentMethod.BACS)
            .build();
    }
}
