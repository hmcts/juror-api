package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Voters;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VotersRepository extends JpaRepository<Voters, Voters.VotersId>,
    QuerydslPredicateExecutor<Voters> {

    /**
     * Function to randomly select a number of voters from the Voters table.
     * The function returns the juror number of the selected voters.
     */
    @Query(nativeQuery = true, value = "SELECT * from juror_mod.get_voters( :required, :minDate, :maxDate, :LocCode, "
        + ":areaCodeList, :poolType)")
    List<String> callGetVoters(@Param("required") Integer required,
                               @Param("minDate") LocalDate minDate,
                               @Param("maxDate") LocalDate maxDate,
                               @Param("LocCode") String locCode,
                               @Param("areaCodeList") String areaCodeList,
                               @Param("poolType") String poolType) throws SQLException;

    Voters findByJurorNumber(String jurorNumber);

}
