package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
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
    /**
     * Find expense payment appearances for the adjusted limits report.
     *
     * @param locCode Court location code
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @param stages Set of appearance stages to filter by (EXPENSE_ENTERED, EXPENSE_EDITED, EXPENSE_AUTHORISED)
     * @return List of appearances matching the criteria
     */

    @Query("SELECT a FROM Appearance a WHERE a.locCode = :locCode "
        + "AND a.attendanceDate BETWEEN :fromDate AND :toDate "
        + "AND a.isDraftExpense = false "
        + "AND a.appearanceStage IN :stages "
        + "ORDER BY a.attendanceDate DESC")
    List<Appearance> findExpensePaymentsForReport(
        @Param("locCode") String locCode,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("stages") Set<AppearanceStage> stages
    );

    /**
     * Find public transport expense payments for jurors affected by a specific court location revision.
     * Returns jurors who have public transport expenses in the last 360 days.
     *
     * @param courtLocationRevision The revision number from financial_audit_details
     * @param locCode The court location code
     * @return List of CSV strings: juror_number,first_name,last_name,pool_number,trial_number,total_paid
     */
    @Query(value = "SELECT a.juror_number, " +
        "j.first_name, " +
        "j.last_name, " +
        "a.pool_number, " +
        "a.trial_number, " +
        "SUM(a.total_paid) " +
        "FROM juror_mod.appearance a " +
        "JOIN juror_mod.juror j ON a.juror_number = j.juror_number " +
        "WHERE a.juror_number IN ( " +
        "    SELECT DISTINCT fad.juror_number " +
        "    FROM juror_mod.financial_audit_details fad " +
        "    WHERE fad.court_location_revision = :courtLocationRevision " +
        "    AND fad.loc_code = :locCode " +
        ") " +
        "AND a.public_transport_total_due IS NOT NULL " +
        "AND a.public_transport_total_due > 0 " +
        "AND a.attendance_date > CURRENT_DATE - 360 " +
        "GROUP BY a.juror_number, j.first_name, j.last_name, a.pool_number, a.trial_number " +
        "ORDER BY a.juror_number, a.pool_number",
        nativeQuery = true)
    List<String> findPublicTransportExpensesByRevision(
        @Param("courtLocationRevision") Long courtLocationRevision,
        @Param("locCode") String locCode);

    /**
     * Find taxi/hired vehicle expense payments for jurors affected by a specific court location revision.
     * Returns jurors who have taxi expenses in the last 360 days.
     *
     * @param courtLocationRevision The revision number from financial_audit_details
     * @param locCode The court location code
     * @return List of CSV strings: juror_number,first_name,last_name,pool_number,trial_number,total_paid
     */
    @Query(value = "SELECT a.juror_number, " +
        "j.first_name, " +
        "j.last_name, " +
        "a.pool_number, " +
        "a.trial_number, " +
        "SUM(a.total_paid) " +
        "FROM juror_mod.appearance a " +
        "JOIN juror_mod.juror j ON a.juror_number = j.juror_number " +
        "WHERE a.juror_number IN ( " +
        "    SELECT DISTINCT fad.juror_number " +
        "    FROM juror_mod.financial_audit_details fad " +
        "    WHERE fad.court_location_revision = :courtLocationRevision " +
        "    AND fad.loc_code = :locCode " +
        ") " +
        "AND a.hired_vehicle_total_due IS NOT NULL " +
        "AND a.hired_vehicle_total_due > 0 " +
        "AND a.attendance_date > CURRENT_DATE - 360 " +
        "GROUP BY a.juror_number, j.first_name, j.last_name, a.pool_number, a.trial_number " +
        "ORDER BY a.juror_number, a.pool_number",
        nativeQuery = true)
    List<String> findTaxiExpensesByRevision(
        @Param("courtLocationRevision") Long courtLocationRevision,
        @Param("locCode") String locCode);
}
