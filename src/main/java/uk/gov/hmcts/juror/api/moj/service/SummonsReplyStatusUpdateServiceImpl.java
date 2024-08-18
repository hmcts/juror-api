package uk.gov.hmcts.juror.api.moj.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.JurorPaperResponseException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAuditChangeService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation of operations for updating the status of a Juror's response by a Bureau officer.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings("PMD.CyclomaticComplexity")
public class SummonsReplyStatusUpdateServiceImpl implements SummonsReplyStatusUpdateService, SummonsReplyMergeService {
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    private final AssignOnUpdateServiceMod assignOnUpdateService;
    private final EntityManager entityManager;
    private final JurorRepository jurorRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final JurorHistoryRepository jurorHistoryRepository;
    private final JurorReasonableAdjustmentRepository jurorReasonableAdjustmentsRepository;
    private final WelshCourtLocationRepository welshCourtLocationRepository;
    private final JurorRecordService jurorRecordService;
    private final JurorAuditChangeService jurorAuditChangeService;

    private static final String TITLE = "title";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String PHONE_NO = "phoneNumber";
    private static final String ALT_PHONE_NO = "altPhoneNumber";
    private static final String EMAIL = "email";
    private final JurorResponseAuditRepositoryMod jurorResponseAuditRepositoryMod;
    @Autowired
    @Lazy
    JurorResponseService jurorResponseService;

    /**
     * Update the processing status of a Juror response within Juror Digital.
     *
     * @param jurorNumber The juror number the response relates to
     * @param status      The new processing status to update to
     * @param payload     Bureau authentication Json Web Token payload.
     */
    @Override
    @Transactional
    public void updateJurorResponseStatus(final String jurorNumber, final ProcessingStatus status,
                                          final BureauJwtPayload payload) {
        log.debug("Updating status for juror {} to {}", jurorNumber, status.getDescription());

        final PaperResponse paperResponse = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        final JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        // if there is no paper response found for the juror and status is to be updated to responded then update the
        // juror status to responded and return
        if (paperResponse == null && ProcessingStatus.CLOSED == status
            && jurorPool.getStatus().getStatus() != IJurorStatus.RESPONDED) {
            log.info("No paper response found for juror {}, marking as responded", jurorNumber);
            updateJurorAsResponded(jurorNumber, payload.getLogin());
            return;
        }

        // need a valid paper response at this stage
        if (paperResponse == null) {
            throw new MojException.NotFound(
                String.format("Juror: %s. Cannot find paper response", jurorNumber), null);
        }

        // if response is closed already and new processing status is closed (responded) and juror is not
        // responded then update the juror status to responded and return
        if (ProcessingStatus.CLOSED == status && Boolean.TRUE.equals(paperResponse.getProcessingComplete())
            && jurorPool.getStatus().getStatus() != IJurorStatus.RESPONDED) {
            log.info("Juror {} has already responded, marking as responded", jurorNumber);
            updateJurorAsResponded(jurorNumber, payload.getLogin());
            return;
        }

        // store the current processing status (to be used as the "changed from" value in the audit/history table)
        final ProcessingStatus initialProcessingStatus = paperResponse.getProcessingStatus();

        paperResponse.setProcessingStatus(jurorResponseAuditRepositoryMod, status);

        // merge the changes if required/allowed
        if (Boolean.TRUE.equals(paperResponse.getProcessingComplete())) {
            log.debug("Unable to update the response status for juror {} as response processing is already complete.",
                jurorNumber);
            throw new JurorPaperResponseException.JurorPaperResponseAlreadyExists(jurorNumber);
        } else if (status == ProcessingStatus.CLOSED) {
            checkPaperResponseHasMandatoryFields(paperResponse);
            log.debug("Merging juror response for juror {}", jurorNumber);

            final String auditorUsername = payload.getLogin();
            updateJurorAsResponded(jurorNumber, auditorUsername);
            mergePaperResponse(paperResponse, auditorUsername);
        } else {
            // we're not closing the response, so update the response only, without merging changes to the Juror record
            log.debug("Updating juror '{}' response status to '{}' without merge.", jurorNumber, status);
            jurorPaperResponseRepository.save(paperResponse);
        }
        log.info("Updated juror '{}' processing status from '{}' to '{}'", jurorNumber, initialProcessingStatus,
            paperResponse.getProcessingStatus()
        );
    }

    /**
     * Update the processing status of a Juror digital response.
     *
     * @param jurorNumber 9-digit numeric string to identify the juror associated with this response
     * @param status      The processing status this response should be updated to
     * @param payload     Bureau authentication Json Web Token payload.
     */
    @Override
    @Transactional
    public void updateDigitalJurorResponseStatus(final String jurorNumber,
                                                 final ProcessingStatus status,
                                                 final BureauJwtPayload payload) {

        log.debug("Updating status for juror {} to {}", jurorNumber, status.getDescription());
        DigitalResponse jurorResponse = DataUtils.getJurorDigitalResponse(jurorNumber, jurorDigitalResponseRepository);

        if (jurorResponse == null) {
            log.error("No juror response found for juror number {}", jurorNumber);
            throw new JurorPaperResponseException.NoJurorPaperResponseRecordFound(jurorNumber);
        }

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        final String auditorUsername = payload.getLogin();
        // if response is closed already and new processing status is closed (responded) and juror is not
        // responded then update the juror status to responded and return
        if (ProcessingStatus.CLOSED == status && Boolean.TRUE.equals(jurorResponse.getProcessingComplete())
            && jurorPool.getStatus().getStatus() != IJurorStatus.RESPONDED) {
            log.info("Juror {} has already responded, marking as responded", jurorNumber);
            updateJurorAsResponded(jurorNumber, auditorUsername);
            return;
        }

        entityManager.detach(jurorResponse);
        // store the current processing status (to be used as the "changed from" value in the audit/history table)
        final ProcessingStatus auditProcessingStatus = jurorResponse.getProcessingStatus();

        // JDB-2685: if no staff assigned, assign current login
        if (null == jurorResponse.getStaff()) {
            assignOnUpdateService.assignToCurrentLogin(jurorResponse, auditorUsername);
        }

        jurorResponse.setProcessingStatus(jurorResponseAuditRepositoryMod, status);

        // merge the changes if required/allowed
        if (Boolean.TRUE.equals(jurorResponse.getProcessingComplete())) {
            log.debug(
                "Unable to update the response status for juror {} as response processing is already complete.",
                jurorNumber
            );
            throw new JurorPaperResponseException.JurorPaperResponseAlreadyExists(jurorNumber);
        } else if (status == ProcessingStatus.CLOSED) {
            log.debug("Merging juror response for juror {}", jurorNumber);

            mergeDigitalResponse(jurorResponse, auditorUsername);
            updatePoolMemberAsResponded(jurorNumber, auditorUsername);
        } else {
            // we're not closing the response, so update the response only, without merging changes to the Juror record
            log.debug("Updating juror '{}' response status to '{}' without merge.", jurorNumber, status);
            jurorDigitalResponseRepository.save(jurorResponse);
        }

        log.info("Updated juror '{}' processing status from '{}' to '{}'", jurorNumber, auditProcessingStatus,
            jurorResponse.getProcessingStatus()
        );
    }

    /**
     * Save the paper response back into the existing juror system.
     *
     * @param paperResponse   Paper response
     * @param auditorUsername The user performing the merge
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void mergePaperResponse(final PaperResponse paperResponse, final String auditorUsername) {
        final String jurorNumber = paperResponse.getJurorNumber();

        log.trace("Juror: {}. Enter mergePaperResponse", jurorNumber);

        mergeJurorResponseImplementation(paperResponse, auditorUsername);

        log.trace("Juror: {}. Exit mergePaperResponse", jurorNumber);
    }

    /**
     * Save the digital response back into the existing juror system.
     *
     * @param digitalResponse Digital response
     * @param auditorUsername The user performing the merge
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void mergeDigitalResponse(final DigitalResponse digitalResponse, final String auditorUsername) {
        final String jurorNumber = digitalResponse.getJurorNumber();

        log.trace("Juror: {}. Enter mergeDigitalResponse", jurorNumber);

        mergeJurorResponseImplementation(digitalResponse, auditorUsername);

        log.trace("Juror: {}. Exit mergeDigitalResponse", jurorNumber);
    }

    private void mergeJurorResponseImplementation(AbstractJurorResponse jurorResponse,
                                                  String auditorUsername) {
        final String jurorNumber = jurorResponse.getJurorNumber();

        log.trace("Juror: {}. Enter mergeJurorResponseImplementation", jurorNumber);

        if (Boolean.TRUE.equals(jurorResponse.getProcessingComplete())) {
            log.info("Juror: {}. Summons reply has not been merged because it has already been processed", jurorNumber);
            return;
        }

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        mergeReasonableAdjustments(jurorPool);

        // TODO - Processing a paper summons reply for a deceased juror is currently awaiting design/sign off

        Juror juror = jurorPool.getJuror();

        // Check for changes between the new/updated values and the juror record values
        Map<String, Boolean> changedPropertiesMap =
            jurorAuditChangeService.initChangedPropertyMap(juror, jurorResponse);

        // Once the old juror data is stored, the juror record can now be updated using the new summons reply data
        updateJurorFromSummonsReply(jurorResponse, juror, jurorPool.getCourt().getLocCode());

        // Flag the juror paper response as completed (cannot re-copy to pool)
        markSummonReplyAsCompleted(jurorResponse);

        changedPropertiesMap.keySet().forEach(propName -> {
            if (Boolean.TRUE.equals(changedPropertiesMap.get(propName))) {
                jurorAuditChangeService.recordPersonalDetailsHistory(propName, juror, jurorPool.getPoolNumber(),
                    auditorUsername);
            }
        });
        log.trace("Juror: {}. Exit mergeJurorResponseImplementation", jurorNumber);
    }

    private void checkPaperResponseHasMandatoryFields(PaperResponse paperResponse) {
        log.info(
            "Checking summons reply for Juror {} has required fields to be processed",
            paperResponse.getJurorNumber()
        );
        checkNameIsPresent(paperResponse);
        checkDobIsPresent(paperResponse);
        checkAddressIsPresent(paperResponse);
        checkEligibilityIsPresent(paperResponse);
        checkSignatureIsPresent(paperResponse);
        log.info("Summons reply for Juror {} has required fields to be processed", paperResponse.getJurorNumber());
    }

    private void checkNameIsPresent(PaperResponse paperResponse) {
        if (ObjectUtils.isEmpty(paperResponse.getFirstName())
            || ObjectUtils.isEmpty(paperResponse.getLastName())) {
            log.info("Summons reply for Juror {} is missing mandatory name fields", paperResponse.getJurorNumber());
            throw new JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields();
        }
    }

    private void checkDobIsPresent(PaperResponse paperResponse) {
        if (paperResponse.getDateOfBirth() == null) {
            log.info(
                "Summons reply for Juror {} is missing mandatory date of birth field",
                paperResponse.getJurorNumber()
            );
            throw new JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields();
        }
    }

    private void checkAddressIsPresent(PaperResponse paperResponse) {
        if (ObjectUtils.isEmpty(paperResponse.getAddressLine1())
            || ObjectUtils.isEmpty(paperResponse.getAddressLine4())
            || ObjectUtils.isEmpty(paperResponse.getPostcode())) {
            log.info("Summons reply for Juror {} is missing mandatory address fields", paperResponse.getJurorNumber());
            throw new JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields();
        }
    }

    private void checkEligibilityIsPresent(PaperResponse paperResponse) {
        if (paperResponse.getResidency() == null || paperResponse.getMentalHealthAct() == null
            || paperResponse.getMentalHealthCapacity() == null || paperResponse.getBail() == null
            || paperResponse.getConvictions() == null) {
            log.info(
                "Summons reply for Juror {} is missing mandatory eligibility fields",
                paperResponse.getJurorNumber()
            );
            throw new JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields();
        }
    }

    private void checkSignatureIsPresent(PaperResponse paperResponse) {
        if (paperResponse.getSigned() == null) {
            log.info("Summons reply for Juror {} is missing mandatory signature field", paperResponse.getJurorNumber());
            throw new JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields();
        }
    }

    private void updateJurorAsResponded(String jurorNumber, String auditorUsername) {
        log.trace("Enter updateJurorPoolAsResponded for Juror Number: {}", jurorNumber);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);

        Juror juror = jurorPool.getJuror();
        juror.setResponded(true);
        jurorRepository.save(juror);

        jurorPool.setUserEdtq(auditorUsername);
        jurorPool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository));
        jurorPool.setNextDate(jurorPool.getPool().getReturnDate());
        jurorPoolRepository.save(jurorPool);

        recordJurorPoolRespondedHistory(jurorNumber, auditorUsername, jurorPool.getPoolNumber());
        log.trace("Exit updateJurorPoolAsResponded for Juror Number: {}", jurorNumber);
    }

    private void updatePoolMemberAsResponded(String jurorNumber, String auditorUsername) {
        log.trace("Enter updatePoolMemberAsResponded for Juror Number: {}", jurorNumber);

        int respondedStatusCode = 2;

        jurorStatusRepository.findById(respondedStatusCode).ifPresent(respondedStatus -> {
            JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
            jurorPool.setUserEdtq(auditorUsername);
            jurorPool.setStatus(respondedStatus);
            jurorPoolRepository.save(jurorPool);

            Juror juror = jurorPool.getJuror();
            juror.setResponded(true);
            jurorRepository.save(juror);

            recordJurorPoolRespondedHistory(jurorNumber, auditorUsername, jurorPool.getPoolNumber());
        });
        log.trace("Exit updatePoolMemberAsResponded for Juror Number: {}", jurorNumber);
    }

    private void recordJurorPoolRespondedHistory(String jurorNumber, String auditorUsername, String poolNumber) {
        log.trace("Enter recordJurorPoolResponseHistory");
        log.debug("Create history event for updating Juror {} to Responded Status", jurorNumber);

        final JurorHistory history = JurorHistory.builder()
            .jurorNumber(jurorNumber)
            .historyCode(HistoryCodeMod.RESPONDED_POSITIVELY)
            .createdBy(auditorUsername)
            .otherInformation(JurorHistory.RESPONDED)
            .poolNumber(poolNumber)
            .dateCreated(LocalDateTime.now())
            .build();

        jurorHistoryRepository.save(history);

        log.trace("Exit recordJurorPoolResponseHistory");
    }

    private void mergeReasonableAdjustments(JurorPool jurorPool) {
        final String multipleAdjustmentsCode = "M";
        final String jurorNumber = jurorPool.getJurorNumber();

        log.trace("Juror: {}. Enter mergeReasonableAdjustments", jurorNumber);

        List<JurorReasonableAdjustment> reasonableAdjustments =
            jurorReasonableAdjustmentsRepository.findByJurorNumber(jurorNumber);
        log.debug("Juror: {}. Found {} reasonable adjustment records", jurorNumber, reasonableAdjustments.size());

        Juror juror = jurorPool.getJuror();
        if (reasonableAdjustments.size() > 1) {
            juror.setReasonableAdjustmentCode(multipleAdjustmentsCode);
        } else if (reasonableAdjustments.size() == 1 && reasonableAdjustments.get(0) != null) {
            juror.setReasonableAdjustmentCode(reasonableAdjustments.get(0).getReasonableAdjustment().getCode());
        }
        jurorResponseService.getCommonJurorResponseOptional(jurorNumber)
            .ifPresent(abstractResponse ->
                juror.setReasonableAdjustmentMessage(abstractResponse.getReasonableAdjustmentsArrangements()));
        jurorPoolRepository.save(jurorPool);
        log.trace("Juror: {}. Exit mergeReasonableAdjustments", jurorNumber);
    }

    private void applyPhoneNumberRules(Juror juror, AbstractJurorResponse summonsReply) {
        String primaryPhone = summonsReply.getPhoneNumber();
        String secondaryPhone = summonsReply.getAltPhoneNumber();

        // If the main phone number starts with an 07 then it should be allocated to the mobile phone number
        if (isMobileNumber(primaryPhone)) {
            juror.setAltPhoneNumber(primaryPhone);
            juror.setWorkPhone(secondaryPhone);
            juror.setPhoneNumber(null);
        } else if (isMobileNumber(secondaryPhone)) {
            /*
             * If the main phone number does not start with an 07 but the alternative one does,
             * then the alternative phone will be  allocated to the mobile phone number
             */
            juror.setPhoneNumber(primaryPhone);
            juror.setAltPhoneNumber(secondaryPhone);
            juror.setWorkPhone(null);
        } else {
            /*
             * If the main phone number has not been allocated to the mobile number it should be allocated to the home
             * number. If the alternative phone has not been allocated to the mobile number it should be allocated to
             * the Work number.
             */
            juror.setPhoneNumber(primaryPhone);
            juror.setWorkPhone(secondaryPhone);
            juror.setAltPhoneNumber(null);
        }
    }

    private boolean isMobileNumber(String number) {
        return !ObjectUtils.isEmpty(number) && number.startsWith("07");
    }

    private void updateJurorFromSummonsReply(AbstractJurorResponse updatedDetails, Juror juror,
                                             String locCode) {
        log.trace("Juror: {}. Enter updateJurorPoolFromSummonsReply", juror.getJurorNumber());

        if (!ObjectUtils.isEmpty(updatedDetails.getThirdPartyReason())) {
            // Copy the actual details to pool. Avoid copying 3rd party details.
            log.debug(
                "Juror: {}.  Summons reply completed by a third-party, ignore contact details",
                juror.getJurorNumber()
            );
            BeanUtils.copyProperties(updatedDetails, juror, PHONE_NO, ALT_PHONE_NO, EMAIL, TITLE,
                FIRST_NAME, LAST_NAME);
        } else {
            BeanUtils.copyProperties(updatedDetails, juror, TITLE, FIRST_NAME, LAST_NAME);
            applyPhoneNumberRules(juror, updatedDetails);
        }

        // Individually map the details where the property names/types are not an exact match
        juror.setAddressLine1(updatedDetails.getAddressLine1());
        juror.setAddressLine2(updatedDetails.getAddressLine2());
        juror.setAddressLine3(updatedDetails.getAddressLine3());
        juror.setAddressLine4(updatedDetails.getAddressLine4());
        juror.setAddressLine5(updatedDetails.getAddressLine5());
        juror.setPostcode(updatedDetails.getPostcode());
        juror.setDateOfBirth(updatedDetails.getDateOfBirth());

        // check for a potential name change and store the pending change data for future approval
        if (jurorAuditChangeService.hasNameChanged(updatedDetails.getFirstName(), juror.getFirstName(),
            updatedDetails.getLastName(), juror.getLastName())) {
            jurorRecordService.setPendingNameChange(juror, updatedDetails.getTitle(),
                updatedDetails.getFirstName(), updatedDetails.getLastName());
        } else if (jurorAuditChangeService.hasTitleChanged(juror.getTitle(), updatedDetails.getTitle())) {
            juror.setTitle(updatedDetails.getTitle());
        }

        // Derive the value for Welsh
        if (Boolean.TRUE.equals(updatedDetails.getWelsh())
            && welshCourtLocationRepository.findByLocCode(locCode) != null) {
            juror.setWelsh(Boolean.TRUE);
        } else if (Boolean.TRUE.equals(updatedDetails.getWelsh())
            && welshCourtLocationRepository.findByLocCode(locCode) == null) {
            log.trace("Unable to provide Welsh language communications as the selected court is not within Wales.");
            juror.setWelsh(null);
        } else {
            juror.setWelsh(null);
        }

        jurorRepository.save(juror);
        log.trace("Juror {}. Exit updateJurorPoolFromSummonsReply", juror.getJurorNumber());
    }

    private void markSummonReplyAsCompleted(AbstractJurorResponse originalDetails) {
        log.trace("Juror: {}. Enter markSummonReplyAsCompleted", originalDetails.getJurorNumber());
        originalDetails.setProcessingComplete(Boolean.TRUE);
        originalDetails.setCompletedAt(LocalDateTime.now());

        if (originalDetails.getReplyType().getType().equals(ReplyMethod.PAPER.getDescription())) {
            jurorPaperResponseRepository.save((PaperResponse) originalDetails);
        } else if (originalDetails.getReplyType().getType().equals(ReplyMethod.DIGITAL.getDescription())) {
            jurorDigitalResponseRepository.save((DigitalResponse) originalDetails);
        }

        log.trace("Juror: {}. Exit markSummonReplyAsCompleted", originalDetails.getJurorNumber());
    }

}
