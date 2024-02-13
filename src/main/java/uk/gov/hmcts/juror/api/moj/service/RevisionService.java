package uk.gov.hmcts.juror.api.moj.service;

import org.springframework.data.history.Revision;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

public interface RevisionService {


    Revision<Long, CourtLocation> getLatestCourtRevision(String locCode);

    Revision<Long, Juror> getLatestJurorRevision(String jurorNumber);
}
