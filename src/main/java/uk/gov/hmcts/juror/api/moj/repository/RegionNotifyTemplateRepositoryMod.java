package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.RegionNotifyTemplateMod;

import java.util.List;

/**
 * Repository for {@Link RegionalNotifyTemplateMod} entities.
 */

@Repository
public interface RegionNotifyTemplateRepositoryMod extends CrudRepository<RegionNotifyTemplateMod, Integer>,
    QuerydslPredicateExecutor<RegionNotifyTemplateMod> {

    List<RegionNotifyTemplateMod> findByRegionId(String regionId);
}
