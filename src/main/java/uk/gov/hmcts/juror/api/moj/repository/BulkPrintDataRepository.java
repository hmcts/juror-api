package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;

/**
 * Repository for {@link BulkPrintData}.
 */
@Repository
public interface BulkPrintDataRepository extends JpaRepository<BulkPrintData, Long>,
    QuerydslPredicateExecutor<BulkPrintData> {
    long countByJurorNo(String jurorNo);

}
