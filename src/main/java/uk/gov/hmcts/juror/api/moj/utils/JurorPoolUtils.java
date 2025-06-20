package uk.gov.hmcts.juror.api.moj.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class JurorPoolUtils {

    private JurorPoolUtils() {
    }

    /**
     * Bureau officers are authorised to view/read any records.
     * Jury officers are restricted to only view/read the records they 'own'
     *
     * @param jurorPool Associative entity linking a Juror with a Pool
     * @param owner     3-digit numeric string to uniquely identify a court location
     */
    public static void checkReadAccessForCurrentUser(JurorPool jurorPool, String owner) {
        log.trace("Enter checkReadAccessForCurrentUser");
        log.debug("Check current user's access (owner = {}) to read juror: {} in pool: {}",
            owner, jurorPool.getJuror().getJurorNumber(), jurorPool.getPoolNumber());

        if (!JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            // Current user is not a Bureau User, therefore check the user owns the record (to read or write to it)
            checkOwnershipForCurrentUser(jurorPool, owner);
        }

        log.trace("Exit checkReadAccessForCurrentUser");
    }

    /**
     * Bureau officers are authorised to view/read any records.
     * If there are potentially multiple active juror pool records across multiple court locations,
     * a jury officer at either court location can view the juror record providing they own at least one active juror
     * pool association
     *
     * @param jurorNumber 9-digit numeric string to identify Juror Record(s)
     * @param owner       3-digit numeric string to uniquely identify the court location the current user belongs to
     */
    public static void checkMultipleRecordReadAccess(final JurorPoolRepository jurorPoolRepository,
                                                     final String jurorNumber, final String owner) {
        log.trace("Enter checkMultipleRecordReadAccess");
        log.debug("Check current user's access (owner = {}) to read juror pool record(s): {}",
            owner, jurorNumber);

        List<JurorPool> jurorPools = getActiveJurorPoolRecords(jurorPoolRepository, jurorNumber);
        checkMultipleRecordReadAccess(jurorPools, owner);

        log.trace("Exit checkMultipleRecordReadAccess");
    }

    public static void checkMultipleRecordReadAccess(final List<JurorPool> jurorPools, final String owner) {
        log.trace("Enter checkMultipleRecordReadAccess");
        log.debug("Check current user's access (owner = {}) to read juror pool record(s)", owner);

        if (!JurorDigitalApplication.JUROR_OWNER.equals(owner)) {

            List<String> activeJurorPoolOwners = jurorPools.stream()
                .map(JurorPool::getOwner)
                .toList();

            if (!activeJurorPoolOwners.contains(owner)) {
                throw new MojException.Forbidden("User does not have access to view any of "
                    + "the supplied juror pool records", null);
            }
        }

        log.trace("Exit checkMultipleRecordReadAccess");
    }

    /**
     * Users are only authorised to edit a record they own.
     * Bureau user's can view any record but can only write to a record they own
     * Court users can only view a record they own as well as only write to a record they own.
     *
     * @param jurorPool Juror Record object
     * @param owner     3-digit numeric string to uniquely identify a court location
     */
    public static void checkOwnershipForCurrentUser(JurorPool jurorPool, String owner) {
        log.trace("Enter checkWriteAccessForCurrentUser");
        log.debug("Check if current user (owner = {}) owns juror pool record: {}",
            owner, jurorPool.getJuror().getJurorNumber());

        if (!jurorPool.getOwner().equals(owner)) {
            throw new MojException.Forbidden("Current user does not have sufficient permission to "
                + "view the juror pool record(s)", null);
        }

        log.trace("Exit checkWriteAccessForCurrentUser");
    }

    /**
     * Reusable utility function to query the database and return all active, editable juror pool associations for a
     * given juror number.
     *
     * @param jurorPoolRepository JPA interface to the database to generate and execute SQL queries
     * @param jurorNumber         9-digit numeric string to identify jurors
     * @return a collection (list) of juror pool association records. Although the juror number can uniquely identify
     *          an individual, sometimes one individual can have multiple, active, editable juror pool associative
     *          records, for example when they have been transferred.
     */
    public static List<JurorPool> getActiveJurorPoolRecords(JurorPoolRepository jurorPoolRepository,
                                                            String jurorNumber) {
        log.debug("Retrieving active juror records for juror number {}", jurorNumber);
        List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);

        if (jurorPools.isEmpty()) {
            throw new MojException.NotFound(String.format("Unable to find any Juror Pool associations for juror "
                + "number %s", jurorNumber), null);
        }

        log.debug("{} records retrieved for juror number {}", jurorPools.size(), jurorNumber);
        return jurorPools;
    }

    /**
     * Query the database and return the latest active, juror pool associations for a given juror number. Ordered by
     * start date descending (latest first).
     *
     * @param jurorPoolRepository JPA interface to the database to generate and execute SQL queries
     * @param jurorNumber         9-digit numeric string to identify jurors
     * @return a juror pool association record with the latest service start date for a given juror
     * @deprecated Use getActiveJurorPoolRecord(JurorPoolRepository jurorPoolRepository,
     *                                                      JurorPoolService jurorPoolService,
     *                                                      String jurorNumber)
     */
    @Deprecated(forRemoval = true)
    private static JurorPool getLatestActiveJurorPoolRecord(JurorPoolRepository jurorPoolRepository,
                                                           String jurorNumber) {
        log.debug("Retrieving active juror records for juror number {}", jurorNumber);
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        if (jurorPools.isEmpty()) {
            throw new MojException.NotFound(String.format("Unable to find any Juror Pool associations for juror "
                + "number %s", jurorNumber), null);
        }

        log.debug("{} records retrieved for juror number {}", jurorPools.size(), jurorNumber);
        return jurorPools.get(0);
    }

    /**
     * Reusable utility function to query the database and return the active juror pool association for a
     * given juror number and location.
     *
     * @param jurorPoolRepository JPA interface to the database to generate and execute SQL queries
     * @param jurorNumber         9-digit numeric string to identify jurors
     * @param courtLocation       court location of the juror
     * @return a juror pool record, there should be only one active record for a given location
     */
    public static JurorPool getActiveJurorPool(JurorPoolRepository jurorPoolRepository, String jurorNumber,
                                               CourtLocation courtLocation) {
        log.debug("Retrieving active juror pool record for juror number {} and location {}", jurorNumber,
            courtLocation);

        JurorPool jurorPool = jurorPoolRepository.findByJurorNumberAndIsActiveAndCourt(jurorNumber, true,
            courtLocation);

        if (jurorPool == null) {
            throw new MojException.NotFound(String.format("Unable to find any Juror Pool associations for juror "
                + "number %s at court location %s", jurorNumber, courtLocation.getLocCode()), null);
        }

        log.debug("retrieved juror pool record for juror number {} and location {}", jurorNumber, courtLocation);
        return jurorPool;
    }

    /**
     * To be used for retrieving a list of Jurors who are in a summoned/responded state from a specific pool and
     * court location.
     * <p/>
     * This method is particularly useful when transferring or reassigning jurors from a source pool and location.
     *
     * @param jurorNumbers        List of 9-digit numeric string to identify jurors
     * @param poolNumber          Pool number where jurors currently belong
     * @param courtLocation       Court location of jurors
     * @param jurorPoolRepository JPA interface to the database to generate and execute SQL queries
     */
    public static List<JurorPool> getSourceJurorsForPool(List<String> jurorNumbers, String poolNumber,
                                                         CourtLocation courtLocation,
                                                         JurorPoolRepository jurorPoolRepository) {

        List<Integer> validSourceStatusList = new ArrayList<>();
        validSourceStatusList.add(Math.toIntExact(IJurorStatus.SUMMONED));
        validSourceStatusList.add(Math.toIntExact(IJurorStatus.RESPONDED));

        log.debug("Find summoned/responded jurors from Pool: {} and location {}", poolNumber, courtLocation);

        return jurorPoolRepository.findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(jurorNumbers,
            true, poolNumber, courtLocation, validSourceStatusList);
    }

    /**
     * Sometimes there may be more than one active juror pool association record for a given juror number. This can
     * happen when a juror is transferred between courts - having an active record at both the sending (source) and
     * receiving (target) court locations.
     * If a bureau user is requesting court owned juror information, it doesn't matter which juror pool association
     * record is returned, this will return the first active record it finds, regardless of court location
     * (useful when viewing shared information such as the juror record or the summons reply)
     * <p/>
     * For court users, a single active juror pool record is expected to exist for any court under the jurisdiction of a
     * single primary court location. Even if a juror is reassigned to a secondary court location under the initial
     * primary court, the initial record should be marked as inactive, leaving just the one record active. Therefore,
     * the list of juror pools can be filtered based on the owner value of the current user's JWT and the owner of any
     * active juror pool records in the list to return just the record the current user has access to view.
     *
     * @param jurorPoolRepository JPA repository interface to execute SQL queries against the JurorPool entity/table
     * @param jurorNumber         9-digit numeric string identifying all jurors in the jurors list
     * @param owner               3-digit numeric string representing a court location. Used to determine user
     *                            permissions and filter the results
     * @return a single juror record that the current user has permission to view
     */
    public static JurorPool getActiveJurorPoolForUser(JurorPoolRepository jurorPoolRepository, String jurorNumber,
                                                      String owner) {
        log.trace("Enter getActiveJurorPoolForUser for juror number {} and owner {}", jurorNumber, owner);

        if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            /*
             * Bureau users may not own any juror pool associations if they have all been transferred so return the
             * latest one (ordered by service start date descending)
             */
            return getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
        }

        List<JurorPool> jurorPools = getActiveJurorPoolRecords(jurorPoolRepository, jurorNumber);

        return jurorPools.stream().filter(jp -> jp.getOwner().equalsIgnoreCase(owner)).findFirst()
            .orElseThrow(() -> new MojException.Forbidden(String.format("Current user (%s) does not own any Juror "
                + "Pool associations for Juror Number: %s", owner, jurorNumber), null));
    }

    public static JurorPool getActiveJurorPoolForUser(JurorPoolRepository jurorPoolRepository, String jurorNumber) {
        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(
            jurorNumber, true,
            SecurityUtil.getActiveOwner());

        if (jurorPool == null) {
            throw new MojException.NotFound(String.format("Unable to find any Juror Pool associations for juror "
                + "number %s at a court owned by %s", jurorNumber, SecurityUtil.getActiveOwner()), null);
        }
        return jurorPool;
    }

}
