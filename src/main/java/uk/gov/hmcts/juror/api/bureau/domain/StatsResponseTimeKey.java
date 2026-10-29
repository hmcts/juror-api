package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Composite key for {@link StatsResponseTime}.
 */
@EqualsAndHashCode
public class StatsResponseTimeKey implements Serializable {
    private LocalDate summonsMonth;
    private LocalDate responseMonth;
    private String responsePeriod;
    private String locCode;
    private String responseMethod;
}
