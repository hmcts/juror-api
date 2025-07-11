package uk.gov.hmcts.juror.api.moj.repository.trial;

import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;

import java.time.LocalDate;
import java.util.List;

public interface IPanelRepository {
    List<Panel> retrieveMembersOnTrial(String trialNumber, String locationCode);

    boolean isEmpanelledJuror(String jurorNumber, String locationCode, LocalDate date);

    long getCountPanelledJurors(String locCode);
}
