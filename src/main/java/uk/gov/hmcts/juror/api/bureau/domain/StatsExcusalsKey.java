package uk.gov.hmcts.juror.api.bureau.domain;


import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Composite key for {@link StatsExcusals}.
 */
@EqualsAndHashCode


public class StatsExcusalsKey implements Serializable {

    private String bureauOrCourt;
    private String execCode;
    private String calendarYear;
    private String financialYear;
    private String week;
}
