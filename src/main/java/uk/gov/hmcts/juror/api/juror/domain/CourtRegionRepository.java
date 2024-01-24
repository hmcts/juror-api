package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@Link CourtRegion} entities.
 */

@Repository
public interface CourtRegionRepository extends CrudRepository<CourtRegion, String>,
    QuerydslPredicateExecutor<CourtRegion> {

    CourtRegion findByRegionId(String regionId);
}
