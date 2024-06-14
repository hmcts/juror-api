package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.juror.domain.THistoryCode;

@Repository
public interface THistoryCodeRepository extends ReadOnlyRepository<THistoryCode, String> {
}
