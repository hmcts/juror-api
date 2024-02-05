package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface AppearanceRepository extends
    IAppearanceRepository, JpaRepository<Appearance, AppearanceId>,
    RevisionRepository<Appearance, AppearanceId, Long> {

    long countByJurorNumber(String jurorNumber);

    Appearance findByJurorNumber(String jurorNumber);

    Appearance findByJurorNumberAndAttendanceDate(String jurorNumber, LocalDate attendanceDate);

    List<Appearance> findAllByJurorNumberAndFinancialAuditDetailsId(String jurorNumber, long financialAuditDetails);

    List<Appearance> findAllByJurorNumberAndPoolNumber(String jurorNumber, String poolNumber);

    Optional<Appearance> findByJurorNumberAndPoolNumberAndAttendanceDateAndIsDraftExpenseTrue(String jurorNumber,
                                                                                              String poolNumber,
                                                                                              LocalDate attendanceDate);

    Optional<Appearance> findByJurorNumberAndPoolNumberAndAttendanceDate(String jurorNumber,
                                                                         String poolNumber, LocalDate attendanceDate);

    List<Appearance> findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(String jurorNumber, String poolNumber);
}
