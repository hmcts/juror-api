package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository for {@link StatsResponseTime}.
 */
@Repository
public interface StatsResponseTimeRepository extends CrudRepository<StatsResponseTime, StatsResponseTimeKey> {


    List<StatsResponseTime> findBySummonsMonthBetween(
        Date summonsMonthStart,
        Date summonsMonthEnd);

    List<StatsResponseTime> findBySummonsMonthEquals(Date queryDate);

    List<StatsResponseTime> findBySummonsMonthIsGreaterThanEqual(Date queryDate);

    List<StatsResponseTime> findAllBySummonsMonthEquals(Date queryDate);

    List<StatsResponseTime> findByLocCodeEquals(String locCode);

    List<StatsResponseTime> findByResponseMethodEquals(String responseMethod);

}
