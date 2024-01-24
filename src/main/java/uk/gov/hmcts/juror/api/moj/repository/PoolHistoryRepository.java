package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;

import java.util.List;

@Repository
public interface PoolHistoryRepository extends JpaRepository<PoolHistory, Long>,
    QuerydslPredicateExecutor<PoolHistory>, IPoolHistoryRepository {

    List<PoolHistory> findByPoolNumberOrderByHistoryDateDesc(String poolNumber);

}
