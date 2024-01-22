package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Composite key for {@link JurorResponseAudit}.
 */
@EqualsAndHashCode
public class JurorResponseAuditKey implements Serializable {
    private String jurorNumber;
    private Date changed;
}
