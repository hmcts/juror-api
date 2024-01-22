package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.repository.staff.StaffJurorResponseAuditKeyMod;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "staff_juror_response_audit", schema = "juror_mod")
@Getter
@Setter
@IdClass(StaffJurorResponseAuditKeyMod.class)
@Builder(builderMethodName = "realBuilder")
public class StaffJurorResponseAuditMod {
    @Column(name = "team_leader_login")
    @Id
    private String teamLeaderLogin;

    @Column(name = "staff_login")
    @Id
    private String staffLogin;

    @NotEmpty
    @Pattern(regexp = JUROR_NUMBER)
    @Length(max = 9)
    @Id
    @Column(name = "juror_number")
    private String jurorNumber;

    @Column(name = "date_received")
    @Id
    private LocalDate dateReceived;

    @Column(name = "staff_assignment_date")
    private LocalDate staffAssignmentDate;

    @Column(name = "created")
    @Id
    private LocalDateTime created;

    @Column(name = "version")
    private Integer version;

    @PrePersist
    void prePersist() {
        this.created = LocalDateTime.now();
    }

    public static StaffJurorResponseAuditMod.StaffJurorResponseAuditModBuilder builder(
        final String teamLeaderLogin,
        final String staffLogin,
        final String jurorNumber,
        final LocalDate dateReceived,
        final LocalDate staffAssignmentDate) {
        return realBuilder()
            .teamLeaderLogin(teamLeaderLogin)
            .staffLogin(staffLogin)
            .jurorNumber(jurorNumber)
            .dateReceived(dateReceived)
            .staffAssignmentDate(staffAssignmentDate);
    }
}
