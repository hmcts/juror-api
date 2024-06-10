package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * Composite key for {@link StatsResponseTimesTotals}.
 */
@EqualsAndHashCode
@Deprecated(forRemoval = true)
public class StatsResponseTimesTotalsKey implements Serializable {
    private Integer onlineResponsesTotal;
}
