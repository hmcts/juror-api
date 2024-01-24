package uk.gov.hmcts.juror.api.moj.domain.trial;

import jakarta.persistence.JoinColumn;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

public class PanelId {
    @JoinColumn(name = "juror_number", nullable = false)
    @JoinColumn(name = "pool_number", nullable = false)
    private JurorPool jurorPool;
    @JoinColumn(name = "loc_code")
    @JoinColumn(name = "trial_number")
    private Trial trial;

}
