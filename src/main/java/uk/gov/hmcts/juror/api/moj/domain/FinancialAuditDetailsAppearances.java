package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.io.Serializable;
import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;

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


    @Column(name = "appearance_version")
    @NotNull
    @Id
    private Long appearanceVersion;
}
