package uk.gov.hmcts.juror.api.moj.service;

@FunctionalInterface
public interface SjoTasksService {

    void undoFailedToAttendStatus(String jurorNumber);

}
