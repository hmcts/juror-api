package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.StatsSittingDays;

import java.util.List;

@Repository
public interface StatsSittingDaysRepository extends CrudRepository<StatsSittingDays,
    StatsSittingDays.StatsSittingDaysId> {

    @Query(value = "SELECT "
        + "ssd.court_code AS courtCode, "
        + "ssd.court_name AS courtName, "
        + "ssd.sitting_days_category AS sittingDaysCategory, "
        + "CAST(COALESCE(SUM(ssd.number_of_sitting_days), 0) AS integer) AS numberOfSittingDays, "
        + "CAST(COALESCE(SUM(ssd.number_of_jurors), 0) AS integer) AS numberOfJurors "
        + "FROM juror_mod.stats_sitting_days ssd "
        + "WHERE ssd.service_month BETWEEN :fromMonth AND :toMonth "
        + "AND ssd.court_code IN (:courtCodes) "
        + "GROUP BY ssd.court_code, ssd.court_name, ssd.sitting_days_category "
        + "ORDER BY ssd.court_name, ssd.sitting_days_category",
        nativeQuery = true)
    List<SittingDaysStatsData> findStatsByMonthRangeAndCourtCodes(@Param("fromMonth") String fromMonth,
                                                                   @Param("toMonth") String toMonth,
                                                                   @Param("courtCodes") List<String> courtCodes);

    @Query(value = "SELECT "
        + "ssd.court_code AS courtCode, "
        + "ssd.court_name AS courtName, "
        + "ssd.sitting_days_category AS sittingDaysCategory, "
        + "CAST(COALESCE(SUM(ssd.number_of_sitting_days), 0) AS integer) AS numberOfSittingDays, "
        + "CAST(COALESCE(SUM(ssd.number_of_jurors), 0) AS integer) AS numberOfJurors "
        + "FROM juror_mod.stats_sitting_days ssd "
        + "WHERE ssd.service_month BETWEEN :fromMonth AND :toMonth "
        + "GROUP BY ssd.court_code, ssd.court_name, ssd.sitting_days_category "
        + "ORDER BY ssd.court_name, ssd.sitting_days_category",
        nativeQuery = true)
    List<SittingDaysStatsData> findStatsByMonthRange(@Param("fromMonth") String fromMonth,
                                                     @Param("toMonth") String toMonth);

    interface SittingDaysStatsData {
        String getCourtCode();

        String getCourtName();

        String getSittingDaysCategory();

        Integer getNumberOfSittingDays();

        Integer getNumberOfJurors();
    }
}
