package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Composite key for {@link StaffJurorResponseAudit}.
 */
@EqualsAndHashCode
public class StaffJurorResponseAuditKey implements Serializable {
    private String teamLeaderLogin;
    private String staffLogin;
    private String jurorNumber;
    private Date dateReceived;
    private Date created;
}
