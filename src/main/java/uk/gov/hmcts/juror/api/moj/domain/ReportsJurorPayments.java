package uk.gov.hmcts.juror.api.moj.domain;

import groovy.transform.Immutable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.juror.api.moj.service.letter.court.CourtLetterService.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POOL_NUMBER;


@Entity
@Table(name = "reports_juror_payments_summary", schema = "juror_mod")
@NoArgsConstructor
@Getter
@AllArgsConstructor
@Immutable
@IdClass(ReportsJurorPaymentsKey.class)
public class ReportsJurorPayments extends Address implements Serializable {

    @Column(name = "trial_number")
    private String trialNumber;

    @Id
    @Column(name = "loc_code")
    private String locCode;

    @Id
    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Column(name = "non_attendance")
    private Boolean nonAttendance;

    @Id
    @Column(name = "juror_number")
    @Length(max = 9)
    @Pattern(regexp = JUROR_NUMBER)
    private String jurorNumber;

    @Column(name = "first_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String firstName;

    @Column(name = "last_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String lastName;

    @Column(name = "pool_number")
    @Pattern(regexp = POOL_NUMBER)
    private String poolNumber;

    @Column(name = "latest_payment_f_audit_id")
    private String latestPaymentFAuditId;

    @Column(name = "checked_in")
    private LocalTime checkedIn;

    @Column(name = "checked_out")
    private LocalTime checkedOut;

    @Column(name = "hours_attended")
    private LocalTime hoursAttended;

    @Column(name = "attendance_audit")
    private String attendanceAudit;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "total_travel_due")
    private BigDecimal totalTravelDue;

    @Column(name = "total_financial_loss_due")
    private BigDecimal totalFinancialLossDue;

    @Column(name = "subsistence_due")
    private BigDecimal subsistenceDue;

    @Column(name = "smart_card_due")
    private BigDecimal smartCardDue;

    @Column(name = "total_due")
    private BigDecimal totalDue;

    @Column(name = "total_paid")
    private BigDecimal totalPaid;
}
