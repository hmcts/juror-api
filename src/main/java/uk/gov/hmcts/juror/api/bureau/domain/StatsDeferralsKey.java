package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Composite key for {@link StatsDeferrals}.
 */
@EqualsAndHashCode
@Deprecated(forRemoval = true)
public class StatsDeferralsKey implements Serializable {
    private String bureauOrCourt;
    private String execCode;
    private String calendarYear;
    private String financialYear;
    private String week;
}
