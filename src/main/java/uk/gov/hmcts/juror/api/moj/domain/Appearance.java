package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Generated;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils.getOrZero;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;

@Entity
@Table(name = "appearance", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(AppearanceId.class)
@Builder
@Getter
@Setter
@Audited
@ToString
@SuppressWarnings({"PMD.TooManyFields", "PMD.TooManyImports"})
public class Appearance implements Serializable {
    private static final int ROUNDING_PRECISION = 2;

    @Version
    @Audited
    private Long version;

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

    //Used for audit retrieval of this entity do not modify this field.
    @Column(name = "loc_code", nullable = false, insertable = false, updatable = false)
    private String locCode;

    @Column(name = "f_audit")
    private Long financialAudit;

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

    @Column(name = "non_attendance")
    @Builder.Default
    private Boolean nonAttendanceDay = false;

    @Column(name = "misc_description")
    private String miscDescription;

    @Column(name = "pay_cash")
    @Builder.Default
    private boolean payCash = false;

    @Column(name = "last_updated_by")
    @LastModifiedBy
    private String lastUpdatedBy;

    @Column(name = "created_by")
    @CreatedBy
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
     * Flag indicating whether the expense is being "saved for later" in a draft state (true) or if it is ready to be
     * authorised (false).
     */
    @Column(name = "is_draft_expense")
    @Builder.Default
    private boolean isDraftExpense = true;

    @Column(name = "hide_on_unpaid_expense_and_reports")
    @Builder.Default
    @NotAudited
    private boolean hideOnUnpaidExpenseAndReports = false;

    /**
     * flag indicating whether the juror has not attended court on a day they were due to be present (unauthorised
     * absence).
     */
    @Column(name = "no_show")
    private Boolean noShow;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_rates_id", referencedColumnName = "id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ExpenseRates expenseRates;


    /**
     * Do not save to this field it is auto generated by the database.
     */
    @Column(name = "total_due")
    @Setter(AccessLevel.NONE)
    @NotAudited
    @Generated
    private BigDecimal totalDue;


    /**
     * Do not save to this field it is auto generated by the database.
     */
    @Column(name = "total_paid")
    @Setter(AccessLevel.NONE)
    @NotAudited
    @Generated
    private BigDecimal totalPaid;

    /**
     * Sequence generated number to group jurors by when their attendance is confirmed. Uses either a 'P' prefix for
     * pool attendance (Jurors in waiting) or a 'J' prefix for jury attendance (serving on jury for a trial)
     */
    @Column(name = "attendance_audit_number")
    private String attendanceAuditNumber;

    /**
     * flag indicating if the appearance has been confirmed for the day via the juror management screen.
     */
    @Column(name = "appearance_confirmed")
    @NotAudited
    private boolean appearanceConfirmed;


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

    public BigDecimal getBalanceToPay() {
        return this.getTotalDue().subtract(this.getTotalPaid());
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

    public Boolean isExtraLongTrialDay() {
        return this.attendanceType.getIsExtraLongTrial();
    }

    public LocalTime getEffectiveTime() {
        return this.getTimeSpentAtCourt().plusNanos(this.getTravelTime().toNanoOfDay());
    }

    public LocalTime getTravelTime() {
        if (travelTime == null) {
            return LocalTime.of(0, 0);
        }
        return this.travelTime;
    }

    public BigDecimal getTotalFinancialLossDue() {
        return
            getOrZero(this.getLossOfEarningsDue())
                .add(getOrZero(this.getChildcareDue()))
                .add(getOrZero(this.getMiscAmountDue()));
    }

    public BigDecimal getTotalFinancialLossPaid() {
        return
            getOrZero(this.getLossOfEarningsPaid())
                .add(getOrZero(this.getChildcarePaid()))
                .add(getOrZero(this.getMiscAmountPaid()));
    }

    public BigDecimal getTotalTravelDue() {
        return
            getOrZero(this.getCarDue())
                .add(getOrZero(this.getMotorcycleDue()))
                .add(getOrZero(this.getBicycleDue()))
                .add(getOrZero(this.getParkingDue()))
                .add(getOrZero(this.getPublicTransportDue()))
                .add(getOrZero(this.getHiredVehicleDue()));
    }

    public BigDecimal getTotalTravelPaid() {
        return
            getOrZero(this.getCarPaid())
                .add(getOrZero(this.getMotorcyclePaid()))
                .add(getOrZero(this.getBicyclePaid()))
                .add(getOrZero(this.getParkingPaid()))
                .add(getOrZero(this.getPublicTransportPaid()))
                .add(getOrZero(this.getHiredVehiclePaid()));
    }

    public BigDecimal getSubsistenceTotalDue() {
        return getOrZero(this.getSubsistenceDue());
    }

    public BigDecimal getSubsistenceTotalPaid() {
        return getOrZero(this.getSubsistencePaid());
    }


    private BigDecimal getTotalSmartCardAmountPaid() {
        return getOrZero(this.getSmartCardAmountPaid());
    }

    private BigDecimal getTotalSmartCardAmountDue() {
        return getOrZero(this.getSmartCardAmountDue());
    }

    public BigDecimal getTotalChanged() {
        return getTotalDue().subtract(getTotalPaid());
    }

    public BigDecimal getFinancialLossTotalChanged() {
        return getTotalFinancialLossDue().subtract(getTotalFinancialLossPaid());
    }

    public BigDecimal getSubsistenceTotalChanged() {
        return getSubsistenceTotalDue()
            .subtract(getSubsistenceTotalPaid());
    }

    public BigDecimal getSmartCardTotalChanged() {
        if (AppearanceStage.EXPENSE_EDITED.equals(this.getAppearanceStage())
            || AppearanceStage.EXPENSE_AUTHORISED.equals(this.getAppearanceStage())) {
            //This will result in a negative as smart card for re-approval becomes a credit
            return getTotalSmartCardAmountDue()
                .subtract(getTotalSmartCardAmountPaid());
        }
        return getTotalSmartCardAmountDue();
    }

    public BigDecimal getTravelTotalChanged() {
        return getTotalTravelDue().subtract(getTotalTravelPaid());
    }

    public Map<String, Object> getExpensesWhereDueIsLessThenPaid() {
        Map<String, Object> errors = new HashMap<>();
        addExpenseToErrors(errors, "publicTransport", this.getPublicTransportDue(), this.getPublicTransportPaid());
        addExpenseToErrors(errors, "hiredVehicle", this.getHiredVehicleDue(), this.getHiredVehiclePaid());
        addExpenseToErrors(errors, "motorcycle", this.getMotorcycleDue(), this.getMotorcyclePaid());
        addExpenseToErrors(errors, "car", this.getCarDue(), this.getCarPaid());
        addExpenseToErrors(errors, "bicycle", this.getBicycleDue(), this.getBicyclePaid());
        addExpenseToErrors(errors, "parking", this.getParkingDue(), this.getParkingPaid());
        addExpenseToErrors(errors, "subsistence", this.getSubsistenceDue(), this.getSubsistencePaid());
        addExpenseToErrors(errors, "lossOfEarnings", this.getLossOfEarningsDue(), this.getLossOfEarningsPaid());
        addExpenseToErrors(errors, "childcare", this.getChildcareDue(), this.getChildcarePaid());
        addExpenseToErrors(errors, "miscAmount", this.getMiscAmountDue(), this.getMiscAmountPaid());
        addExpenseToErrors(errors, "total", this.getTotalDue(), this.getTotalPaid());
        if ((AppearanceStage.EXPENSE_EDITED.equals(this.getAppearanceStage())
            || AppearanceStage.EXPENSE_AUTHORISED.equals(this.getAppearanceStage()))
            && BigDecimalUtils.isLessThan(getOrZero(this.getSmartCardAmountPaid()),
            getOrZero(this.getSmartCardAmountDue()))) {
            errors.put("smartCardAmount",
                "Must be at most " + BigDecimalUtils.currencyFormat(getOrZero(this.getSmartCardAmountPaid())));
        }
        return errors;
    }

    void addExpenseToErrors(Map<String, Object> errors, String expenseName,
                            BigDecimal actualAmount, BigDecimal minAmount) {
        if (BigDecimalUtils.isLessThan(getOrZero(actualAmount), getOrZero(minAmount))) {
            errors.put(expenseName, "Must be at least " + BigDecimalUtils.currencyFormat(minAmount));
        }
    }

    public void setPayAttendanceType(PayAttendanceType payAttendanceType) {
        if (this.getAttendanceType() == null
            || Set.of(AttendanceType.ABSENT, AttendanceType.NON_ATTENDANCE, AttendanceType.NON_ATTENDANCE_LONG_TRIAL,
                      AttendanceType.NON_ATT_EXTRA_LONG_TRIAL)
            .contains(this.getAttendanceType())) {
            return;
        }
        setAttendanceType(
            payAttendanceType.getAttendanceType(this.isLongTrialDay(), this.isExtraLongTrialDay())
        );
    }

    public PayAttendanceType getPayAttendanceType() {
        return getAttendanceType().getPayAttendanceType();
    }

    public boolean isFullDay() {
        return getEffectiveTime().isAfter(LocalTime.of(4, 0));
    }

    public void clearExpenses(boolean validate) {
        if (validate) {
            validateCanClearExpenses();
        }

        clearFinancialLossExpenses(false);

        clearTravelExpenses(false);
        clearFoodAndDrinkExpenses(false);
    }

    public void clearFinancialLossExpenses(boolean validate) {
        if (validate) {
            validateCanClearExpenses();
        }
        setLossOfEarningsPaid(BigDecimal.ZERO);
        setChildcarePaid(BigDecimal.ZERO);
        setMiscAmountPaid(BigDecimal.ZERO);
    }


    public void clearTravelExpenses(boolean validate) {
        if (validate) {
            validateCanClearExpenses();
        }
        setPublicTransportPaid(BigDecimal.ZERO);
        setHiredVehiclePaid(BigDecimal.ZERO);
        setMotorcyclePaid(BigDecimal.ZERO);
        setCarPaid(BigDecimal.ZERO);
        setBicyclePaid(BigDecimal.ZERO);
        setParkingPaid(BigDecimal.ZERO);
    }

    public void clearFoodAndDrinkExpenses(boolean validate) {
        if (validate) {
            validateCanClearExpenses();
        }
        setSubsistencePaid(BigDecimal.ZERO);
        setSmartCardAmountPaid(BigDecimal.ZERO);
    }

    private void validateCanClearExpenses() {
        if (BigDecimalUtils.isGreaterThan(getTotalPaid(), BigDecimal.ZERO)
            || (appearanceStage != null && Set.of(AppearanceStage.EXPENSE_EDITED, AppearanceStage.EXPENSE_AUTHORISED)
            .contains(getAppearanceStage()))) {
            throw new MojException.InternalServerError(
                "Cannot clear expenses for appearance that has authorised values", null);
        }
    }

    public void setPublicTransportDue(BigDecimal publicTransportDue) {
        this.publicTransportDue = BigDecimalUtils.round(publicTransportDue, ROUNDING_PRECISION);
    }

    public void setPublicTransportPaid(BigDecimal publicTransportPaid) {
        this.publicTransportPaid = BigDecimalUtils.round(publicTransportPaid, ROUNDING_PRECISION);
    }

    public void setHiredVehicleDue(BigDecimal hiredVehicleDue) {
        this.hiredVehicleDue = BigDecimalUtils.round(hiredVehicleDue, ROUNDING_PRECISION);
    }

    public void setHiredVehiclePaid(BigDecimal hiredVehiclePaid) {
        this.hiredVehiclePaid = BigDecimalUtils.round(hiredVehiclePaid, ROUNDING_PRECISION);
    }

    public void setMotorcycleDue(BigDecimal motorcycleDue) {
        this.motorcycleDue = BigDecimalUtils.round(motorcycleDue, ROUNDING_PRECISION);
    }

    public void setMotorcyclePaid(BigDecimal motorcyclePaid) {
        this.motorcyclePaid = BigDecimalUtils.round(motorcyclePaid, ROUNDING_PRECISION);
    }

    public void setCarDue(BigDecimal carDue) {
        this.carDue = BigDecimalUtils.round(carDue, ROUNDING_PRECISION);
    }

    public void setCarPaid(BigDecimal carPaid) {
        this.carPaid = BigDecimalUtils.round(carPaid, ROUNDING_PRECISION);
    }

    public void setBicycleDue(BigDecimal bicycleDue) {
        this.bicycleDue = BigDecimalUtils.round(bicycleDue, ROUNDING_PRECISION);
    }

    public void setBicyclePaid(BigDecimal bicyclePaid) {
        this.bicyclePaid = BigDecimalUtils.round(bicyclePaid, ROUNDING_PRECISION);
    }

    public void setParkingDue(BigDecimal parkingDue) {
        this.parkingDue = BigDecimalUtils.round(parkingDue, ROUNDING_PRECISION);
    }

    public void setParkingPaid(BigDecimal parkingPaid) {
        this.parkingPaid = BigDecimalUtils.round(parkingPaid, ROUNDING_PRECISION);
    }

    public void setChildcareDue(BigDecimal childcareDue) {
        this.childcareDue = BigDecimalUtils.round(childcareDue, ROUNDING_PRECISION);
    }

    public void setChildcarePaid(BigDecimal childcarePaid) {
        this.childcarePaid = BigDecimalUtils.round(childcarePaid, ROUNDING_PRECISION);
    }

    public void setMiscAmountDue(BigDecimal miscAmountDue) {
        this.miscAmountDue = BigDecimalUtils.round(miscAmountDue, ROUNDING_PRECISION);
    }

    public void setMiscAmountPaid(BigDecimal miscAmountPaid) {
        this.miscAmountPaid = BigDecimalUtils.round(miscAmountPaid, ROUNDING_PRECISION);
    }

    public void setLossOfEarningsDue(BigDecimal lossOfEarningsDue) {
        this.lossOfEarningsDue = BigDecimalUtils.round(lossOfEarningsDue, ROUNDING_PRECISION);
    }

    public void setLossOfEarningsPaid(BigDecimal lossOfEarningsPaid) {
        this.lossOfEarningsPaid = BigDecimalUtils.round(lossOfEarningsPaid, ROUNDING_PRECISION);
    }

    public void setSubsistenceDue(BigDecimal subsistenceDue) {
        this.subsistenceDue = BigDecimalUtils.round(subsistenceDue, ROUNDING_PRECISION);
    }

    public void setSubsistencePaid(BigDecimal subsistencePaid) {
        this.subsistencePaid = BigDecimalUtils.round(subsistencePaid, ROUNDING_PRECISION);
    }

    public void setSmartCardAmountDue(BigDecimal smartCardAmountDue) {
        this.smartCardAmountDue = BigDecimalUtils.round(smartCardAmountDue, ROUNDING_PRECISION);
    }

    public void setSmartCardAmountPaid(BigDecimal smartCardAmountPaid) {
        this.smartCardAmountPaid = BigDecimalUtils.round(smartCardAmountPaid, ROUNDING_PRECISION);
    }

    @PrePersist
    private void prePersist() {
        preUpdate();
    }

    @PreUpdate
    private void preUpdate() {
        this.hideOnUnpaidExpenseAndReports = false;
    }
}
