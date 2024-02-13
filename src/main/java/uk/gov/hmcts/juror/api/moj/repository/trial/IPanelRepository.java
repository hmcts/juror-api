package uk.gov.hmcts.juror.api.moj.repository.trial;

import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;

import java.util.List;

public interface IPanelRepository {
    List<Panel> retrieveMembersOnTrial(String trialNumber, String locationCode);
}
