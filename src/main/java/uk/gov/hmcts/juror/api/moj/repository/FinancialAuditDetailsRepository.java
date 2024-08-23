package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface FinancialAuditDetailsRepository
    extends CrudRepository<FinancialAuditDetails, FinancialAuditDetails.IdClass>,
    IFinancialAuditDetailsRepository {

    @Query("SELECT fad FROM FinancialAuditDetails fad "
        + "JOIN FinancialAuditDetailsAppearances fada on "
        + "fad.id = fada .financialAuditId "
        + "and fad.locCode = fada.locCode "
        + "WHERE fad.jurorNumber=?1 "
        + "AND fada.attendanceDate=?2 "
        + "AND fad.locCode=?3 "
        + "AND fada.appearanceVersion =?4")
    List<FinancialAuditDetails> findAllByAppearance(String jurorNumber, LocalDate attendanceDate,
                                                    String courtLocation, Long appearanceVersion);

    default List<FinancialAuditDetails> findAllByAppearance(Appearance appearance) {
        return findAllByAppearance(appearance.getJurorNumber(),
            appearance.getAttendanceDate(),
            appearance.getCourtLocation().getLocCode(),
            appearance.getVersion());
    }

    @Query("SELECT fad FROM FinancialAuditDetails fad "
        + "JOIN FinancialAuditDetailsAppearances fada on "
        + "fad.id = fada .financialAuditId "
        + "and fad.locCode = fada.locCode "
        + "WHERE fad.jurorNumber=?1 "
        + "AND fada.attendanceDate=?2 "
        + "AND fad.locCode=?3 "
        + "AND fad.type in ?4 "
        + "ORDER BY fad.createdOn DESC "
        + "LIMIT 1 ")
    Optional<FinancialAuditDetails> findLastFinancialAuditDetailsByType(
        String jurorNumber, LocalDate attendanceDate, String courtLocation,
        Collection<FinancialAuditDetails.Type> type);
}
