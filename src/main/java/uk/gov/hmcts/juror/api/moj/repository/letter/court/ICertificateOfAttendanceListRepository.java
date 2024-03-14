package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.CertificateOfAttendanceLetterList;

import java.util.List;

public interface ICertificateOfAttendanceListRepository {

    List<CertificateOfAttendanceLetterList> findJurorsEligibleForCertificateOfAcceptanceLetter(CourtLetterSearchCriteria
                                                                                                   searchCriteria,
                                                                                               String owner);
}
