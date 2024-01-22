package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.User;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

/**
 * Entity representing a row in the JUROR_DIGITAL.STAFF_AUDIT table. Holds audit information for staff profile changes.
 */
@Entity
@IdClass(StaffAuditKey.class)
@Table(name = "STAFF_AUDIT", schema = "JUROR_DIGITAL")
@Data
@Builder(builderMethodName = "realBuilder")
@NoArgsConstructor
@AllArgsConstructor
public class StaffAudit implements Serializable {
    /**
     * Amendment type.
     */
    @Id
    @NotNull
    @Column(name = "ACTION")
    @Enumerated(EnumType.STRING)
    private StaffAmendmentAction action;

    /**
     * Staff login of the editor (matches their entry in JUROR.PASSWORD).
     */
    @Id
    @NotEmpty
    @Column(name = "EDITOR_LOGIN")
    private String editorLogin;

    /**
     * Timestamp of audit entry creation.
     */
    @Id
    @Column(name = "CREATED")
    private Date created;

    /**
     * Staff member login (matches their entry in JUROR.PASSWORD).
     */
    @Column(name = "LOGIN")
    private String login;

    /**
     * Staff member name. (E.g. "Joe Bloggs")
     */
    @Column(name = "NAME")
    private String name;

    /**
     * Staff rank.
     */
    @Column(name = "RANK")
    private Integer rank;

    /**
     * Active status.
     */
    @Column(name = "ACTIVE")
    private Integer active;

    /**
     * Court location staff member covers.
     */
    @Column(name = "COURT_1")
    private String court1;

    /**
     * Court location staff member covers.
     */
    @Column(name = "COURT_2")
    private String court2;

    /**
     * Court location staff member covers.
     */
    @Column(name = "COURT_3")
    private String court3;

    /**
     * Court location staff member covers.
     */
    @Column(name = "COURT_4")
    private String court4;

    /**
     * Court location staff member covers.
     */
    @Column(name = "COURT_5")
    private String court5;

    /**
     * Court location staff member covers.
     */
    @Column(name = "COURT_6")
    private String court6;

    /**
     * Court location staff member covers.
     */
    @Column(name = "COURT_7")
    private String court7;

    /**
     * Court location staff member covers.
     */
    @Column(name = "COURT_8")
    private String court8;

    /**
     * Court location staff member covers.
     */
    @Column(name = "COURT_9")
    private String court9;

    /**
     * Court location staff member covers.
     */
    @Column(name = "COURT_10")
    private String court10;

    /**
     * Team id the staff member belonged to.
     */
    @Column(name = "TEAM_ID")
    private Long team;

    @Version
    private Integer version;

    @PrePersist
    private void prePersist() {
        this.created = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Build a complete audit entry for a staff member change history.
     *
     * @param audit       The staff object BEFORE changes (this is a historical audit).
     * @param action      Action being audited
     * @param editorLogin Username of the editor.
     * @return Persistable audit entity builder
     */
    public static StaffAuditBuilder builder(final User audit, final StaffAmendmentAction action,
                                            final String editorLogin) {

        final StaffAuditBuilder staffAuditBuilder = realBuilder()
            .action(action)
            .editorLogin(editorLogin)
            .login(audit.getUsername());

        switch (action) {
            case CREATE:
                // no previous values to audit
                return staffAuditBuilder;
            case EDIT:
                // audit the original values.
                return staffAuditBuilder.login(audit.getUsername())
                    .name(audit.getName())
                    .rank(audit.getLevel())
                    .active(audit.isActive() ? 1 : 0)
                    .court1(audit.getCourtAtIndex(0,null))
                    .court2(audit.getCourtAtIndex(1,null))
                    .court3(audit.getCourtAtIndex(2,null))
                    .court4(audit.getCourtAtIndex(3,null))
                    .court5(audit.getCourtAtIndex(4,null))
                    .court6(audit.getCourtAtIndex(5,null))
                    .court7(audit.getCourtAtIndex(6,null))
                    .court8(audit.getCourtAtIndex(7,null))
                    .court9(audit.getCourtAtIndex(8,null))
                    .court10(audit.getCourtAtIndex(9,null))
                    .team(audit.getTeam() != null
                        ?
                        audit.getTeam().getId()
                        :
                            null)
                    ;
            default:
                // unsupported action
                throw new IllegalArgumentException("StaffAmendmentAction " + action + " not supported!");
        }
    }
}
