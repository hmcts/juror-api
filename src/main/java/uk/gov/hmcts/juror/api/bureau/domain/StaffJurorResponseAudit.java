package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

/**
 * Entity representing a row in the JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT table. Holds audit information for staff
 * assignments to juror responses.
 */
@Entity
@IdClass(StaffJurorResponseAuditKey.class)
@Table(name = "STAFF_JUROR_RESPONSE_AUDIT", schema = "JUROR_DIGITAL")
@Data
@Builder(builderMethodName = "realBuilder")
@NoArgsConstructor
@AllArgsConstructor
public class StaffJurorResponseAudit implements Serializable {
    /**
     * @param staffLogin          Login name of the staff member being assigned.
     * @param jurorNumber         Juror number of the juror response.
     * @param dateReceived        Date received of the juror response.
     * @param staffAssignmentDate Day for which staffLogin is assigned to the response.
     * @return builder populated with required arguments.
     */
    public static StaffJurorResponseAuditBuilder builder(final String teamLeaderLogin, final String staffLogin,
                                                         final String jurorNumber, final Date dateReceived,
                                                         final Date staffAssignmentDate) {
        return realBuilder()
            .teamLeaderLogin(teamLeaderLogin)
            .staffLogin(staffLogin)
            .jurorNumber(jurorNumber)
            .dateReceived(dateReceived)
            .staffAssignmentDate(staffAssignmentDate);
    }

    @NotEmpty
    @Size(min = 1, max = 20)
    @Id
    private String teamLeaderLogin;

    @Size(max = 20)
    @Id
    private String staffLogin;

    @NotEmpty
    @Pattern.List({
        @Pattern(regexp = JUROR_NUMBER),
        @Pattern(regexp = NO_PIPES_REGEX)
    })
    @Length(max = 9)
    @Id
    private String jurorNumber;

    @NotNull
    @Id
    private Date dateReceived;

    @NotNull
    private Date staffAssignmentDate;

    @NotNull
    @Id
    private Date created;

    @Version
    private Integer version;

    @PrePersist
    private void prePersist() {
        this.created = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
    }

}
