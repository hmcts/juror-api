package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils.getOrZero;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;

@Entity
@Table(name = "appearance", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(AppearanceId.class)
@Builder
@ToString
@Getter
@Setter
@Audited
@SuppressWarnings("PMD.TooManyFields")
public class Appearance implements Serializable {

    @Id
    @Column(name = "juror_number")
    @Pattern(regexp = JUROR_NUMBER)
    @Length(max = 9)
    private String jurorNumber;

    @Id
    @NotNull
    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "loc_code", nullable = false)
    private CourtLocation courtLocation;

    @ManyToOne
    @JoinColumn(name = "f_audit", referencedColumnName = "id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private FinancialAuditDetails financialAuditDetails;

    @Length(max = 9)
    @Column(name = "pool_number")
    private String poolNumber;

    /**
     * Flag recording whether the juror sat on a jury (true = sat on jury).
     */
    @Column(name = "sat_on_jury")
    private Boolean satOnJury;

    @Column(name = "time_in")
    private LocalTime timeIn;

    @Column(name = "time_out")
    private LocalTime timeOut;

    @Column(name = "trial_number")
    @Length(max = 16)
    private String trialNumber;

    @Column(name = "appearance_stage")
    @Enumerated(EnumType.STRING)
    private AppearanceStage appearanceStage;

    @Column(name = "attendance_type")
    @Enumerated(EnumType.STRING)
    private AttendanceType attendanceType;

    @Column(name = "pay_attendance_type")
    @Enumerated(EnumType.STRING)
    private PayAttendanceType payAttendanceType;

    @Column(name = "non_attendance")
    @Builder.Default
    private Boolean nonAttendanceDay = false;

    @Column(name = "mileage_due")
    private Integer mileageDue;

    @Column(name = "mileage_paid")
    private Integer mileagePaid;

    @Column(name = "misc_description")
    private String miscDescription;

    @Column(name = "pay_cash")
    private Boolean payCash;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @Column(name = "created_by")
    private String createdBy;

    // transport expenses
    @Column(name = "public_transport_total_due")
    private BigDecimal publicTransportDue;
    @Column(name = "public_transport_total_paid")
    private BigDecimal publicTransportPaid;

    @Column(name = "hired_vehicle_total_due")
    private BigDecimal hiredVehicleDue;
    @Column(name = "hired_vehicle_total_paid")
    private BigDecimal hiredVehiclePaid;

    @Column(name = "motorcycle_total_due")
    private BigDecimal motorcycleDue;
    @Column(name = "motorcycle_total_paid")
    private BigDecimal motorcyclePaid;

    @Column(name = "car_total_due")
    private BigDecimal carDue;
    @Column(name = "car_total_paid")
    private BigDecimal carPaid;

    @Column(name = "pedal_cycle_total_due")
    private BigDecimal bicycleDue;
    @Column(name = "pedal_cycle_total_paid")
    private BigDecimal bicyclePaid;

    @Column(name = "parking_total_due")
    private BigDecimal parkingDue;
    @Column(name = "parking_total_paid")
    private BigDecimal parkingPaid;

    // childcare expenses
    @Column(name = "childcare_total_due")
    private BigDecimal childcareDue;
    @Column(name = "childcare_total_paid")
    private BigDecimal childcarePaid;

    // miscellaneous expenses
    @Column(name = "misc_total_due")
    private BigDecimal miscAmountDue;
    @Column(name = "misc_total_paid")
    private BigDecimal miscAmountPaid;

    // Loss of earnings (different caps apply for half day/full day/long trials)
    @Column(name = "loss_of_earnings_due")
    private BigDecimal lossOfEarningsDue;
    @Column(name = "loss_of_earnings_paid")
    private BigDecimal lossOfEarningsPaid;

    // Subsistence (food and drink) expenses (same flat rate applies for whole day/half day/long day/overnight).
    @Column(name = "subsistence_due")
    private BigDecimal subsistenceDue;
    @Column(name = "subsistence_paid")
    private BigDecimal subsistencePaid;

    /**
     * Amount spent on a smart card (credit), usually on food and drink in a canteen (to be deducted from expenses due).
     */
    @Column(name = "smart_card_due")
    private BigDecimal smartCardAmountDue;

    /**
     * Amount of spend on a smart card that has been deducted prior to payment.
     */
    @Column(name = "smart_card_paid")
    private BigDecimal smartCardAmountPaid;


    @Column(name = "travel_time")
    private LocalTime travelTime;

    @Column(name = "travel_by_car")
    private Boolean traveledByCar;

    @Column(name = "travel_jurors_taken_by_car")
    @Min(0)
    private Integer jurorsTakenCar;

    @Column(name = "travel_by_motorcycle")
    private Boolean traveledByMotorcycle;

    @Column(name = "travel_jurors_taken_by_motorcycle")
    @Min(0)
    private Integer jurorsTakenMotorcycle;

    @Column(name = "travel_by_bicycle")
    private Boolean traveledByBicycle;


    @Column(name = "miles_traveled")
    private Integer milesTraveled;

    @Column(name = "food_and_drink_claim_type")
    @Enumerated(EnumType.STRING)
    private FoodDrinkClaimType foodAndDrinkClaimType;


    /**
     * The date the expense was approved for payment to be processed.
     */
    @Column(name = "payment_approved_date")
    private LocalDateTime paymentApprovedDate;

    /**
     * The date the juror submitted their expense to the court.
     */
    @Column(name = "expense_submitted_date")
    private LocalDate expenseSubmittedDate;

    /**
     * Flag indicating whether the expense is being "saved for later" in a draft state (true) or if it is ready to be
     * authorised (false).
     */
    @Column(name = "is_draft_expense")
    private Boolean isDraftExpense;

    /**
     * flag indicating whether the juror has not attended court on a day they were due to be present (unauthorised
     * absence).
     */
    @Column(name = "no_show")
    private Boolean noShow;

    public String getIdString() {
        return "JurorNumber: " + this.jurorNumber + ", "
            + "AttendanceDate: " + this.attendanceDate + ", "
            + "CourtLocation: " + this.courtLocation;
    }

    public BigDecimal getPublicTransportTotal() {
        return getOrZero(this.publicTransportDue).add(getOrZero(this.publicTransportPaid));
    }

    public BigDecimal getHiredVehicleTotal() {
        return getOrZero(this.hiredVehicleDue).add(getOrZero(this.hiredVehiclePaid));
    }

    public BigDecimal getMotorcycleTotal() {
        return getOrZero(this.motorcycleDue).add(getOrZero(this.motorcyclePaid));
    }

    public BigDecimal getCarTotal() {
        return getOrZero(this.carDue).add(getOrZero(this.carPaid));
    }

    public BigDecimal getBicycleTotal() {
        return getOrZero(this.bicycleDue).add(getOrZero(this.bicyclePaid));
    }

    public BigDecimal getParkingTotal() {
        return getOrZero(this.parkingDue).add(getOrZero(this.parkingPaid));
    }

    public BigDecimal getSubsistenceTotal() {
        return getOrZero(this.subsistenceDue).add(getOrZero(this.subsistencePaid));
    }

    public BigDecimal getLossOfEarningsTotal() {
        return getOrZero(this.lossOfEarningsDue).add(getOrZero(this.lossOfEarningsPaid));
    }

    public BigDecimal getChildcareTotal() {
        return getOrZero(this.childcareDue).add(getOrZero(this.childcarePaid));
    }

    public BigDecimal getMiscAmountTotal() {
        return getOrZero(this.miscAmountDue).add(getOrZero(this.miscAmountPaid));
    }

    public BigDecimal getSmartCardAmountTotal() {
        return getOrZero(this.smartCardAmountDue).add(getOrZero(this.smartCardAmountPaid));
    }

    //Does not include smart card reduction
    public BigDecimal getTotalDue() {
        return getOrZero(this.getPublicTransportDue())
            .add(getOrZero(this.getHiredVehicleDue()))
            .add(getOrZero(this.getMotorcycleDue()))
            .add(getOrZero(this.getCarDue()))
            .add(getOrZero(this.getBicycleDue()))
            .add(getOrZero(this.getParkingDue()))
            .add(getOrZero(this.getSubsistenceDue()))
            .add(getOrZero(this.getLossOfEarningsDue()))
            .add(getOrZero(this.getChildcareDue()))
            .add(getOrZero(this.getMiscAmountDue()))
            .subtract(getOrZero(this.getSmartCardAmountDue()));
    }

    public BigDecimal getTotalPaid() {
        return getOrZero(this.getPublicTransportPaid())
            .add(getOrZero(this.getHiredVehiclePaid()))
            .add(getOrZero(this.getMotorcyclePaid()))
            .add(getOrZero(this.getCarPaid()))
            .add(getOrZero(this.getBicyclePaid()))
            .add(getOrZero(this.getParkingPaid()))
            .add(getOrZero(this.getSubsistencePaid()))
            .add(getOrZero(this.getLossOfEarningsPaid()))
            .add(getOrZero(this.getChildcarePaid()))
            .add(getOrZero(this.getMiscAmountPaid()))
            .subtract(getOrZero(this.getSmartCardAmountPaid()));
    }

    public LocalTime getTimeSpentAtCourt() {
        if (timeOut == null || timeIn == null) {
            return LocalTime.of(0, 0);
        }
        return this.timeOut.minusNanos(this.timeIn.toNanoOfDay());
    }

    public Boolean isLongTrialDay() {
        return this.attendanceType.getIsLongTrial();
    }
}
