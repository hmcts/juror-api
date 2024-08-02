package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository for {@link StatsNotResponded}.
 */
@Repository
public interface StatsNotRespondedRepository extends CrudRepository<StatsNotResponded, StatsNotRespondedKey> {

    List<StatsNotResponded> findBySummonsMonthBetween(
        Date summonsMonthStart,
        Date summonsMonthEnd);
}
