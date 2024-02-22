package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReplyType;
import uk.gov.hmcts.juror.api.moj.domain.letter.DisqualificationLetterMod;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.letter.LetterService;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
public class StraightThroughProcessorServiceImpl implements StraightThroughProcessorService {

    private static final String COPY_RESPONSE_TO_GENERIC_JUROR_RESPONSE_POJO = "Juror: {}. Copying properties from {}"
        + " to a generic juror response pojo";

    @Autowired
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Autowired
    private final JurorResponseCjsEmploymentRepositoryMod jurorResponseCjsRepository;
    @Autowired
    private final JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;
    @Autowired
    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    @Autowired
    private final JurorRepository jurorRepository;
    @Autowired
    private final JurorPoolRepository jurorPoolRepository;
    @Autowired
    private final JurorHistoryRepository jurorHistoryRepository;
    @Autowired
    private final JurorStatusRepository jurorStatusRepository;
    @Autowired
    private final ResponseInspector responseInspector;
    @Autowired
    private final SummonsReplyMergeService mergeService;
    @Autowired
    @Qualifier("DisqualificationLetterServiceImpl")
    private final LetterService<DisqualificationLetterMod> disqualificationLetterService;

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public StraightThroughProcessorServiceImpl(
        @NonNull final JurorPaperResponseRepositoryMod jurorPaperResponseRepository,
        @NonNull final JurorResponseCjsEmploymentRepositoryMod jurorResponseCjsRepository,
        @NonNull final JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository,
        @NonNull final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository,
        @NonNull final JurorRepository jurorRepository,
        @NonNull final JurorPoolRepository jurorPoolRepository,
        @NonNull final JurorHistoryRepository jurorHistoryRepository,
        @NonNull final JurorStatusRepository jurorStatusRepository,
        @NonNull final ResponseInspector responseInspector,
        @NonNull final SummonsReplyMergeService mergeService,
        @NonNull final LetterService<DisqualificationLetterMod> disqualificationLetterService) {
        this.jurorPaperResponseRepository = jurorPaperResponseRepository;
        this.jurorResponseCjsRepository = jurorResponseCjsRepository;
        this.jurorReasonableAdjustmentRepository = jurorReasonableAdjustmentRepository;
        this.jurorDigitalResponseRepository = jurorDigitalResponseRepository;
        this.jurorRepository = jurorRepository;
        this.jurorPoolRepository = jurorPoolRepository;
        this.jurorHistoryRepository = jurorHistoryRepository;
        this.jurorStatusRepository = jurorStatusRepository;
        this.responseInspector = responseInspector;
        this.mergeService = mergeService;
        this.disqualificationLetterService = disqualificationLetterService;
    }

    /**
     * If all the mandatory fields have been entered and the no proof of data changes are required then the response
     * may be eligible to immediately be marked as responded.
     *
     * @param jurorNumber           9-digit numeric string used to identify the juror record related to this summons
     *                              reply
     * @param owner                 3-digit numeric string representing the current user's access to a primary court
     *                              location
     * @param canServeOnSummonsDate flag indicating whether the juror can attend jury service on the date they were
     *                              originally summoned for
     *
     * @return true if the paper summons reply is eligible to be marked as responded immediately, else false
     */
    @Override
    public boolean isValidForStraightThroughAcceptance(String jurorNumber, String owner,
                                                       boolean canServeOnSummonsDate) {
        log.trace("Enter isValidForStraightThroughAcceptance");

        // Check if the current user has access to the Juror record (and also that the record exists)
        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, owner);

        PaperResponse paperResponse = DataUtils.getJurorPaperResponse(jurorNumber,
            jurorPaperResponseRepository);

        // validate personal details
        if (!arePersonalDetailsValidForStraightThroughAcceptance(jurorPool.getJuror(), paperResponse)) {
            return false;
        }

        LocalDate returnDate = jurorPool.getReturnDate();
        // complete juror age check for service start date
        if (paperResponse.getDateOfBirth() == null || (returnDate != null
            && !isJurorAgeValidForServiceStartDate(jurorPool.getJurorNumber(),
            paperResponse.getDateOfBirth(), returnDate))) {
            log.debug("Juror's age is invalid for the service start date");
            return false;
        }

        // carry out basic validation of reply status and third party completion
        if (!hasPassedValidationForStraightThroughProcessing(paperResponse.getReplyType(),
            paperResponse.getRelationship(),
            jurorPool)) {
            return false;
        }

        // carry out eligibility validation
        if (!isEligibilityCriteriaValidForStraightThroughAcceptance(paperResponse)) {
            return false;
        }

        // validate juror can serve on the summoned date (has not requested an excusal or deferral)
        boolean excusalRequested = Boolean.TRUE.equals(paperResponse.getExcusal());
        boolean deferralRequested = Boolean.TRUE.equals(paperResponse.getDeferral());

        if (!canServeOnSummonsDate || excusalRequested || deferralRequested) {
            log.debug("Juror must be available to serve on summoned date to qualify as straight-through");
            return false;
        }

        // carry out CJS employment validation
        if (!jurorResponseCjsRepository.findByJurorNumber(jurorPool.getJurorNumber()).isEmpty()) {
            log.debug("Criminal Justice System employment must not be provided to qualify as straight-through");
            return false;
        }

        // carry out reasonable adjustments validation
        if (!jurorReasonableAdjustmentRepository.findByJurorNumber(jurorPool.getJurorNumber()).isEmpty()) {
            log.debug("Request for Reasonable Adjustments must not be provided to qualify as straight-through");
            return false;
        }

        // carry out signature validation
        if (!Boolean.TRUE.equals(paperResponse.getSigned())) {
            log.debug("Juror must have signed the paper reply to be eligible for straight-through processing");
            return false;
        }

        return true;
    }

    /**
     * Potential jurors must meet a minimum and maximum age limit.
     * The age limit is applied based on the service start date/when the juror is first due to attend court. If by this
     * date they do not satisfy the requirements and fall outside the minimum or maximum bounds then they will be
     * disqualified. If they are within the bounds on the service start date, but then exceed the maximum age limit
     * during their service, then they will not be disqualified and will be expected to complete their full service
     *
     * @param paperResponse Juror paper summons reply data
     * @param jurorPool     Juror record associated with the paper summons reply
     * @param payload       JSON Web Token from the request authentication header
     */
    @Override
    @Transactional
    public void processAgeDisqualification(PaperResponse paperResponse, LocalDate returnDate, JurorPool jurorPool,
                                           BureauJWTPayload payload) {
        ageDisqualificationImplementation(paperResponse, jurorPool, payload);
    }

    @Override
    @Transactional
    public void processAgeDisqualification(DigitalResponse digitalResponse, JurorPool jurorPool,
                                           BureauJWTPayload payload) {

        ageDisqualificationImplementation(digitalResponse, jurorPool, payload);
    }

    /**
     * Validation checks for a paper response to evaluate whether it is suitable for straight through processing.
     * Responses completed by a Third Party should be reviewed by an officer and are not suitable for straight through
     * processing. Responses for a Juror Record whose status is not currently Summoned should be reviewed by an officer
     * and are not suitable for straight through processing.
     *
     * @param jurorPaperResponse The Paper Summons Reply object entered via the Juror Paper application
     * @param jurorPool          The Juror record associated with the Paper Summons Reply entered
     *
     * @return a boolean to indicate if the response is suitable for straight through processing (true) or not (false)
     */
    @Override
    public boolean isValidForStraightThroughAgeDisqualification(PaperResponse jurorPaperResponse, LocalDate returnDate,
                                                                JurorPool jurorPool) {

        return hasPassedValidationForStraightThroughProcessing(jurorPaperResponse.getReplyType(),
            jurorPaperResponse.getRelationship(), jurorPool
        )
            && !isJurorAgeValidForServiceStartDate(jurorPool.getJurorNumber(),
            jurorPaperResponse.getDateOfBirth(), returnDate
        );
    }

    /**
     * Validation checks for a digital response to evaluate whether it is suitable for straight through processing.
     * Responses completed by a Third Party should be reviewed by an officer and are not suitable for straight through
     * processing. Responses for a Juror Record whose status is not currently Summoned should be reviewed by an officer
     * and are not suitable for straight through processing.
     *
     * @param jurorDigitalResponse The Paper Summons Reply object entered via the Juror Paper application
     * @param jurorPool            The Juror record associated with the Paper Summons Reply entered
     *
     * @return a boolean to indicate if the response is suitable for straight through processing (true) or not (false)
     */
    @Override
    public boolean isValidForStraightThroughAgeDisqualification(DigitalResponse jurorDigitalResponse,
                                                                LocalDate returnDate, JurorPool jurorPool) {
        return hasPassedValidationForStraightThroughProcessing(jurorDigitalResponse.getReplyType(),
            jurorDigitalResponse.getRelationship(), jurorPool)
            && !isJurorAgeValidForServiceStartDate(jurorPool.getJurorNumber(),
                jurorDigitalResponse.getDateOfBirth(), returnDate);
    }

    private void ageDisqualificationImplementation(AbstractJurorResponse jurorResponse,
                                                   JurorPool jurorPool, BureauJWTPayload payload) {
        final String username = payload.getLogin();
        final String owner = payload.getOwner();
        final String jurorNumber = jurorResponse.getJurorNumber();

        log.trace(
            "Juror: {}. Enter ageDisqualificationImplementation for {} response",
            jurorNumber,
            jurorResponse.getReplyType().getDescription().toLowerCase()
        );

        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, owner);

        jurorResponse.setProcessingStatus(ProcessingStatus.CLOSED);

        processJurorAgeDisqualification(jurorPool, jurorNumber, owner, username);

        if (jurorResponse.getReplyType().getType().equals(ReplyMethod.PAPER.getDescription())) {
            PaperResponse jurorPaperResponse = (PaperResponse) jurorResponse;
            BeanUtils.copyProperties(jurorResponse, jurorPaperResponse);

            jurorPaperResponseRepository.save(jurorPaperResponse);

            mergeService.mergePaperResponse(jurorPaperResponse, username);
        } else if (jurorResponse.getReplyType().getType().equals(ReplyMethod.DIGITAL.getDescription())) {
            DigitalResponse jurorDigitalResponse = (DigitalResponse) jurorResponse;
            jurorDigitalResponseRepository.save(jurorDigitalResponse);

            mergeService.mergeDigitalResponse(jurorDigitalResponse, username);
        }

        // TODO - audit response status change

        log.trace(
            "Juror: {}. Exit ageDisqualificationImplementation for {} response",
            jurorNumber,
            jurorResponse.getReplyType().getDescription().toLowerCase()
        );
    }

    private boolean hasPassedValidationForStraightThroughProcessing(ReplyType replyMethod, String relationship,
                                                                    JurorPool jurorPool) {
        log.debug("Juror: {}. Validating {} response is eligible for straight through processing",
            jurorPool.getJurorNumber(), replyMethod.getDescription().toLowerCase());

        if (!ObjectUtils.isEmpty(relationship)) {
            log.debug("Juror: {}. {} response was submitted by a Third Party so is not eligible for straight through "
                + "processing", jurorPool.getJurorNumber(), replyMethod);
            return false;
        }

        if (!Objects.equals(jurorPool.getStatus().getStatus(), IJurorStatus.SUMMONED)) {
            log.debug("Juror: {}. The juror record must still be in a summoned state, but the state was {} so is not "
                + "eligible for straight through processing", jurorPool.getJurorNumber(), jurorPool.getStatus());
            return false;
        }

        return true;
    }

    private boolean isJurorAgeValidForServiceStartDate(String jurorNumber, LocalDate dateOfBirth,
                                                       LocalDate serviceStartDate) {
        int youngestJurorAgeAllowed = responseInspector.getYoungestJurorAgeAllowed();
        int tooOldJurorAge = responseInspector.getTooOldJurorAge();
        int age = JurorUtils.getJurorAgeAtHearingDate(dateOfBirth, serviceStartDate);

        log.debug("Juror: {}. The juror will be {} year's old on their service start date", jurorNumber, age);

        return age >= youngestJurorAgeAllowed && age < tooOldJurorAge;
    }

    private void processJurorAgeDisqualification(JurorPool jurorPool, String jurorNumber, String owner,
                                                 String username) {
        updateJurorPoolForAgeExcusal(jurorPool, username);

        LocalDateTime currentDate = LocalDateTime.now();

        // record juror record disqualification history record
        JurorHistory disqualifyHistory = new JurorHistory(jurorNumber, HistoryCodeMod.DISQUALIFY_POOL_MEMBER,
            currentDate, username, "Disqualify Code A", jurorPool.getPoolNumber());

        jurorHistoryRepository.save(disqualifyHistory);

        DisqualificationLetterMod disqualificationLetter = disqualificationLetterService.getLetterToEnqueue(owner,
            jurorNumber);
        disqualificationLetter.setDisqCode(DisCode.AGE);
        disqualificationLetter.setDateDisq(LocalDate.now());
        disqualificationLetterService.enqueueLetter(disqualificationLetter);

        // record disqualification letter entry history record
        JurorHistory disqualifyLetterHistory = new JurorHistory(jurorNumber, HistoryCodeMod.WITHDRAWAL_LETTER,
            currentDate, username, "Disqualify Letter Code A", jurorPool.getPoolNumber());

        jurorHistoryRepository.save(disqualifyLetterHistory);
    }

    private void updateJurorPoolForAgeExcusal(JurorPool jurorPool, String username) {
        Juror juror = jurorPool.getJuror();
        juror.setResponded(true);
        juror.setDisqualifyDate(LocalDate.now());
        juror.setDisqualifyCode(DisCode.AGE);
        jurorRepository.save(juror);

        jurorPool.setUserEdtq(username);
        jurorPool.setStatus(getJurorStatus(IJurorStatus.DISQUALIFIED));
        jurorPool.setNextDate(null);

        jurorPoolRepository.save(jurorPool);
    }

    private JurorStatus getJurorStatus(int poolStatusId) {
        return RepositoryUtils.retrieveFromDatabase(poolStatusId, jurorStatusRepository);
    }

    private boolean arePersonalDetailsValidForStraightThroughAcceptance(Juror juror, PaperResponse paperResponse) {
        String jurorNumber = juror.getJurorNumber();

        if (hasNullablePropertyChanged(juror.getTitle(), paperResponse.getTitle())) {
            log.debug("Juror {}: Title values do not match: {} - {}", jurorNumber, juror.getTitle(),
                paperResponse.getTitle());
            return false;
        }

        if (hasNonNullablePropertyChanged(juror.getFirstName(), paperResponse.getFirstName())) {
            log.debug("First Name does not match: {} - {}", juror.getFirstName(), paperResponse.getFirstName());
            return false;
        }

        if (hasNonNullablePropertyChanged(juror.getLastName(), paperResponse.getLastName())) {
            log.debug("Last Name does not match: {} - {}", juror.getLastName(), paperResponse.getLastName());
            return false;
        }

        if (hasNonNullablePropertyChanged(juror.getPostcode(), paperResponse.getPostcode())) {
            log.debug("Postcode does not match: {} - {}", juror.getPostcode(), paperResponse.getPostcode());
            return false;
        }

        if (hasNonNullablePropertyChanged(juror.getAddressLine1(), paperResponse.getAddressLine1())) {
            log.debug("Address Line 1 does not match: {} - {}", juror.getAddressLine1(),
                paperResponse.getAddressLine1());
            return false;
        }

        if (hasNullablePropertyChanged(juror.getAddressLine2(), paperResponse.getAddressLine2())) {
            log.debug("Address Line 2 does not match: {} - {}", juror.getAddressLine2(),
                paperResponse.getAddressLine2());
            return false;
        }

        if (hasNullablePropertyChanged(juror.getAddressLine3(), paperResponse.getAddressLine3())) {
            log.debug("Address Line 3 does not match: {} - {}", juror.getAddressLine3(),
                paperResponse.getAddressLine3());
            return false;
        }

        if (hasNonNullablePropertyChanged(juror.getAddressLine4(), paperResponse.getAddressLine4())) {
            log.debug("Address Line 4 does not match: {} - {}", juror.getAddressLine4(),
                paperResponse.getAddressLine4());
            return false;
        }

        if (hasNullablePropertyChanged(juror.getAddressLine5(), paperResponse.getAddressLine5())) {
            log.debug("Address Line 5 does not match: {} - {}", juror.getAddressLine5(),
                paperResponse.getAddressLine5());
            return false;
        }

        return true;
    }

    private boolean hasNonNullablePropertyChanged(String originalValue, String newValue) {
        return ObjectUtils.isEmpty(originalValue) || !originalValue.equalsIgnoreCase(newValue);
    }

    private boolean hasNullablePropertyChanged(String originalValue, String newValue) {
        boolean valueChanged = false;
        if (!ObjectUtils.isEmpty(originalValue)) {
            if (!originalValue.equalsIgnoreCase(newValue)) {
                valueChanged = true;
            }
        } else {
            if (!ObjectUtils.isEmpty(newValue)) {
                valueChanged = true;
            }
        }
        return valueChanged;
    }

    private boolean isEligibilityCriteriaValidForStraightThroughAcceptance(PaperResponse paperResponse) {

        if (!Boolean.TRUE.equals(paperResponse.getResidency())) {
            log.debug("Residency question must be Yes to qualify as straight-through");
            return false;
        }

        if (!Boolean.FALSE.equals(paperResponse.getMentalHealthAct())) {
            log.debug("Mental Health Act question must be No to qualify as straight-through");
            return false;
        }

        if (!Boolean.FALSE.equals(paperResponse.getMentalHealthCapacity())) {
            log.debug("Mental Health Capacity question must be No to qualify as straight-through");
            return false;
        }

        if (!Boolean.FALSE.equals(paperResponse.getBail())) {
            log.debug("Bail must be No to qualify as straight-through");
            return false;
        }

        if (!Boolean.FALSE.equals(paperResponse.getConvictions())) {
            log.debug("Must have no convictions to qualify as straight-through");
            return false;
        }

        return true;
    }

}
