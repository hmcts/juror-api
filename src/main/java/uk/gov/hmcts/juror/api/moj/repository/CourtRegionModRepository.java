package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.CourtRegionMod;

/**
 * Repository for {@Link CourtRegionMod} entities}.
 */

@Repository
public interface CourtRegionModRepository extends CrudRepository<CourtRegionMod, Integer>,
    QuerydslPredicateExecutor<CourtRegionMod> {

    CourtRegionMod findByRegionId(String regionId);
}
