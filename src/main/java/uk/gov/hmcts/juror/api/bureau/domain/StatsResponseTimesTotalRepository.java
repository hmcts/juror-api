package uk.gov.hmcts.juror.api.bureau.domain;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@Link StatsResponseTimesTotals}.
 */
@Repository
public interface StatsResponseTimesTotalRepository extends CrudRepository<StatsResponseTimesTotals,
    StatsResponseTimesTotalsKey> {


}
