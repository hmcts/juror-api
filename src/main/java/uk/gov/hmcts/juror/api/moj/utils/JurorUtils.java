package uk.gov.hmcts.juror.api.moj.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.exception.JurorRecordException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class JurorUtils {

    private JurorUtils() {
    }

    /**
     * Bureau officers are authorised to view/read any records.
     * If there are potentially multiple active juror pool records, perhaps across multiple court locations,
     * a jury officer at either court location can view the juror record providing they own at least one active
     * juror pool association
     *
     * @param jurorNumber 9-digit numeric string to identify a Juror Record
     * @param owner       3-digit numeric string to uniquely identify the court location the current user belongs to
     */
    public static void checkReadAccessForCurrentUser(final JurorPoolRepository jurorPoolRepository,
                                                     final String jurorNumber, final String owner) {
        log.trace("Enter checkReadAccessForCurrentUser");
        log.debug("Check current user's access (owner = {}) to read juror record(s): {}",
            owner, jurorNumber);

        List<JurorPool> jurorPools = JurorPoolUtils.getActiveJurorPoolRecords(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkMultipleRecordReadAccess(jurorPools, owner);

        log.trace("Exit checkReadAccessForCurrentUser");
    }

    /**
     * Users are only authorised to edit a record they own.
     * Bureau user's can view any record but can only write to a record they own
     * Court users can only view a record they own as well as only write to a record they own.
     *
     * @param juror Juror record object
     * @param owner 3-digit numeric string to uniquely identify a court location
     */
    public static void checkOwnershipForCurrentUser(Juror juror, String owner) {
        log.trace("Enter checkOwnershipForCurrentUser");
        log.debug("Check if current user (owner = {}) owns juror record: {}", owner, juror.getJurorNumber());

        List<String> activeJurorPoolOwners = juror.getAssociatedPools().stream()
            .map(JurorPool::getOwner)
            .toList();

        if (!activeJurorPoolOwners.contains(owner)) {
            throw new MojException.Forbidden("User does not have access to write to the supplied "
                + "juror records", null);
        }

        log.trace("Exit checkOwnershipForCurrentUser");
    }

    /**
     * Reusable utility function to query the database and return the active juror record for a given juror number.
     *
     * @param jurorRepository JPA interface to the database to generate and execute SQL queries
     * @param jurorNumber     9-digit numeric string to identify jurors
     *
     * @return a juror record, there should be only one juror record
     */
    public static Juror getActiveJurorRecord(JurorRepository jurorRepository, String jurorNumber) {
        log.debug("Retrieving juror record for juror number {}", jurorNumber);

        Juror juror = jurorRepository.findById(jurorNumber).orElseThrow(() ->
            new MojException.NotFound(String.format("Unable to find valid juror record for Juror Number: %s",
                jurorNumber), null));

        log.debug("retrieved juror record for juror number {}", jurorNumber);
        return juror;
    }

    /**
     * To be used for verifying a list of Juror numbers are valid, typically before passing into a JPA query.
     *
     * @param jurorNumbers List of 9-digit numeric string to identify jurors
     */
    public static void validateJurorNumbers(List<String> jurorNumbers) {

        if (jurorNumbers == null || jurorNumbers.isEmpty()) {
            log.debug("List of Juror numbers is null or empty");
            throw new JurorRecordException.InvalidJurorNumber("Null or empty list");
        }

        Pattern pattern = Pattern.compile(ValidationConstants.JUROR_NUMBER);

        jurorNumbers.forEach(jurorNumber -> {
            Matcher matcher = pattern.matcher(jurorNumber);
            if (!matcher.find()) {
                log.debug("Invalid Juror number found {}", jurorNumber);
                throw new JurorRecordException.InvalidJurorNumber(jurorNumber);
            }
        });
    }

    /**
     * Calculate a Juror's age when they are due to start their jury service.
     *
     * @param dateOfBirth      juror's date of birth
     * @param serviceStartDate first date the juror is due to attend court for their service
     *
     * @return integer representation of age in whole years
     * @throws IllegalArgumentException thrown if either argument is null
     */
    public static int getJurorAgeAtHearingDate(final LocalDate dateOfBirth,
                                               final LocalDate serviceStartDate) throws IllegalArgumentException {
        log.debug("Calculate Juror age using date of birth {} and service start date {}", dateOfBirth,
            serviceStartDate);
        if (dateOfBirth == null || serviceStartDate == null) {
            log.warn("Cannot compare null dates!");
            throw new IllegalArgumentException("Birth Date and Hearing Date cannot be null");
        }
        return Period.between(dateOfBirth, serviceStartDate).getYears();
    }

    /**
     * To be used for retrieving a list of Jurors who are in a summoned/responded state from a specific pool
     * and court location.
     * <p/>
     * This method is particularly useful when transferring or reassigning jurors from a source pool and location.
     *
     * @param jurorNumbers    List of 9-digit numeric string to identify jurors
     * @param poolNumber      Pool number where jurors currently belong
     * @param courtLocation   Court location code of jurors
     * @param jurorRepository JPA interface to the database to generate and execute SQL queries
     */
    public static List<Juror> getSourceJurors(List<String> jurorNumbers, String poolNumber,
                                              CourtLocation courtLocation, JurorRepository jurorRepository) {
        List<Integer> validSourceStatusList = new ArrayList<>();
        validSourceStatusList.add(Math.toIntExact(IPoolStatus.SUMMONED));
        validSourceStatusList.add(Math.toIntExact(IPoolStatus.RESPONDED));

        log.debug("Find summoned/responded jurors from Pool: {} and location {}", poolNumber, courtLocation);

        return jurorRepository.findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(jurorNumbers,
            true, poolNumber, courtLocation, validSourceStatusList);
    }

}
