package uk.gov.hmcts.juror.api.moj.service;

public interface RevisionService {

    Long getLatestCourtRevisionNumber(String locCode);

    Long getLatestJurorRevisionNumber(String jurorNumber);
}
