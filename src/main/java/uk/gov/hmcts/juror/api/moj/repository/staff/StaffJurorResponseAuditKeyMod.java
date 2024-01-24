package uk.gov.hmcts.juror.api.moj.repository.staff;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorStaffAuditRepositoryMod;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Composite key for {@link JurorStaffAuditRepositoryMod}.
 */
@EqualsAndHashCode
public class StaffJurorResponseAuditKeyMod implements Serializable {
    private String teamLeaderLogin;
    private String staffLogin;
    private String jurorNumber;
    private LocalDate dateReceived;
    private LocalDateTime created;
}
