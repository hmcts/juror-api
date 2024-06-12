package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Composite key for {@link StatsResponseTime}.
 */
@EqualsAndHashCode
@Deprecated(forRemoval = true)
public class StatsResponseTimeKey implements Serializable {
    private Date summonsMonth;
    private Date responseMonth;
    private String responsePeriod;
    private String locCode;
    private String responseMethod;
}
