package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link StatsExcusals}.
 **/
@Repository
@Deprecated(forRemoval = true)
public interface StatsExcusalsRepository extends CrudRepository<StatsExcusals, StatsExcusalsKey>,
    QuerydslPredicateExecutor<StatsExcusals> {

    List<StatsExcusals> findByWeekBetween(String startYearWeek, String endYearWeek);

}
