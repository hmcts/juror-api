package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.CertificateOfAttendanceLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.LetterListId;

@Repository
public interface CertificateOfAttendanceListRepository extends ICertificateOfAttendanceListRepository,
    ReadOnlyRepository<CertificateOfAttendanceLetterList, LetterListId> {

}
