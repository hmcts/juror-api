package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import org.springframework.data.jpa.repository.Query;
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

    @Query(
        value = "WITH latest_stats AS ("
            + "  SELECT *, "
            + "         ROW_NUMBER() OVER (PARTITION BY loc_code ORDER BY month_start DESC) AS rn "
            + "  FROM juror_mod.utilisation_stats "
            + ") "
            + "SELECT ls.loc_code, cl.loc_court_name, ls.month_start, ls.available_days, ls.attendance_days, ls.sitting_days, ls.last_update "
            + "FROM latest_stats ls "
            + "join juror_mod.court_location cl on ls.loc_code = cl.loc_code "
            + "WHERE rn = 1",
        nativeQuery = true
    )
    List<Tuple> getCourtUtilisationStats();
}
