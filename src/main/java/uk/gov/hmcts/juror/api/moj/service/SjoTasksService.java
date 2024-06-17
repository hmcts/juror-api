package uk.gov.hmcts.juror.api.moj.service;

public interface SjoTasksService {

    void undoFailedToAttendStatus(String jurorNumber, String poolNumber);

}
