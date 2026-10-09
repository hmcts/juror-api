package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link StatsResponseTime}.
 */
@Repository
public interface StatsResponseTimeRepository extends CrudRepository<StatsResponseTime, StatsResponseTimeKey> {


    List<StatsResponseTime> findBySummonsMonthBetween(
        LocalDate summonsMonthStart,
        LocalDate summonsMonthEnd);

    List<StatsResponseTime> findBySummonsMonthEquals(LocalDate queryDate);

    List<StatsResponseTime> findBySummonsMonthIsGreaterThanEqual(LocalDate queryDate);

    List<StatsResponseTime> findAllBySummonsMonthEquals(LocalDate queryDate);

    List<StatsResponseTime> findByLocCodeEquals(String locCode);

    List<StatsResponseTime> findByResponseMethodEquals(String responseMethod);

}
