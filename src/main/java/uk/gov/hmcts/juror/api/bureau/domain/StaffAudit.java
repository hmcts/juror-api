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
