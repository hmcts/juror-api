package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "low_level_financial_audit_details_including_approved_amounts", schema = "juror_mod")
@IdClass(LowLevelFinancialAuditDetails.IdClass.class)
@Getter
@Setter(AccessLevel.NONE)
//Read only view
@Immutable
public class LowLevelFinancialAuditDetailsIncludingApprovedAmounts {
    @Column(name = "id")
    @Id
    private Long id;

    @Column(name = "total_approved")
    private BigDecimal totalApproved;

    @Column(name = "total_travel_approved")
    private BigDecimal totalTravelApproved;

    @Column(name = "total_financial_loss_approved")
    private BigDecimal totalFinancialLossApproved;

    @Column(name = "total_subsistence_approved")
    private BigDecimal totalSubsistenceApproved;

    @Column(name = "total_smartcard_approved")
    private BigDecimal totalSmartCardApproved;

    @Column(name = "juror_revision")
    private Long jurorRevision;

    @Column(name = "court_location_revision")
    private Long courtLocationRevision;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private FinancialAuditDetails.Type type;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on")
    private LocalDateTime createdOn;


    @Column(name = "created_on_date")
    private LocalDate createdOnDate;

    @Column(name = "juror_number")
    @Id
    private String jurorNumber;

    @Column(name = "attendance_date")
    @Id
    private LocalDate attendanceDate;

    @Column(name = "loc_code")
    @Id
    private String locCode;

    @Column(name = "pool_number")
    private String poolNumber;

    @Column(name = "trial_number")
    private String trialNumber;

    @Column(name = "appearance_stage")
    @Enumerated(EnumType.STRING)
    private AppearanceStage appearanceStage;

    @Column(name = "attendance_type")
    @Enumerated(EnumType.STRING)
    private AttendanceType attendanceType;

    @Column(name = "is_draft_expense")
    private Boolean isDraftExpense;

    @Column(name = "pay_cash")
    private Boolean payCash;

    @Column(name = "f_audit")
    private String financialAudit;

    @Column(name = "attendance_audit_number")
    private String attendanceAuditNumber;

    @Column(name = "total_travel_due")
    private BigDecimal totalTravelDue;

    @Column(name = "total_travel_paid")
    private BigDecimal totalTravelPaid;

    @Column(name = "total_financial_loss_due")
    private BigDecimal totalFinancialLossDue;

    @Column(name = "total_financial_loss_paid")
    private BigDecimal totalFinancialLossPaid;

    @Column(name = "total_subsistence_due")
    private BigDecimal totalSubsistenceDue;

    @Column(name = "total_subsistence_paid")
    private BigDecimal totalSubsistencePaid;

    @Column(name = "total_smart_card_due")
    private BigDecimal totalSmartCardDue;

    @Column(name = "total_smart_card_paid")
    private BigDecimal totalSmartCardPaid;

    @Column(name = "total_travel_outstanding")
    private BigDecimal totalTravelOutstanding;

    @Column(name = "total_financial_loss_outstanding")
    private BigDecimal totalFinancialLossOutstanding;

    @Column(name = "total_subsistence_outstanding")
    private BigDecimal totalSubsistenceOutstanding;

    @Column(name = "total_smartcard_outstanding")
    private BigDecimal totalSmartcardOutstanding;

    @Column(name = "total_due")
    private BigDecimal totalDue;

    @Column(name = "total_paid")
    private BigDecimal totalPaid;

    @Column(name = "total_outstanding")
    private BigDecimal totalOutstanding;

    public static class IdClass {
        private Long id;
        private String jurorNumber;
        private LocalDate attendanceDate;
        private String locCode;
    }
}
