package uk.gov.hmcts.juror.api.moj.service;

import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendment;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.JurorPaperResponseException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAuditChangeService;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of operations for updating the status of a Juror's response by a Bureau officer.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SummonsReplyStatusUpdateServiceImpl implements SummonsReplyStatusUpdateService, SummonsReplyMergeService {
    public static final String COPY_RESPONSE_TO_GENERIC_JUROR_RESPONSE_POJO = "Juror: {}. Copying properties from {} "
        + "to a generic juror response pojo";

    @NonNull
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @NotNull
    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    @NonNull
    private final JurorResponseAuditRepositoryMod auditRepository;
    @NonNull
    private final AssignOnUpdateServiceMod assignOnUpdateService;
    @NotNull
    private final EntityManager entityManager;
    @NonNull
    private final JurorRepository jurorRepository;
    @NonNull
    private final JurorPoolRepository jurorPoolRepository;
    @NonNull
    private final JurorStatusRepository jurorStatusRepository;
    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;
    @NonNull
    private final JurorReasonableAdjustmentRepository jurorReasonableAdjustmentsRepository;
    @NonNull
    private final WelshCourtLocationRepository welshCourtLocationRepository;
    @NonNull
    private final JurorRecordService jurorRecordService;
    @NonNull
    private final JurorAuditChangeService jurorAuditChangeService;

    private static final String TITLE = "title";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String PHONE_NO = "phoneNumber";
    private static final String ALT_PHONE_NO = "altPhoneNumber";
    private static final String EMAIL = "email";

    private static final String ADDRESS = "address";

    private static final String POSTCODE = "postcode";

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
                                          final BureauJWTPayload payload) {
        log.debug("Updating status for juror {} to {}", jurorNumber, status.getDescription());
        PaperResponse paperResponse = DataUtils.getJurorPaperResponse(jurorNumber,
            jurorPaperResponseRepository);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        // store the current processing status (to be used as the "changed from" value in the audit/history table)
        final ProcessingStatus initialProcessingStatus = paperResponse.getProcessingStatus();

        paperResponse.setProcessingStatus(status);

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
        // TODO - Persist an audit entry to record the Processing Status change of the Juror Response
    }

    /**
     * Update the processing status of a Juror digital response
     *
     * @param jurorNumber 9-digit numeric string to identify the juror associated with this response
     * @param status      The processing status this response should be updated to
     * @param payload     Bureau authentication Json Web Token payload.
     */
    @Override
    @Transactional
    public void updateDigitalJurorResponseStatus(final String jurorNumber,
        final ProcessingStatus status,
        final BureauJWTPayload payload) {
        final String auditorUsername = payload.getLogin();

        log.debug("Updating status for juror {} to {}", jurorNumber, status.getDescription());
        DigitalResponse jurorResponse = DataUtils.getJurorDigitalResponse(jurorNumber, jurorDigitalResponseRepository);

        if (jurorResponse == null) {
            log.error("No juror response found for juror number {}", jurorNumber);
            throw new JurorPaperResponseException.NoJurorPaperResponseRecordFound(jurorNumber);
        }

        entityManager.detach(jurorResponse);
        final ProcessingStatus auditProcessingStatus = jurorResponse.getProcessingStatus();

        // JDB-2685: if no staff assigned, assign current login
        if (null == jurorResponse.getStaff()) {
            assignOnUpdateService.assignToCurrentLogin(jurorResponse, auditorUsername);
        }

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        // store the current processing status (to be used as the "changed from" value in the audit/history table)
        final ProcessingStatus initialProcessingStatus = jurorResponse.getProcessingStatus();
        jurorResponse.setProcessingStatus(status);

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

        log.info("Updated juror '{}' processing status from '{}' to '{}'", jurorNumber, initialProcessingStatus,
            jurorResponse.getProcessingStatus()
        );
        JurorResponseAuditMod responseAudit = auditRepository.save(JurorResponseAuditMod.builder()
            .jurorNumber(jurorResponse.getJurorNumber())
            .login(auditorUsername)
            .oldProcessingStatus(auditProcessingStatus)
            .newProcessingStatus(jurorResponse.getProcessingStatus())
            .build());
        log.trace("Audit entry: {}", responseAudit);
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

        jurorPoolRepository.save(jurorPool);
        log.trace("Juror: {}. Exit mergeReasonableAdjustments", jurorNumber);
    }

    /**
     * Create a Map containing property names as keys and a boolean result indicating whether the value provided in the
     * summons reply is different to the value currently stored in the juror record.
     * <p></p>
     * Property names are intentionally written in camelCase but with spaces separating individual words, for example,
     * the lastName property is referenced by the key "last Name" - this makes it easier to utilise a single key for
     * both property reference in code (remove space between words) and text descriptions in a more readable format
     * for audit/history records (capitalise the first letter and maintain the spaces)
     *
     * @param juror                    the original juror record, to reference the existing juror details
     * @param jurorResponse the transient paper/digital response pojo, to reference the newly provided
     *                                 juror details from a summons reply
     *
     * @return a Map containing juror response property names as keys and a Boolean result indicating whether the
     *     property values differ between the original juror record and the new juror summons reply, true means there
     *     is a
     *     difference, false means there is no difference
     */
    private Map<String, Boolean> initChangedPropertyMap(Juror juror,
                                                        AbstractJurorResponse jurorResponse) {
        // check for changes between the new/updated values and the juror record values
        Map<String, Boolean> changedPropertiesMap = new HashMap<>();

        // new title value CAN be null
        changedPropertiesMap.put(TITLE, (juror.getTitle() != null && jurorResponse.getTitle() == null)
            || hasPropertyChanged(jurorResponse.getTitle(), juror.getTitle()));
        changedPropertiesMap.put(
            "first Name",
            hasPropertyChanged(jurorResponse.getFirstName(), juror.getFirstName())
        );
        changedPropertiesMap.put(
            "last Name",
            hasPropertyChanged(jurorResponse.getLastName(), juror.getLastName())
        );

        LocalDate originalDate = setOriginalDateOfBirth(juror.getDateOfBirth());
        changedPropertiesMap.put("date Of Birth", hasPropertyChanged(jurorResponse.getDateOfBirth(),
            originalDate));

        changedPropertiesMap.put(ADDRESS, hasAddressChanged(jurorResponse, juror));
        changedPropertiesMap.put(
            POSTCODE,
            hasPropertyChanged(jurorResponse.getPostcode(), juror.getPostcode())
        );

        return changedPropertiesMap;
    }

    /**
     * In most cases the date of birth field will be empty (null) when the Juror record was initially created from the
     * voters table - the part amendment table will insert a record when the date of birth is changed, in
     * the event of the initial assignment (from null to a date value) a default date is used 1901-01-01 (YYYY-MM-DD)
     * for comparison (to avoid a null pointer exception).
     *
     * @return LocalDate object with either the Juror's date of birth or a default date value to use for Part Amendments
     */
    private LocalDate setOriginalDateOfBirth(LocalDate jurorPoolDob) {
        final LocalDate defaultNullReplacementDob = LocalDate.of(1901, 1, 1);
        return jurorPoolDob != null ? jurorPoolDob : defaultNullReplacementDob;
    }

    private boolean hasPropertyChanged(String updatedValue, String originalValue) {
        if (updatedValue != null) {
            if (originalValue != null) {
                return updatedValue.compareTo(originalValue) != 0;
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean hasPropertyChanged(LocalDate updatedValue, LocalDate originalValue) {
        if (updatedValue != null) {
            if (originalValue != null) {
                return !updatedValue.isEqual(originalValue);
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean hasAddressChanged(AbstractJurorResponse updatedDetails, Juror juror) {
        String newAddress = formatForConcat(updatedDetails.getAddressLine1())
            + formatForConcat(updatedDetails.getAddressLine2())
            + formatForConcat(updatedDetails.getAddressLine3())
            + formatForConcat(updatedDetails.getAddressLine4())
            + formatForConcat(updatedDetails.getAddressLine5());

        String oldAddress = formatForConcat(juror.getAddressLine1())
            + formatForConcat(juror.getAddressLine2())
            + formatForConcat(juror.getAddressLine3())
            + formatForConcat(juror.getAddressLine4())
            + formatForConcat(juror.getAddressLine5());

        return oldAddress.compareToIgnoreCase(newAddress) != 0;
    }

    private String formatForConcat(String property) {
        String nullDefault = "";
        return Objects.toString(property, nullDefault).trim();
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

    private PartAmendment createBasePartAmendment(Juror juror, String auditorUsername) {
        final String lineSeparator = ", ";
        PartAmendment allAmendments = new PartAmendment();
        BeanUtils.copyProperties(juror, allAmendments);

        // initialise the original date of birth property from the juror record
        LocalDate dob = juror.getDateOfBirth();
        if (dob == null) {
            // if the juror record date of birth is null, use a default date for part amendments (required by Heritage)
            dob = LocalDate.of(1901, 1, 1);
        }

        allAmendments.setDateOfBirth(dob);

        final String fullAddress = Stream.of(
                juror.getAddressLine1(),
                juror.getAddressLine2(),
                juror.getAddressLine3(),
                juror.getAddressLine4(),
                juror.getAddressLine5()
            )
            .filter(string -> string != null && !string.isEmpty())
            .map(lineSeparator::concat)
            .collect(Collectors.joining())
            .replaceFirst(lineSeparator, "");

        allAmendments.setAddress(fullAddress);
        allAmendments.setOwner(JurorDigitalApplication.JUROR_OWNER);
        allAmendments.setEditUserId(auditorUsername);

        return allAmendments;
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
        originalDetails.setCompletedAt(LocalDate.now());

        if (originalDetails.getReplyType().getType().equals(ReplyMethod.PAPER.getDescription())) {
            jurorPaperResponseRepository.save((PaperResponse) originalDetails);
        } else if (originalDetails.getReplyType().getType().equals(ReplyMethod.DIGITAL.getDescription())) {
            jurorDigitalResponseRepository.save((DigitalResponse) originalDetails);
        }

        log.trace("Juror: {}. Exit markSummonReplyAsCompleted", originalDetails.getJurorNumber());
    }

}
