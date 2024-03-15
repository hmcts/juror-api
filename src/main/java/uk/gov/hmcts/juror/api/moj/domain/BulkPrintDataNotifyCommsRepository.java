package uk.gov.hmcts.juror.api.moj.domain;


import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;

/**
 * Repository for {@link BulkPrintDataNotifyComms}.
 */

@Repository
public interface BulkPrintDataNotifyCommsRepository extends ReadOnlyRepository <BulkPrintDataNotifyComms, Long>,
    QuerydslPredicateExecutor<BulkPrintDataNotifyComms>{

}
