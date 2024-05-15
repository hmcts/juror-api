package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface JurorRepository extends RevisionRepository<Juror, String, Long>, IJurorRepository,
    JpaRepository<Juror, String>, QuerydslPredicateExecutor<Juror> {

    Juror findByJurorNumber(String jurorNumber);

    /**
     * Function to return daily utilisation stats for jurors.
     */
    @Query(nativeQuery = true, value = "SELECT * from juror_mod.util_report_daily_summary( :LocCode, :fromDate, "
        + ":toDate)")
    List<String> callDailyUtilStats(@Param("LocCode") String locCode,
                                   @Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate) throws SQLException;


}
