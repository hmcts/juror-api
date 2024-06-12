package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link StatsDeferrals}.
 **/
@Repository
@Deprecated(forRemoval = true)
public interface StatsDeferralsRepository extends CrudRepository<StatsDeferrals, String>,
    QuerydslPredicateExecutor<StatsDeferrals> {
    List<StatsDeferrals> findByWeekBetween(String startYearWeek, String endYearWeek);


}
