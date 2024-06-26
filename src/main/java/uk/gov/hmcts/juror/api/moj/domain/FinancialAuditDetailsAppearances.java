package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "financial_audit_details_appearances", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@IdClass(FinancialAuditDetailsAppearances.IdClass.class)
public class FinancialAuditDetailsAppearances implements Serializable {

    @Column(name = "financial_audit_id")
    @NotNull
    @Id
    private Long financialAuditId;

    @Id
    @NotNull
    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Column(name = "appearance_version")
    @NotNull
    @Id
    private Long appearanceVersion;

    @Column(name = "loc_code")
    @Id
    private String locCode;

    @Column(name = "last_approved_faudit")
    private Long lastApprovedFAudit;

    @AllArgsConstructor
    @NoArgsConstructor
    public static final class IdClass {
        private Long financialAuditId;
        private LocalDate attendanceDate;
        private Long appearanceVersion;
        private String locCode;
    }
}
