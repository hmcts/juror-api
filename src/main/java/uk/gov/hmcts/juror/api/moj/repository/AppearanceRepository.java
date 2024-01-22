package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppearanceRepository extends
    IAppearanceRepository, JpaRepository<Appearance, AppearanceId>,
    RevisionRepository<Appearance, AppearanceId, Long> {

    long countByJurorNumber(String jurorNumber);

    Appearance findByJurorNumber(String jurorNumber);

    Appearance findByJurorNumberAndAttendanceDate(String jurorNumber, LocalDate attendanceDate);

    List<Appearance> findAllByJurorNumberAndFinancialAuditDetailsId(String jurorNumber, long financialAuditDetails);

    List<Appearance> findAllByJurorNumberAndPoolNumber(String jurorNumber, String poolNumber);
}
