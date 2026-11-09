package uk.gov.hmcts.juror.api.moj.domain.trial;

import jakarta.persistence.JoinColumn;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

@EqualsAndHashCode
public class PanelId {
    @JoinColumn(name = "juror_number", nullable = false)
    private Juror juror;
    @JoinColumn(name = "loc_code")
    @JoinColumn(name = "trial_number")
    private Trial trial;

}
