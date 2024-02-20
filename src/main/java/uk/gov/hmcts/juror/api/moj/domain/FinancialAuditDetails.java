package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_audit_details", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FinancialAuditDetails implements Serializable {
    private static final String F_AUDIT_GENERATOR_NAME = "appearance_f_audit_gen";
    public static final String F_AUDIT_PREFIX = "F";

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = F_AUDIT_GENERATOR_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = F_AUDIT_GENERATOR_NAME, schema = "juror_mod", sequenceName = "appearance_f_audit_seq",
        allocationSize = 1)
    @Getter(value = AccessLevel.NONE)
    private Long id;


    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "username")
    private User createdBy;

    @Column(name = "juror_revision")
    private Long jurorRevision;

    @Column(name = "court_location_revision")
    private Long courtLocationRevision;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;


    /**
     * In Heritage the financial audit number was prefixed by the letter "F" - in the new schema JPA will call a
     * global sequence to generate the next available audit number - but this will save a numeric value only in the
     * database. This getter will return the numeric value with the "F" prefix prepended for display in the UI.
     *
     * @return numeric f_audit number from the database prepended with the "F" prefix
     */
    public String getFinancialAuditNumber() {
        return F_AUDIT_PREFIX + this.id;
    }


    public enum Type {
        FOR_APPROVAL,
        APPROVED,
        EDIT
    }
}
