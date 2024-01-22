package uk.gov.hmcts.juror.api.juror.service;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.bureau.domain.THistoryCode;
import uk.gov.hmcts.juror.api.bureau.service.ResponseMergeService;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetter;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static uk.gov.hmcts.juror.api.JurorDigitalApplication.AUTO_USER;

/**
 * Implementation of Straight through processing flows.
 */
@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StraightThroughProcessorImpl implements StraightThroughProcessor {
    private final JurorResponseRepository responseRepository;
    private final PoolRepository detailsRepository;
    private final ResponseMergeService mergeService;
    private final PartHistRepository historyRepository;
    private final JurorResponseAuditRepository jurorResponseAuditRepository;
    private final DisqualificationLetterRepository disqualificationLetterRepository;
    private final UserRepository userRepository;
    private final ResponseInspector responseInspector;

    private static final String THIRD_PARTY_REASON_DECEASED = "deceased";
    private static final String MESSAGE = "Urgent response does not qualify for straight-through";
    private static final String MESSAGE_TITLE = "Title does not coincide with saved response";
    private static final String MESSAGE_FIRSTNAME = "Firstname does not match with saved response";
    private static final String MESSAGE_LASTNAME = "Lastname does not coincide with saved response";
    private static final String MESSAGE_POSTCODE = "Postcode does not coincide with saved response";
    private static final String MESSAGE_ADDRESS = "Address does not coincide with saved response";
    private static final String MESSAGE_ADDRESS2 = "Address2 does not coincide with saved response";
    private static final String MESSAGE_ADDRESS3 = "Address3 does not coincide with saved response";
    private static final String MESSAGE_ADDRESS4 = "Address4 does not coincide with saved response";
    private static final String MESSAGE_ADDRESS5 = "Address5 does not coincide with saved response";



    /**
     * Process the straight through acceptance of a juror response.
     *
     * @param jurorResponse Juror response to process
     *
     * @throws StraightThroughProcessingServiceException Juror response does not meet the requirements
     */
    @Override
    @Transactional
    public void processAcceptance(final JurorResponse jurorResponse) throws StraightThroughProcessingServiceException {
        try {
            log.debug("Begin processing straight through acceptance.");

            final JurorResponse savedResponse = responseRepository.findByJurorNumber(jurorResponse.getJurorNumber());
            final Pool poolDetails = detailsRepository.findByJurorNumber(savedResponse.getJurorNumber());

            // check the response for answers making it ineligible for straight through processing.
            // JDB-126 a. the title, first name and last name must be the same in the Juror response as they are on
            // the Juror application
            if (poolDetails.getTitle() != null) {// title is nullable!!!
                //pool details title has value
                if (!poolDetails.getTitle().equalsIgnoreCase(savedResponse.getTitle())) {
                    log.debug("Title does not match: {} - {}", poolDetails.getTitle(), savedResponse.getTitle());
                    throw new StraightThroughProcessingServiceException(MESSAGE_TITLE);
                }
            } else {
                //pool details title is null
                if (!Strings.isNullOrEmpty(savedResponse.getTitle())) {
                    log.debug("Title does not match: {} - {}", poolDetails.getTitle(), savedResponse.getTitle());
                    throw new StraightThroughProcessingServiceException(MESSAGE_TITLE);
                }
            }
            // first name should always have a value
            if (ObjectUtils.isEmpty(poolDetails.getFirstName())
                || !poolDetails.getFirstName().equalsIgnoreCase(savedResponse.getFirstName())) {
                log.debug(
                    "Firstname does not match: {} - {}",
                    poolDetails.getFirstName(),
                    savedResponse.getFirstName()
                );
                throw new StraightThroughProcessingServiceException(MESSAGE_FIRSTNAME);
            }
            // last name should always have a value
            if (ObjectUtils.isEmpty(poolDetails.getLastName())
                || !poolDetails.getLastName().equalsIgnoreCase(savedResponse.getLastName())) {
                log.debug("Lastname does not match: {} - {}", poolDetails.getLastName(), savedResponse.getLastName());
                throw new StraightThroughProcessingServiceException(MESSAGE_LASTNAME);
            }

            // JDB-126 b. the postcode must be the same in the Juror response as it is on the Juror application
            if (ObjectUtils.isEmpty(poolDetails.getPostcode())
                || !poolDetails.getPostcode().equalsIgnoreCase(savedResponse.getPostcode())) {
                log.debug("Postcode does not match: {} - {}", poolDetails.getPostcode(), savedResponse.getPostcode());
                throw new StraightThroughProcessingServiceException(MESSAGE_POSTCODE);
            }

            // JDB-3679  the address must be the same in the Juror response as it is on the Juror application
            if (hasAddressLineChanged(poolDetails.getAddress(), savedResponse.getAddress())) {
                log.debug("Address does not match: {} - {}", poolDetails.getAddress(), savedResponse.getAddress());
                throw new StraightThroughProcessingServiceException(MESSAGE_ADDRESS);
            }

            // JDB-3679  the address2 must be the same in the Juror response as it is on the Juror application
            if (hasAddressLineChanged(poolDetails.getAddress2(), savedResponse.getAddress2())) {
                log.debug("Address2 does not match: {} - {}", poolDetails.getAddress2(), savedResponse.getAddress2());
                throw new StraightThroughProcessingServiceException(MESSAGE_ADDRESS2);
            }

            // JDB-3679  the address3 must be the same in the Juror response as it is on the Juror application
            if (hasAddressLineChanged(poolDetails.getAddress3(), savedResponse.getAddress3())) {
                log.debug("Address3 does not match: {} - {}", poolDetails.getAddress3(), savedResponse.getAddress3());
                throw new StraightThroughProcessingServiceException(MESSAGE_ADDRESS3);
            }

            // JDB-3679  the address4 must be the same in the Juror response as it is on the Juror application
            if (hasAddressLineChanged(poolDetails.getAddress4(), savedResponse.getAddress4())) {
                log.debug("Address4 does not match: {} - {}", poolDetails.getAddress4(), savedResponse.getAddress4());
                throw new StraightThroughProcessingServiceException(MESSAGE_ADDRESS4);
            }

            // JDB-3679  the address5 must be the same in the Juror response as it is on the Juror application
            if (hasAddressLineChanged(poolDetails.getAddress5(), savedResponse.getAddress5())) {
                log.debug("Address does not match: {} - {}", poolDetails.getAddress5(), savedResponse.getAddress5());
                throw new StraightThroughProcessingServiceException(MESSAGE_ADDRESS5);
            }

            // JDB-3747  urgent responses will not be auto processed.
            if (savedResponse.getUrgent()) {
                throw new StraightThroughProcessingServiceException(MESSAGE);
            }

            // JDB-126 l. the submission of the response must not be so late as to be considered super urgent.
            if (savedResponse.getSuperUrgent()) {
                throw new StraightThroughProcessingServiceException(
                    "Super urgent response does not qualify for straight-through");
            }


            // JDB-126 c. the date of birth in the response must not indicate that the Juror is too old or too young.
            int youngestJurorAgeAllowed = responseInspector.getYoungestJurorAgeAllowed();
            int tooOldJurorAge = responseInspector.getTooOldJurorAge();
            if (savedResponse.getDateOfBirth() == null) {
                log.debug("Cannot calculate age from null DOB");
                throw new StraightThroughProcessingServiceException("Cannot calculate age from null DOB");
            }
            if (poolDetails.getHearingDate() == null) {
                log.debug("Cannot calculate age on a null hearing date");
                throw new StraightThroughProcessingServiceException("Cannot calculate age on a null hearing date");
            }
            int age = responseInspector.getJurorAgeAtHearingDate(
                savedResponse.getDateOfBirth(),
                poolDetails.getHearingDate()
            );
            if (log.isTraceEnabled()) {
                log.trace(
                    "Juror DOB {} at hearing date {} will be {} years old",
                    savedResponse.getDateOfBirth(),
                    poolDetails.getHearingDate(),
                    age
                );
            }
            if (age < youngestJurorAgeAllowed) {
                log.info(
                    "Juror {} too young for straight through as they are younger than {} on summon date",
                    poolDetails.getJurorNumber(),
                    youngestJurorAgeAllowed
                );
                throw new StraightThroughProcessingServiceException("Juror is too young on summon date");
            }
            if (age >= tooOldJurorAge) {
                log.info(
                    "Juror {} too old for straight through as they are {} or older on summon date",
                    poolDetails.getJurorNumber(),
                    tooOldJurorAge
                );
                throw new StraightThroughProcessingServiceException("Juror is too old on summon date");
            }

            // JDB-126 d. the response must not have been submitted by a third party
            if (savedResponse.getRelationship() != null && !savedResponse.getRelationship().isEmpty()) {
                throw new StraightThroughProcessingServiceException(
                    "Response must not have been submitted by third party");
            }
            // JDB-126 e. the Juror’s answer to the residency question must be Yes
            if (!savedResponse.getResidency()) {
                throw new StraightThroughProcessingServiceException(
                    "Residency question must be Yes to qualify as straight-through");
            }
            // JDB-126 f. the Juror’s answer to the mental health question must be No
            if (savedResponse.getMentalHealthAct()) {
                throw new StraightThroughProcessingServiceException(
                    "Mental Health Act must be No to qualify as straight-through");
            }
            // JDB-126 g. the Juror’s answer to the bail question must be No
            if (savedResponse.getBail()) {
                throw new StraightThroughProcessingServiceException("Bail must be No to qualify as straight-through");
            }
            // JDB-126 h. the Juror’s answer to the convictions question must be No
            if (savedResponse.getConvictions()) {
                throw new StraightThroughProcessingServiceException(
                    "Must have no convictions to qualify as straight-through");
            }
            // JDB-126 i. the Juror must have accepted the date of their jury service
            if ((savedResponse.getExcusalReason() != null && !savedResponse.getExcusalReason().isEmpty())
                || (savedResponse.getDeferralReason() != null && !savedResponse.getDeferralReason().isEmpty())) {
                throw new StraightThroughProcessingServiceException(
                    "No excusal or deferral request to be accepted to qualify as straight-through");
            }
            // JDB-126 j. the Juror's answer to the Criminal Justice System must be No
            if (!CollectionUtils.isEmpty(savedResponse.getCjsEmployments())) {
                throw new StraightThroughProcessingServiceException(
                    "Criminal Justice System must not be provided to qualify as straight-through");
            }
            // JDB-126 k. the Juror's answer to the question about whether they require assistance in court must be No
            if (!CollectionUtils.isEmpty(savedResponse.getSpecialNeeds())) {
                throw new StraightThroughProcessingServiceException(
                    "Request for special assistance must not be provided to qualify as straight-through");
            }

            //JDB-126 m. the status of the summons on the Juror application must still be Summoned
            if (!poolDetails.getStatus().equals(Long.valueOf("1"))) {
                throw new StraightThroughProcessingServiceException(
                    "Response must be in summoned state to qualify for straight-through");
            }

            savedResponse.setProcessingStatus(ProcessingStatus.CLOSED);
            savedResponse.setStaff(staffMember(AUTO_USER));
            savedResponse.setStaffAssignmentDate(Date.from(Instant.now().atZone(ZoneId.systemDefault()).truncatedTo(
                ChronoUnit.DAYS).toInstant()));

            // save the response
            mergeService.mergeResponse(savedResponse, AUTO_USER);

            //audit response status change
            jurorResponseAuditRepository.save(JurorResponseAudit.builder()
                .jurorNumber(savedResponse.getJurorNumber())
                .login(AUTO_USER)
                .oldProcessingStatus(ProcessingStatus.TODO)
                .newProcessingStatus(savedResponse.getProcessingStatus())
                .build());

            // update juror pool entry
            final Pool updatedDetails = detailsRepository.findByJurorNumber(savedResponse.getJurorNumber());
            updatedDetails.setResponded(Pool.RESPONDED);
            updatedDetails.setUserEdtq(AUTO_USER);
            updatedDetails.setStatus(IPoolStatus.RESPONDED);
            detailsRepository.save(updatedDetails);

            // audit the pool changes
            final PartHist history = new PartHist();
            history.setJurorNumber(savedResponse.getJurorNumber());
            history.setOwner(JurorDigitalApplication.JUROR_OWNER);
            history.setHistoryCode(THistoryCode.RESPONDED);
            history.setUserId(AUTO_USER);
            history.setInfo(PartHist.RESPONDED);
            history.setPoolNumber(poolDetails.getPoolNumber());
            history.setDatePart(Date.from(Instant.now()));
            historyRepository.save(history);
        } catch (StraightThroughProcessingServiceException stpse) {
            // log and rethrow that the response cannot be processed as a straight through.
            log.debug("Rethrowing StraightThroughProcessingServiceException!");
            if (log.isTraceEnabled()) {
                log.trace("Failed to process straight through response for juror {}: {}",
                    jurorResponse.getJurorNumber(), stpse.getMessage()
                );
            }
            throw stpse;
        } catch (Exception e) {
            // unexpected error - wrap and throw to prevent parent transaction to continue.
            log.error("Failed processing straight through: {}", e.getMessage());
            throw new StraightThroughProcessingServiceException(e);
        } finally {
            log.debug("Finished processing straight through acceptance.");
        }
    }

    /**
     * Process the straight through DECEASED excusal of a juror response.
     *
     * @param jurorResponse Juror response to process (an attached entity)
     *
     * @throws StraightThroughProcessingServiceException.DeceasedExcusal Juror response does not meet the requirements
     */
    @Override
    @Transactional
    public void processDeceasedExcusal(
        final JurorResponse jurorResponse) throws StraightThroughProcessingServiceException.DeceasedExcusal {
        log.debug("Begin processing excusal deceased straight through.");
        try {
            final JurorResponse savedResponse = responseRepository.findByJurorNumber(jurorResponse.getJurorNumber());
            final Pool poolDetails = detailsRepository.findByJurorNumber(savedResponse.getJurorNumber());

            // check the response for answers making it ineligible for straight through processing.
            //JDB-73 a. the response must have been submitted by a third party
            if (ObjectUtils.isEmpty(savedResponse.getRelationship())) {
                throw new StraightThroughProcessingServiceException.DeceasedExcusal(
                    "Response must be submitted by third party");
            }

            //JDB-73 b. the reason given for response by the third party must be "deceased"
            if (!THIRD_PARTY_REASON_DECEASED.equalsIgnoreCase(savedResponse.getThirdPartyReason())) {
                throw new StraightThroughProcessingServiceException.DeceasedExcusal(
                    "Third party reason is not 'Deceased'");
            }

            //JDB-73 c. the status of the summons on the Juror application must still be Summoned
            if (!poolDetails.getStatus().equals(IPoolStatus.SUMMONED)) {
                throw new StraightThroughProcessingServiceException.DeceasedExcusal(
                    "The status of the summons must still be Summoned");
            }

            // JDB-3747  urgent responses will not be auto processed.
            if (savedResponse.getUrgent()) {
                throw new StraightThroughProcessingServiceException.DeceasedExcusal(MESSAGE);
            }

            //JDB-73 d. the submission of the response must not be so late as to be considered super urgent.
            if (savedResponse.getSuperUrgent()) {
                throw new StraightThroughProcessingServiceException.DeceasedExcusal(
                    "Super urgent response does not qualify for excusal straight-through");
            }

            //update response
            savedResponse.setProcessingStatus(ProcessingStatus.CLOSED);
            savedResponse.setStaff(staffMember(AUTO_USER));
            savedResponse.setStaffAssignmentDate(Date.from(Instant.now().atZone(ZoneId.systemDefault()).truncatedTo(
                ChronoUnit.DAYS).toInstant()));

            // save the response
            mergeService.mergeResponse(savedResponse, AUTO_USER);

            //audit response status change
            jurorResponseAuditRepository.save(JurorResponseAudit.builder()
                .jurorNumber(savedResponse.getJurorNumber())
                .login(AUTO_USER)
                .oldProcessingStatus(ProcessingStatus.TODO)
                .newProcessingStatus(savedResponse.getProcessingStatus())
                .build());

            // update POOL
            poolDetails.setResponded(Pool.RESPONDED);
            poolDetails.setExcusalDate(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
            poolDetails.setExcusalCode("D");
            poolDetails.setUserEdtq(AUTO_USER);
            poolDetails.setStatus(IPoolStatus.EXCUSED);
            poolDetails.setHearingDate(null);
            detailsRepository.save(poolDetails);

            // audit pool
            PartHist partHist = new PartHist();
            partHist.setOwner(JurorDigitalApplication.JUROR_OWNER);
            partHist.setJurorNumber(savedResponse.getJurorNumber());
            partHist.setHistoryCode(THistoryCode.EXCUSE_POOL_MEMBER);
            partHist.setUserId(AUTO_USER);
            partHist.setInfo("ADD Excuse - D");
            partHist.setPoolNumber(poolDetails.getPoolNumber());
            historyRepository.save(partHist);
        } catch (StraightThroughProcessingServiceException.DeceasedExcusal stpse) {
            // log and rethrow that the response cannot be processed as a straight through.
            log.debug("Rethrowing StraightThroughProcessingServiceException.DeceasedExcusal!");
            if (log.isTraceEnabled()) {
                log.trace("Failed to process deceased excusal straight through response for juror {}: {}",
                    jurorResponse.getJurorNumber(), stpse.getMessage());
            }
            throw stpse;
        } catch (Exception e) {
            log.error("Failed processing deceased excusal straight through: {}", e.getMessage());
            throw new StraightThroughProcessingServiceException.DeceasedExcusal(e);
        } finally {
            log.debug("Finished processing deceased excusal straight through.");
        }
    }

    /**
     * Process the straight through AGE excusal of a juror response.
     *
     * @param jurorResponse Juror response to process (an attached entity)
     *
     * @throws StraightThroughProcessingServiceException.AgeExcusal Juror response does not meet the requirements
     */
    @Override
    @Transactional
    public void processAgeExcusal(
        final JurorResponse jurorResponse) throws StraightThroughProcessingServiceException.AgeExcusal {
        log.debug("Begin processing age excusal straight through.");
        try {
            final JurorResponse savedResponse = responseRepository.findByJurorNumber(jurorResponse.getJurorNumber());
            final Pool poolDetails = detailsRepository.findByJurorNumber(savedResponse.getJurorNumber());

            // check the response for answers making it ineligible for straight through processing.
            //JDB-91 b. the response must not have been submitted by a third party
            if (!ObjectUtils.isEmpty(savedResponse.getRelationship())) {
                throw new StraightThroughProcessingServiceException.AgeExcusal(
                    "Response must not be submitted by third party");
            }

            //JDB-91 c. the status of the summons on the Juror application must still be Summoned
            if (!poolDetails.getStatus().equals(IPoolStatus.SUMMONED)) {
                throw new StraightThroughProcessingServiceException.AgeExcusal(
                    "The status of the summons must still be Summoned");
            }

            // JDB-3747  urgent responses will not be auto processed.
            if (savedResponse.getUrgent()) {
                throw new StraightThroughProcessingServiceException(MESSAGE);
            }

            //JDB-91 d. the submission of the response must not be so late as to be considered super urgent.
            if (savedResponse.getSuperUrgent()) {
                throw new StraightThroughProcessingServiceException.AgeExcusal(
                    "Super urgent response does not qualify for excusal straight-through");
            }

            // JDB-91 a. the date of birth they entered must mean they'll be <18 or >=76 on the day that their jury
            // service is due to start (but the values must not be hardcoded)
            int age = responseInspector.getJurorAgeAtHearingDate(
                savedResponse.getDateOfBirth(),
                poolDetails.getHearingDate()
            );

            int youngestJurorAgeAllowed = responseInspector.getYoungestJurorAgeAllowed();
            int tooOldJurorAge = responseInspector.getTooOldJurorAge();

            if (age >= youngestJurorAgeAllowed && age < tooOldJurorAge) {
                throw new StraightThroughProcessingServiceException.AgeExcusal(
                    "Juror must be below the minimum age or above the maximum age on first day of jury duty");
            }

            //update response
            savedResponse.setProcessingStatus(ProcessingStatus.CLOSED);
            savedResponse.setStaff(staffMember(AUTO_USER));
            savedResponse.setStaffAssignmentDate(Date.from(Instant.now().atZone(ZoneId.systemDefault()).truncatedTo(
                ChronoUnit.DAYS).toInstant()));

            // save the response
            mergeService.mergeResponse(savedResponse, AUTO_USER);

            //audit response status change
            jurorResponseAuditRepository.save(JurorResponseAudit.builder()
                .jurorNumber(savedResponse.getJurorNumber())
                .login(AUTO_USER)
                .oldProcessingStatus(ProcessingStatus.TODO)
                .newProcessingStatus(savedResponse.getProcessingStatus())
                .build());

            // update POOL
            poolDetails.setResponded(Pool.RESPONDED);
            poolDetails.setDisqualifyDate(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
            poolDetails.setDisqualifyCode(DisCode.AGE);
            poolDetails.setUserEdtq(AUTO_USER);
            poolDetails.setStatus(IPoolStatus.DISQUALIFIED);
            poolDetails.setHearingDate(null);
            detailsRepository.save(poolDetails);

            // audit pool
            PartHist retrieveHistory = new PartHist();
            retrieveHistory.setOwner("400");
            retrieveHistory.setJurorNumber(savedResponse.getJurorNumber());
            retrieveHistory.setDatePart(Date.from(Instant.now()));
            retrieveHistory.setHistoryCode(THistoryCode.DISQUALIFY_POOL_MEMBER);
            retrieveHistory.setUserId(AUTO_USER);
            retrieveHistory.setInfo("Disqualify Code A");
            retrieveHistory.setPoolNumber(poolDetails.getPoolNumber());
            historyRepository.save(retrieveHistory);

            // audit pool second entry
            retrieveHistory = new PartHist();
            retrieveHistory.setOwner("400");
            retrieveHistory.setJurorNumber(savedResponse.getJurorNumber());
            retrieveHistory.setDatePart(Date.from(Instant.now()));
            retrieveHistory.setHistoryCode(THistoryCode.DISQUALIFY_RESPONSE);
            retrieveHistory.setUserId(AUTO_USER);
            retrieveHistory.setInfo("Disqualify Letter Code A");
            retrieveHistory.setPoolNumber(poolDetails.getPoolNumber());
            historyRepository.save(retrieveHistory);

            // disq_lett table entry
            DisqualificationLetter disqualificationLetter = new DisqualificationLetter();
            disqualificationLetter.setJurorNumber(savedResponse.getJurorNumber());
            disqualificationLetter.setDisqCode(DisCode.AGE);
            disqualificationLetter.setDateDisq(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
            disqualificationLetterRepository.save(disqualificationLetter);

        } catch (StraightThroughProcessingServiceException.AgeExcusal stpse) {
            // log and rethrow that the response cannot be processed as a straight through.
            log.debug("Rethrowing StraightThroughProcessingServiceException.AgeExcusal!");
            log.trace("Failed to process excusal straight through response for juror {}: {}",
                jurorResponse.getJurorNumber(), stpse.getMessage()
            );
            throw stpse;
        } catch (NullPointerException e) {
            log.error("Failed processing excusal straight through: {}", e.getMessage());
            throw new StraightThroughProcessingServiceException.AgeExcusal(e);
        } finally {
            log.info("Finished processing age excusal straight through for Juror {}.", jurorResponse.getJurorNumber());
        }
    }

    /**
     * Get an attached {@link User} entity for a username.
     *
     * @param login Staff login username
     *
     * @return Entity
     */
    private User staffMember(final String login) {
        return userRepository.findByUsername(login);
    }

    /**
     * Verify the address line has not been changed. If so, not suitable for straight through processing.
     *
     * @param poolAddressLine     original pool record address line content.
     * @param responseAddressLine response record address line content.
     *
     * @return boolean JDB-4084 - do not allow straight through processing when address line changed from null to value.
     */
    private boolean hasAddressLineChanged(String poolAddressLine, String responseAddressLine) {
        boolean addressChanged = false;
        if (!ObjectUtils.isEmpty(poolAddressLine)) {
            if (!poolAddressLine.equalsIgnoreCase(responseAddressLine)) {
                addressChanged = true;
            }
        } else {  // pool address is null
            if (!ObjectUtils.isEmpty(responseAddressLine)) {
                addressChanged = true;
            }
        }
        return addressChanged;
    }
}
