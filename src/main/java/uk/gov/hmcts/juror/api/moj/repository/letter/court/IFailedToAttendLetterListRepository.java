package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.FailedToAttendLetterList;

import java.util.List;

public interface IFailedToAttendLetterListRepository {
    List<FailedToAttendLetterList> findJurorsEligibleForFailedToAttendLetter(CourtLetterSearchCriteria searchCriteria,
                                                                             String owner);
}
