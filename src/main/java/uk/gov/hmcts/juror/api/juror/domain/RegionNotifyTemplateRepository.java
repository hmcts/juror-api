package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@Link RegionalNotifyTemplate} entities.
 */

@Repository
public interface RegionNotifyTemplateRepository extends CrudRepository<RegionNotifyTemplate, String>,
    QuerydslPredicateExecutor<RegionNotifyTemplate> {
    //  List<RegionNotifyTemplate> findByRegionIdAndLegacyTemplateIdAndMessageFormat(String regionId,Integer
    //  legacyTemplateId,String messageFormat);
    List<RegionNotifyTemplate> findByRegionId(String regionId);
}
