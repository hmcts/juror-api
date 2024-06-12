package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.UtilisationStats;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UtilisationStatsRepository extends CrudRepository<UtilisationStats,
    UtilisationStats.UtilisationStatsID> {
    List<UtilisationStats> findByMonthStartBetweenAndLocCode(LocalDate reportDateFrom, LocalDate reportDateTo,
                                                             String locCode);

    List<UtilisationStats> findTop12ByLocCodeOrderByMonthStartDesc(String locCode);
}
