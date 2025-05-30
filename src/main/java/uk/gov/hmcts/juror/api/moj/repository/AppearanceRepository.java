package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
@SuppressWarnings("LineLength")
public interface AppearanceRepository extends IAppearanceRepository, JpaRepository<Appearance, AppearanceId>,
    RevisionRepository<Appearance, AppearanceId, Long> {

    long countByJurorNumber(String jurorNumber);

    List<Appearance> findAllByCourtLocationLocCodeAndJurorNumber(String locCode, String jurorNumber);

    List<Appearance> findAllByJurorNumber(String jurorNumber);

    List<Appearance> findAllByCourtLocationLocCodeAndJurorNumberAndAppearanceStageAndIsDraftExpenseTrueOrderByAttendanceDate(
        String locCode,
        String jurorNumber,
        AppearanceStage appearanceStage);


    List<Appearance> findAllByCourtLocationLocCodeAndJurorNumberAndAppearanceStageIn(String locCode,
                                                                                     String jurorNumber,
                                                                                     Set<AppearanceStage> appearanceStages);

    List<Appearance> findAllByJurorNumberAndAppearanceStageInAndCourtLocationOwnerAndIsDraftExpenseTrueOrderByAttendanceDateDesc(
        String jurorNumber,
        Set<AppearanceStage> stages,
        String owner);


    Optional<Appearance> findByCourtLocationLocCodeAndJurorNumberAndAttendanceDateAndIsDraftExpense(String locCode,
                                                                                                    String jurorNumber,
                                                                                                    LocalDate attendanceDate,
                                                                                                    boolean draftExpense);

    Optional<Appearance> findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(String locCode,
                                                                                   String jurorNumber,
                                                                                   LocalDate attendanceDate);

    List<Appearance> findByCourtLocationLocCodeAndJurorNumberAndIsDraftExpenseTrue(String locCode,
                                                                                   String jurorNumber);

    List<Appearance> findByCourtLocationLocCodeAndJurorNumberAndIsDraftExpenseTrueAndAppearanceStageIn(String locCode,
                                                                                                       String jurorNumber,
                                                                                                       Set<AppearanceStage> appearanceStages);

    @Query("select a from Appearance a where a.courtLocation.locCode = ?1 "
        + "and a.appearanceStage = ?2 "
        + "and a.payCash = ?3 "
        + "and a.isDraftExpense = false "
        + "and a.hideOnUnpaidExpenseAndReports = false")
    List<Appearance> findAllByCourtLocationLocCodeAndAppearanceStageAndPayCashAndIsDraftExpenseFalse(
        String locCode,
        AppearanceStage appearanceStage,
        boolean payCash);

    List<Appearance> findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateInOrderByAttendanceDate(String locCode,
                                                                                                         String jurorNumber,
                                                                                                         List<LocalDate> dates);


    @Query(value = "select nextval('juror_mod.attendance_audit_seq')", nativeQuery = true)
    Long getNextAttendanceAuditNumber();

    @Query("select count(*) from Appearance a where a.jurorNumber= ?1 "
        + "and a.appearanceStage is not null "
        + "and( a.attendanceType is null or a.attendanceType not in (AttendanceType.ABSENT))")
    long countNoneAbsentAttendances(String jurorNumber);

    @Query("select count(*) from Appearance a where a.jurorNumber= ?1 and a.poolNumber = ?2")
    long countAttendancesInPool(String jurorNumber, String poolNumber);

    Optional<Appearance> findByLocCodeAndJurorNumberAndAttendanceDateAndAppearanceStage(String locCode,
                                                                                        String jurorNumber,
                                                                                        LocalDate attendanceDate,
                                                                                        AppearanceStage appearanceStage);


    Optional<Appearance> findFirstByAttendanceAuditNumberEqualsAndLocCodeIn(String auditNumber,
                                                                            Collection<String> locCode);

    Optional<Appearance> findByLocCodeAndJurorNumberAndAttendanceDate(String locCode, String jurorNumber,
                                                                      LocalDate attendanceDate);

    List<Appearance> findByLocCodeAndAttendanceDateAndTrialNumber(String locCode, LocalDate attendanceDate,
                                                                  String trialNumber);

    @Query(value = "select max(a.version) from juror_mod.appearance_audit a where "
        + "a.juror_number = ?1 and a.attendance_date = ?2 and a.loc_code = ?3", nativeQuery = true)
    Long getLastVersionNumber(String jurorNumber, LocalDate date, String locCode);

    List<Appearance> findAllByJurorNumberAndAttendanceDateGreaterThanEqualAndLocCodeOrderByAttendanceDateDesc(String jurorNumber,
                                                                                              LocalDate date,
                                                                                              String locCodes);
}
