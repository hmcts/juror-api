package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.PoolTransferWeekday;

@Repository
public interface PoolTransferDayRepository extends ReadOnlyRepository<PoolTransferWeekday, String> {
    java.util.Optional<PoolTransferWeekday> findByTransferDayAndRunDayIgnoreCase(String transferDay, String runDay);
}
