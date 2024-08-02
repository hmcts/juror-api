package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Composite key for {@link StatsNotResponded}.
 */
@EqualsAndHashCode
public class StatsNotRespondedKey implements Serializable {
    private Date summonsMonth;
    private String locCode;
}
