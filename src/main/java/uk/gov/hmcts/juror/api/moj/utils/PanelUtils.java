package uk.gov.hmcts.juror.api.moj.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

@Slf4j
public final class PanelUtils {

    private PanelUtils() {
    }

    public static JurorPool getAssociatedJurorPool(JurorPoolRepository jurorPoolRepository, Panel panel) {

        if (panel.getTrial() == null) {
            throw new MojException.NotFound("No trial associated with panel", null);
        }

        return JurorPoolUtils.getActiveJurorPool(jurorPoolRepository, panel.getJurorNumber(),
            panel.getTrial().getCourtLocation());
    }

}
