package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Composite key for {@link PrintFile}.
 */
@EqualsAndHashCode
public class PrintFileKey implements Serializable {
    private String partNo;
    private String printFileName;
    private Date creationDate;
}
