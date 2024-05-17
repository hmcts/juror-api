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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "financial_audit_details_appearances", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Data
@IdClass(FinancialAuditDetailsAppearances.class)
public class FinancialAuditDetailsAppearances implements Serializable {

    @Column(name = "financial_audit_id")
    @NotNull
    @Id
    private Long financialAuditId;

    @Column(name = "loc_code")
    @Id
    private String locCode;

    @Id
    @NotNull
    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Column(name = "appearance_version")
    @NotNull
    @Id
    private Long appearanceVersion;
}
