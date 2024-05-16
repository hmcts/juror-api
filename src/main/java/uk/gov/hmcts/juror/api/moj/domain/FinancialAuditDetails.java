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
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "financial_audit_details", schema = "juror_mod")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@ToString
public class FinancialAuditDetails implements Serializable {
    private static final String F_AUDIT_GENERATOR_NAME = "appearance_f_audit_gen";
    public static final String F_AUDIT_PREFIX = "F";

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = F_AUDIT_GENERATOR_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = F_AUDIT_GENERATOR_NAME, schema = "juror_mod", sequenceName = "appearance_f_audit_seq",
        allocationSize = 1)
    private Long id;


    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "username")
    private User createdBy;

    @Column(name = "juror_number")
    private String jurorNumber;

    @Column(name = "juror_revision")
    private Long jurorRevision;

    @Column(name = "loc_code")
    private String locCode;

    @Column(name = "court_location_revision")
    private Long courtLocationRevision;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;


    @OneToMany
    @JoinColumn(name = "financial_audit_id", referencedColumnName = "id")
    private List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances;


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


    @Getter
    public enum Type {
        FOR_APPROVAL(GenericType.FOR_APPROVAL),
        APPROVED_CASH(GenericType.APPROVED),
        APPROVED_BACS(GenericType.APPROVED),
        REAPPROVED_CASH(GenericType.APPROVED),
        REAPPROVED_BACS(GenericType.APPROVED),
        FOR_APPROVAL_EDIT(GenericType.EDIT),
        APPROVED_EDIT(GenericType.EDIT);

        private final GenericType genericType;

        Type(GenericType genericType) {
            this.genericType = genericType;
        }

        public enum GenericType {
            FOR_APPROVAL,
            APPROVED,
            EDIT;

            public Set<Type> getTypes() {
                return Arrays.stream(Type.values())
                    .filter(type -> type.getGenericType().equals(this))
                    .collect(Collectors.toSet());
            }
        }
    }
}
