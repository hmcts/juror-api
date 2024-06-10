package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository for {@link StatsWelshOnlineResponse}.
 */
@Repository
@Deprecated(forRemoval = true)
public interface StatsWelshOnlineResponseRepository extends CrudRepository<StatsWelshOnlineResponse, Date> {

    List<StatsWelshOnlineResponse> findBySummonsMonthBetween(
        Date summonsMonthStart,
        Date summonsMonthEnd);
}
