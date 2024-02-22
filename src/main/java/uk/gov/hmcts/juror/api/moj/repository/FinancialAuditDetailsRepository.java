package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface FinancialAuditDetailsRepository extends CrudRepository<FinancialAuditDetails, Long> {

    @Query("SELECT fad FROM FinancialAuditDetails fad "
        + "JOIN FinancialAuditDetailsAppearances fada on "
        + "fad.id = fada .financialAuditId "
        + "WHERE fada.jurorNumber=?1 "
        + "AND fada.attendanceDate=?2 "
        + "AND fada.courtLocation.locCode=?3 "
        + "AND fada.appearanceVersion =?4")
    List<FinancialAuditDetails> findAllByAppearance(String jurorNumber, LocalDate attendanceDate,
                                                    String courtLocation, Long appearanceVersion);

    default List<FinancialAuditDetails> findAllByAppearance(Appearance appearance) {
        return findAllByAppearance(appearance.getJurorNumber(),
            appearance.getAttendanceDate(),
            appearance.getCourtLocation().getLocCode(),
            appearance.getVersion());
    }
}
