package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;

@Repository
public interface PendingJurorStatusRepository extends ReadOnlyRepository<PendingJurorStatus, Character> {

}
