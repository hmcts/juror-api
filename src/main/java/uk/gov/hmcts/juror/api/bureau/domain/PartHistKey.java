package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Composite key for {@link PartHist}.
 */
@EqualsAndHashCode
public class PartHistKey implements Serializable {
    private String jurorNumber;
    private String owner;
    private Date lastUpdate;
    private Date datePart;
    private String historyCode;
    private String userId;
    private String info;
    private String poolNumber;
}
