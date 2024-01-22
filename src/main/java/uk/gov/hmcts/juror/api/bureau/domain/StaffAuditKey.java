package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Composite key for {@link StaffAudit}.
 */
@EqualsAndHashCode
public class StaffAuditKey implements Serializable {
    private StaffAmendmentAction action;
    private String editorLogin;
    private Date created;
}
