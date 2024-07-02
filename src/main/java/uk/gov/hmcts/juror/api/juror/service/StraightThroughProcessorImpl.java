package uk.gov.hmcts.juror.api.juror.service;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.service.ResponseMergeService;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.JurorDigitalApplication.AUTO_USER;
import static uk.gov.hmcts.juror.api.moj.domain.JurorHistory.RESPONDED;

/**
 * Implementation of Straight through processing flows.
 */
@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StraightThroughProcessorImpl implements StraightThroughProcessor {

    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    private final JurorPoolRepository jurorRepository;
    private final ResponseMergeService mergeService;

    private final JurorHistoryRepository jurorHistoryRepository;

    private final JurorResponseAuditRepositoryMod jurorResponseAuditRepositoryMod;

    private final UserRepository userRepository;
    private final ResponseInspector responseInspector;
    private final JurorStatusRepository jurorStatusRepository;
    private final JurorHistoryService jurorHistoryService;
    private final PrintDataService printDataService;

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
     * @param digitalResponse void processAcceptance(final DigitalResponse digitalResponse) throws
     *                        StraightThroughProcessingServiceException { Juror response to process
     * @throws StraightThroughProcessingServiceException Juror response does not meet the requirements
     */
    @Override
    @Transactional
    public void processAcceptance(
        final DigitalResponse digitalResponse) throws StraightThroughProcessingServiceException {
        try {
            log.debug("Begin processing straight through acceptance.");

            final DigitalResponse savedDigitalResponse =
                jurorDigitalResponseRepository.findByJurorNumber(digitalResponse.getJurorNumber());
            final JurorPool jurorDetails =
                jurorRepository.findByJurorJurorNumber(savedDigitalResponse.getJurorNumber());

            // check the response for answers making it ineligible for straight through processing.
            // JDB-126 a. the title, first name and last name must be the same in the Juror response as they are on
            // the Juror application
            if (jurorDetails.getJuror().getTitle() != null) { // title is nullable!!!
                //pool details title has value
                if (!jurorDetails.getJuror().getTitle().equalsIgnoreCase(savedDigitalResponse.getTitle())) {
                    log.debug("Title does not match: {} - {}", jurorDetails.getJuror().getTitle(),
                        savedDigitalResponse.getTitle());
                    throw new StraightThroughProcessingServiceException(MESSAGE_TITLE);
                }
            } else {
                //pool details title is null
                if (!Strings.isNullOrEmpty(savedDigitalResponse.getTitle())) {
                    log.debug("Title does not match: {} - {}", jurorDetails.getJuror().getTitle(),
                        savedDigitalResponse.getTitle());
                    throw new StraightThroughProcessingServiceException(MESSAGE_TITLE);
                }
            }
            // first name should always have a value
            if (ObjectUtils.isEmpty(jurorDetails.getJuror().getFirstName())
                || !jurorDetails.getJuror().getFirstName().equalsIgnoreCase(savedDigitalResponse.getFirstName())) {
                log.debug(
                    "Firstname does not match: {} - {}",
                    jurorDetails.getJuror().getFirstName(),
                    savedDigitalResponse.getFirstName()
                );
                throw new StraightThroughProcessingServiceException(MESSAGE_FIRSTNAME);
            }
            // last name should always have a value
            if (ObjectUtils.isEmpty(jurorDetails.getJuror().getLastName())
                || !jurorDetails.getJuror().getLastName().equalsIgnoreCase(savedDigitalResponse.getLastName())) {
                log.debug("Lastname does not match: {} - {}", jurorDetails.getJuror().getLastName(),
                    savedDigitalResponse.getLastName());
                throw new StraightThroughProcessingServiceException(MESSAGE_LASTNAME);
            }

            // JDB-126 b. the postcode must be the same in the Juror response as it is on the Juror application
            if (ObjectUtils.isEmpty(jurorDetails.getJuror().getPostcode())
                || !jurorDetails.getJuror().getPostcode().equalsIgnoreCase(savedDigitalResponse.getPostcode())) {
                log.debug("Postcode does not match: {} - {}", jurorDetails.getJuror().getPostcode(),
                    savedDigitalResponse.getPostcode());
                throw new StraightThroughProcessingServiceException(MESSAGE_POSTCODE);
            }

            // JDB-3679  the address must be the same in the Juror response as it is on the Juror application
            if (hasAddressLineChanged(jurorDetails.getJuror().getAddressLine1(),
                savedDigitalResponse.getAddressLine1())) {
                log.debug("Address does not match: {} - {}", jurorDetails.getJuror().getAddressLine1(),
                    savedDigitalResponse.getAddressLine1());
                throw new StraightThroughProcessingServiceException(MESSAGE_ADDRESS);
            }

            // JDB-3679  the address2 must be the same in the Juror response as it is on the Juror application
            if (hasAddressLineChanged(jurorDetails.getJuror().getAddressLine2(),
                savedDigitalResponse.getAddressLine2())) {
                log.debug("Address2 does not match: {} - {}", jurorDetails.getJuror().getAddressLine2(),
                    savedDigitalResponse.getAddressLine2());
                throw new StraightThroughProcessingServiceException(MESSAGE_ADDRESS2);
            }

            // JDB-3679  the address3 must be the same in the Juror response as it is on the Juror application
            if (hasAddressLineChanged(jurorDetails.getJuror().getAddressLine3(),
                savedDigitalResponse.getAddressLine3())) {
                log.debug("Address3 does not match: {} - {}", jurorDetails.getJuror().getAddressLine3(),
                    savedDigitalResponse.getAddressLine3());
                throw new StraightThroughProcessingServiceException(MESSAGE_ADDRESS3);
            }

            // JDB-3679  the address4 must be the same in the Juror response as it is on the Juror application
            if (hasAddressLineChanged(jurorDetails.getJuror().getAddressLine4(),
                savedDigitalResponse.getAddressLine4())) {
                log.debug("Address4 does not match: {} - {}", jurorDetails.getJuror().getAddressLine4(),
                    savedDigitalResponse.getAddressLine4());
                throw new StraightThroughProcessingServiceException(MESSAGE_ADDRESS4);
            }

            // JDB-3679  the address5 must be the same in the Juror response as it is on the Juror application
            if (hasAddressLineChanged(jurorDetails.getJuror().getAddressLine5(),
                savedDigitalResponse.getAddressLine5())) {
                log.debug("Address does not match: {} - {}", jurorDetails.getJuror().getAddressLine5(),
                    savedDigitalResponse.getAddressLine5());
                throw new StraightThroughProcessingServiceException(MESSAGE_ADDRESS5);
            }

            // JDB-3747  urgent responses will not be auto processed.
            if (savedDigitalResponse.isUrgent()) {
                throw new StraightThroughProcessingServiceException(MESSAGE);
            }

            // JDB-126 c. the date of birth in the response must not indicate that the Juror is too old or too young.
            final int youngestJurorAgeAllowed = responseInspector.getYoungestJurorAgeAllowed();
            final int tooOldJurorAge = responseInspector.getTooOldJurorAge();
            if (savedDigitalResponse.getDateOfBirth() == null) {
                log.debug("Cannot calculate age from null DOB");
                throw new StraightThroughProcessingServiceException("Cannot calculate age from null DOB");
            }

            if (jurorDetails.getNextDate() == null) {
                log.debug("Cannot calculate age on a null hearing date");
                throw new StraightThroughProcessingServiceException("Cannot calculate age on a null hearing date");
            }
            int age = responseInspector.getJurorAgeAtHearingDate(
                savedDigitalResponse.getDateOfBirth(),
                jurorDetails.getNextDate());
            if (log.isTraceEnabled()) {
                log.trace(
                    "Juror DOB {} at hearing date {} will be {} years old",
                    savedDigitalResponse.getDateOfBirth(),
                    jurorDetails.getNextDate(),
                    age
                );
            }
            if (age < youngestJurorAgeAllowed) {
                log.info(
                    "Juror {} too young for straight through as they are younger than {} on summon date",
                    jurorDetails.getNextDate(),
                    youngestJurorAgeAllowed
                );
                throw new StraightThroughProcessingServiceException("Juror is too young on summon date");
            }
            if (age >= tooOldJurorAge) {
                log.info(
                    "Juror {} too old for straight through as they are {} or older on summon date",
                    jurorDetails.getJurorNumber(),
                    tooOldJurorAge
                );
                throw new StraightThroughProcessingServiceException("Juror is too old on summon date");
            }

            // JDB-126 d. the response must not have been submitted by a third party
            if (savedDigitalResponse.getRelationship() != null && !savedDigitalResponse.getRelationship().isEmpty()) {
                throw new StraightThroughProcessingServiceException(
                    "Response must not have been submitted by third party");
            }
            // JDB-126 e. the Juror’s answer to the residency question must be Yes
            if (!savedDigitalResponse.getResidency()) {
                throw new StraightThroughProcessingServiceException(
                    "Residency question must be Yes to qualify as straight-through");
            }
            // JDB-126 f. the Juror’s answer to the mental health question must be No
            if (savedDigitalResponse.getMentalHealthAct()) {
                throw new StraightThroughProcessingServiceException(
                    "Mental Health Act must be No to qualify as straight-through");
            }
            // JDB-126 g. the Juror’s answer to the bail question must be No
            if (savedDigitalResponse.getBail()) {
                throw new StraightThroughProcessingServiceException("Bail must be No to qualify as straight-through");
            }
            // JDB-126 h. the Juror’s answer to the convictions question must be No
            if (savedDigitalResponse.getConvictions()) {
                throw new StraightThroughProcessingServiceException(
                    "Must have no convictions to qualify as straight-through");
            }
            // JDB-126 i. the Juror must have accepted the date of their jury service
            if ((savedDigitalResponse.getExcusalReason() != null && !savedDigitalResponse.getExcusalReason().isEmpty())
                || (savedDigitalResponse.getDeferralReason() != null && !savedDigitalResponse.getDeferralReason()
                .isEmpty())) {
                throw new StraightThroughProcessingServiceException(
                    "No excusal or deferral request to be accepted to qualify as straight-through");
            }
            // JDB-126 j. the Juror's answer to the Criminal Justice System must be No
            if (!CollectionUtils.isEmpty(savedDigitalResponse.getCjsEmployments())) {
                throw new StraightThroughProcessingServiceException(
                    "Criminal Justice System must not be provided to qualify as straight-through");
            }
            // JDB-126 k. the Juror's answer to the question about whether they require assistance in court must be No
            if (!CollectionUtils.isEmpty(savedDigitalResponse.getReasonableAdjustments())) {
                throw new StraightThroughProcessingServiceException(
                    "Request for special assistance must not be provided to qualify as straight-through");
            }

            //JDB-126 m. the status of the summons on the Juror application must still be Summoned
            if (jurorDetails.getStatus().getStatus() != IJurorStatus.SUMMONED) {
                throw new StraightThroughProcessingServiceException(
                    "Response must be in summoned state to qualify for straight-through");
            }

            savedDigitalResponse.setProcessingStatus(ProcessingStatus.CLOSED);

            savedDigitalResponse.setStaff(staffMember(AUTO_USER));
            savedDigitalResponse.setStaffAssignmentDate(LocalDate.now());

            // save the response
            mergeService.mergeResponse(savedDigitalResponse, AUTO_USER);

            //audit response status change
            jurorResponseAuditRepositoryMod.save(JurorResponseAuditMod.builder()
                .jurorNumber(savedDigitalResponse.getJurorNumber())
                .login(AUTO_USER)
                .oldProcessingStatus(ProcessingStatus.TODO)
                .newProcessingStatus(savedDigitalResponse.getProcessingStatus())
                .build());

            // update juror entry
            //   final Pool updatedDetails = detailsRepository.findByJurorNumber(savedDigitalResponse.getJurorNumber());
            final JurorPool updatedJurorDetails =
                jurorRepository.findByJurorJurorNumber(savedDigitalResponse.getJurorNumber());
            updatedJurorDetails.getJuror().setResponded(true);
            updatedJurorDetails.setUserEdtq(AUTO_USER);
            updatedJurorDetails.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED,
                jurorStatusRepository));
            jurorRepository.save(updatedJurorDetails);

            // audit the pool changes
            final JurorHistory jurorHistory = new JurorHistory();
            jurorHistory.setJurorNumber(savedDigitalResponse.getJurorNumber());
            //  jurorHistory.setOwner(JurorDigitalApplication.JUROR_OWNER);
            jurorHistory.setHistoryCode(HistoryCodeMod.RESPONDED_POSITIVELY);
            jurorHistory.setCreatedBy(AUTO_USER);
            jurorHistory.setOtherInformation(RESPONDED);
            jurorHistory.setPoolNumber(jurorDetails.getPoolNumber());
            //  jurorHistory.setDatePart(Date.from(Instant.now()));
            jurorHistoryRepository.save(jurorHistory);
        } catch (StraightThroughProcessingServiceException stpse) {
            // log and rethrow that the response cannot be processed as a straight through.
            log.debug("Rethrowing StraightThroughProcessingServiceException!");
            if (log.isTraceEnabled()) {
                log.trace("Failed to process straight through response for juror {}: {}",
                    // jurorResponse.getJurorNumber(), stpse.getMessage()
                    digitalResponse.getJurorNumber(), stpse.getMessage());

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
     * @param digitalResponse Juror response to process (an attached entity)
     * @throws StraightThroughProcessingServiceException.DeceasedExcusal Juror response does not meet the requirements
     */
    @Override
    @Transactional
    public void processDeceasedExcusal(
        final DigitalResponse digitalResponse) throws StraightThroughProcessingServiceException.DeceasedExcusal {
        log.debug("Begin processing excusal deceased straight through.");
        try {
            final DigitalResponse savedDigitalResponse =
                jurorDigitalResponseRepository.findByJurorNumber(digitalResponse.getJurorNumber());
            final JurorPool jurorDetails =
                jurorRepository.findByJurorJurorNumber(savedDigitalResponse.getJurorNumber());

            // check the response for answers making it ineligible for straight through processing.
            //JDB-73 a. the response must have been submitted by a third party
            if (ObjectUtils.isEmpty(savedDigitalResponse.getRelationship())) {
                throw new StraightThroughProcessingServiceException.DeceasedExcusal(
                    "Response must be submitted by third party");
            }

            //JDB-73 b. the reason given for response by the third party must be "deceased"
            if (!THIRD_PARTY_REASON_DECEASED.equalsIgnoreCase(savedDigitalResponse.getThirdPartyReason())) {
                throw new StraightThroughProcessingServiceException.DeceasedExcusal(
                    "Third party reason is not 'Deceased'");
            }

            //JDB-73 c. the status of the summons on the Juror application must still be Summoned
            if (jurorDetails.getStatus().getStatus() != IJurorStatus.SUMMONED) {
                throw new StraightThroughProcessingServiceException.DeceasedExcusal(
                    "The status of the summons must still be Summoned");
            }

            // JDB-3747  urgent responses will not be auto processed.
            if (savedDigitalResponse.isUrgent()) {
                throw new StraightThroughProcessingServiceException.DeceasedExcusal(MESSAGE);
            }

            //update response
            savedDigitalResponse.setProcessingStatus(ProcessingStatus.CLOSED);
            savedDigitalResponse.setStaff(staffMember(AUTO_USER));
            savedDigitalResponse.setStaffAssignmentDate(LocalDate.now());

            // save the response
            mergeService.mergeResponse(savedDigitalResponse, AUTO_USER);

            //audit response status change
            jurorResponseAuditRepositoryMod.save(JurorResponseAuditMod.builder()
                .jurorNumber(savedDigitalResponse.getJurorNumber())
                .login(AUTO_USER)
                .oldProcessingStatus(ProcessingStatus.TODO)
                .newProcessingStatus(savedDigitalResponse.getProcessingStatus())
                .build());

            // update Juror
            jurorDetails.getJuror().setResponded(true);
            jurorDetails.getJuror().setExcusalDate(LocalDate.now());
            jurorDetails.getJuror().setExcusalCode("D");
            jurorDetails.setUserEdtq(AUTO_USER);

            jurorDetails.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.EXCUSED, jurorStatusRepository));
            jurorDetails.setNextDate(null);
            jurorRepository.save(jurorDetails);

            // audit juror
            JurorHistory jurorHistory = new JurorHistory();
            //  jurorHistory.setOwner(JurorDigitalApplication.JUROR_OWNER);
            jurorHistory.setJurorNumber(savedDigitalResponse.getJurorNumber());
            jurorHistory.setHistoryCode(HistoryCodeMod.EXCUSE_POOL_MEMBER);
            jurorHistory.setCreatedBy(AUTO_USER);
            jurorHistory.setOtherInformation("ADD Excuse - D");
            jurorHistory.setPoolNumber(jurorDetails.getPoolNumber());
            jurorHistoryRepository.save(jurorHistory);
        } catch (StraightThroughProcessingServiceException.DeceasedExcusal stpse) {
            // log and rethrow that the response cannot be processed as a straight through.
            log.debug("Rethrowing StraightThroughProcessingServiceException.DeceasedExcusal!");
            if (log.isTraceEnabled()) {
                log.trace("Failed to process deceased excusal straight through response for juror {}: {}",
                    digitalResponse.getJurorNumber(), stpse.getMessage());
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
     * @param digitalResponse Juror response to process (an attached entity)
     * @throws StraightThroughProcessingServiceException.AgeExcusal Juror response does not meet the requirements
     */
    @Override
    @Transactional
    public void processAgeExcusal(
        final DigitalResponse digitalResponse) throws StraightThroughProcessingServiceException.AgeExcusal {
        log.debug("Begin processing age excusal straight through.");
        try {
            final DigitalResponse savedDigitalResponse =
                jurorDigitalResponseRepository.findByJurorNumber(digitalResponse.getJurorNumber());
            final JurorPool jurorDetails =
                jurorRepository.findByJurorJurorNumber(savedDigitalResponse.getJurorNumber());

            // check the response for answers making it ineligible for straight through processing.
            //JDB-91 b. the response must not have been submitted by a third party
            if (!ObjectUtils.isEmpty(savedDigitalResponse.getRelationship())) {
                throw new StraightThroughProcessingServiceException.AgeExcusal(
                    "Response must not be submitted by third party");
            }

            //JDB-91 c. the status of the summons on the Juror application must still be Summoned
            if (jurorDetails.getStatus().getStatus() != IPoolStatus.SUMMONED) {
                throw new StraightThroughProcessingServiceException.AgeExcusal(
                    "The status of the summons must still be Summoned");
            }

            // JDB-3747  urgent responses will not be auto processed.
            if (savedDigitalResponse.isUrgent()) {
                throw new StraightThroughProcessingServiceException(MESSAGE);
            }

            // JDB-91 a. the date of birth they entered must mean they'll be <18 or >=76 on the day that their jury
            // service is due to start (but the values must not be hardcoded)
            int age = responseInspector.getJurorAgeAtHearingDate(
                savedDigitalResponse.getDateOfBirth(),
                jurorDetails.getNextDate()
            );

            int youngestJurorAgeAllowed = responseInspector.getYoungestJurorAgeAllowed();
            int tooOldJurorAge = responseInspector.getTooOldJurorAge();

            if (age >= youngestJurorAgeAllowed && age < tooOldJurorAge) {
                throw new StraightThroughProcessingServiceException.AgeExcusal(
                    "Juror must be below the minimum age or above the maximum age on first day of jury duty");
            }

            //update response
            savedDigitalResponse.setProcessingStatus(ProcessingStatus.CLOSED);
            savedDigitalResponse.setStaff(staffMember(AUTO_USER));
            savedDigitalResponse.setStaffAssignmentDate(LocalDate.now());

            // save the response
            mergeService.mergeResponse(savedDigitalResponse, AUTO_USER);

            //audit response status change
            jurorResponseAuditRepositoryMod.save(JurorResponseAuditMod.builder()
                .jurorNumber(savedDigitalResponse.getJurorNumber())
                .login(AUTO_USER)
                .oldProcessingStatus(ProcessingStatus.TODO)
                .newProcessingStatus(savedDigitalResponse.getProcessingStatus())
                .build());

            // update Juror
            jurorDetails.getJuror().setResponded(true);
            jurorDetails.getJuror().setDisqualifyDate(LocalDate.now());
            jurorDetails.getJuror().setDisqualifyCode(DisCode.AGE);
            jurorDetails.setUserEdtq(AUTO_USER);
            jurorDetails.setStatus(
                RepositoryUtils.retrieveFromDatabase(IJurorStatus.DISQUALIFIED, jurorStatusRepository));
            jurorDetails.setNextDate(null);
            jurorRepository.save(jurorDetails);

            // audit Juror
            JurorHistory retrieveJurorHistory = new JurorHistory();
            //  retrieveJurorHistory.setOwner("400");
            retrieveJurorHistory.setJurorNumber(savedDigitalResponse.getJurorNumber());
            //  retrieveJurorHistory.setDatePart(Date.from(Instant.now()));
            retrieveJurorHistory.setHistoryCode(HistoryCodeMod.DISQUALIFY_POOL_MEMBER);
            retrieveJurorHistory.setCreatedBy(AUTO_USER);
            retrieveJurorHistory.setOtherInformationRef("A");
            retrieveJurorHistory.setPoolNumber(jurorDetails.getPoolNumber());
            jurorHistoryRepository.save(retrieveJurorHistory);

            // audit pool second entry
            jurorHistoryService.createWithdrawHistory(jurorDetails,null,"A");

            // disq_lett table entry
            printDataService.printWithdrawalLetter(jurorDetails);

        } catch (StraightThroughProcessingServiceException.AgeExcusal stpse) {
            // log and rethrow that the response cannot be processed as a straight through.
            log.debug("Rethrowing StraightThroughProcessingServiceException.AgeExcusal!");
            log.trace("Failed to process excusal straight through response for juror {}: {}",
                digitalResponse.getJurorNumber(), stpse.getMessage()
            );
            throw stpse;
        } catch (NullPointerException e) {
            log.error("Failed processing excusal straight through: {}", e.getMessage());
            throw new StraightThroughProcessingServiceException.AgeExcusal(e);
        } finally {
            log.info("Finished processing age excusal straight through for Juror {}.",
                digitalResponse.getJurorNumber());
        }
    }

    /**
     * Get an attached {@link User} entity for a username.
     *
     * @param login Staff login username
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
