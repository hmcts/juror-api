package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

public interface JurorHistoryService {

    void createPoliceCheckDisqualifyHistory(JurorPool jurorPool);

    void createPoliceCheckQualifyHistory(JurorPool jurorPool, boolean isChecked);

    void createPoliceCheckInProgressHistory(JurorPool jurorPool);

    void createCompleteServiceHistory(JurorPool jurorPool);

    void createPoliceCheckInsufficientInformationHistory(JurorPool jurorPool);

    void createFailedToAttendHistory(JurorPool jurorPool);

    void createUndoFailedToAttendHistory(JurorPool jurorPool);

    void createPendingJurorAuthorisedHistory(JurorPool jurorPool);
}
