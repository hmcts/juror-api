package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.UtilisationStats;

@Repository
public interface UtilisationStatsRepository extends CrudRepository<UtilisationStats,
    UtilisationStats.UtilisationStatsID> {

}
