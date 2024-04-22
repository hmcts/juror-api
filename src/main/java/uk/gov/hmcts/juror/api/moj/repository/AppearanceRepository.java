package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface AppearanceRepository extends IAppearanceRepository, JpaRepository<Appearance, AppearanceId>,
    RevisionRepository<Appearance, AppearanceId, Long> {

    long countByJurorNumber(String jurorNumber);

    Optional<Appearance> findByJurorNumberAndAttendanceDate(String jurorNumber, LocalDate attendanceDate);


    List<Appearance> findAllByCourtLocationLocCodeAndJurorNumber(String locCode, String jurorNumber);

    List<Appearance> findAllByCourtLocationLocCodeAndJurorNumberAndAppearanceStageAndIsDraftExpenseTrueOrderByAttendanceDate(
        String locCode,
        String jurorNumber,
        AppearanceStage appearanceStage);

    @SuppressWarnings("checkstyle:LineLength")
    List<Appearance> findAllByJurorNumberAndAppearanceStageInAndCourtLocationOwnerAndIsDraftExpenseTrueOrderByAttendanceDateDesc(
        String jurorNumber,
        Set<AppearanceStage> stages,
        String owner);


    Optional<Appearance> findByCourtLocationLocCodeAndJurorNumberAndAttendanceDateAndDraftExpense(String locCode,
                                                                                                  String jurorNumber,
                                                                                                  LocalDate attendanceDate,
                                                                                                  boolean draftExpense);

    Optional<Appearance> findByJurorNumberAndPoolNumberAndAttendanceDate(String jurorNumber,
                                                                         String poolNumber, LocalDate attendanceDate);

    Optional<Appearance> findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(String locCode,
                                                                                   String jurorNumber,
                                                                                   LocalDate attendanceDate);

    List<Appearance> findByCourtLocationLocCodeAndJurorNumberAndIsDraftExpenseTrue(String locCode,
                                                                                   String jurorNumber);

    List<Appearance> findAllByCourtLocationLocCodeAndAppearanceStageAndPayCashAndIsDraftExpenseFalse(
        String locCode,
        AppearanceStage appearanceStage,
        boolean payCash);

    List<Appearance> findAllByJurorNumberAndPoolNumberAndAttendanceDateIn(String jurorNumber, String poolNumber,
                                                                          List<LocalDate> dates);

    List<Appearance> findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(String locCode,
                                                                                    String jurorNumber,
                                                                                    List<LocalDate> dates);


    @Query(value = "select nextval('juror_mod.attendance_audit_seq')", nativeQuery = true)
    Long getNextAttendanceAuditNumber();

    long countByJurorNumberAndAppearanceStageNotNull(String jurorNumber);

    Optional<Appearance> findByJurorNumberAndAttendanceDateAndAppearanceStage(String jurorNumber,
                                                                              LocalDate attendanceDate,
                                                                              AppearanceStage appearanceStage);


    List<Appearance> findAllByJurorNumberAndPoolNumber(String jurorNumber, String poolNumber);
}
