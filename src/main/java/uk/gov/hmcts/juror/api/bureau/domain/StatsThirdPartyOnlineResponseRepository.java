package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository for {@link StatsThirdPartyOnlineResponse}.
 */
@Repository
@Deprecated(forRemoval = true)
public interface StatsThirdPartyOnlineResponseRepository extends CrudRepository<StatsThirdPartyOnlineResponse, Date> {

    List<StatsThirdPartyOnlineResponse> findBySummonsMonthBetween(
        Date summonsMonthStart,
        Date summonsMonthEnd);
}
