package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLog;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLogItem;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLogRepository;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLogType;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.IContactCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.ContactCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactLogRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.ReasonableAdjustmentsRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.CjsEmploymentDetailsDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.DeferralExcusalDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.FirstPersonJurorDetailsDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.JurorEligibilityDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.JurorNoteDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.JurorPhoneLogDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.ReasonableAdjustmentsDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.ThirdPartyJurorDetailsDto;
import static uk.gov.hmcts.juror.api.bureau.domain.ReasonableAdjustmentQueries.byJurorNumberAndCode;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.ALT_PHONE_NUMBER;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.BAIL;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.BAIL_DETAILS;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.CONVICTIONS;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.CONVICTIONS_DETAILS;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.DEFERRAL_DATE;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.DEFERRAL_REASON;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.DOB;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.EMAIL;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.EXCUSAL_REASON;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.FIRST_NAME;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.JUROR_EMAIL_DETAILS;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.JUROR_PHONE_DETAILS;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.LAST_NAME;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.MENTAL_HEALTH_ACT;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.MENTAL_HEALTH_ACT_DETAILS;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.PHONE_NUMBER;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.POSTCODE;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.RELATIONSHIP;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.RESIDENCY;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.RESIDENCY_DETAIL;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_EMAIL_ADDRESS;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_FIRST_NAME;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_LAST_NAME;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_MAIN_PHONE;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_OTHER_PHONE;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_OTHER_REASON;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_REASON;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.TITLE;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseUpdateServiceImpl implements ResponseUpdateService {
    private final ContactCodeRepository contactCodeRepository;
    static final String HASH_SALT = "445NlwAglWA78Vh9DKbVwN5vPHsvy2kA";
    public static final String UPDATED_NOTES = "Updated notes";
    private static final String MESSAGE = "User {} applied {} total changes to response {}";
    private static final String OTHER_1 = "Other";
    private final JurorPoolRepository jurorRepository;
    private final JurorHistoryRepository partHistRepository;
    private final ContactLogRepository phoneLogRepository;
    private final JurorDigitalResponseRepositoryMod responseRepository;
    private final ChangeLogRepository changeLogRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final JurorReasonableAdjustmentRepository bureauJurorSpecialNeedsRepository;
    private final ReasonableAdjustmentsRepository reasonableAdjustmentsRepository;
    private final JurorResponseCjsEmploymentRepositoryMod cjsRepository;
    private final AssignOnUpdateService assignOnUpdateService;
    private final JurorResponseAuditRepositoryMod responseAuditRepository;


    @Override
    @Transactional(readOnly = true)
    public JurorNoteDto notesByJurorNumber(final String jurorId) {
        final JurorPool juror = jurorRepository.findByJurorJurorNumber(jurorId);

        if (juror != null) {
            final String notes = juror.getJuror().getNotes();
            return new JurorNoteDto(notes, comparisonHash(notes));
        } else {
            log.error("No POOL entry found for PART_NO={}", jurorId);
            throw new NoteNotFoundException("POOL entry not found");
        }
    }

    @Override
    @Transactional
    public void updateNote(final JurorNoteDto noteDto,
                           final String jurorId,
                           final String auditUser) {
        final JurorPool juror = jurorRepository.findByJurorJurorNumber(jurorId);

        if (juror != null) {
            if (comparisonHash(juror.getJuror().getNotes()).compareTo(noteDto.getVersion()) == 0) {
                // hashcode matches, save changes to notes
                juror.getJuror().setNotes(noteDto.getNotes());
                final JurorPool updatedPool = jurorRepository.save(juror);

                // JDB-2685: if no staff assigned, assign current login
                DigitalResponse jurorResponse = responseRepository.findByJurorNumber(jurorId);
                if (null == jurorResponse.getStaff()) {
                    assignOnUpdateService.assignToCurrentLogin(jurorResponse, auditUser);
                }

                // 2. persist response
                if (log.isTraceEnabled()) {
                    log.trace("Persisting staff assignment update for response: {}", jurorResponse);
                }
                responseRepository.save(jurorResponse);

                if (log.isDebugEnabled()) {
                    log.debug("Updated note for juror {}: {}", jurorId, updatedPool.getJuror().getNotes());
                }

                // audit the change to the notes column
                partHistRepository.save(JurorHistory.builder()
                    .createdBy(auditUser)
                    .poolNumber(updatedPool.getPoolNumber())
                    .otherInformation(UPDATED_NOTES)
                    .historyCode(HistoryCodeMod.POOL_EDIT)
                    .jurorNumber(updatedPool.getJurorNumber())
                    .build());
            } else {
                log.debug("Note failed hash comparison.");
                if (log.isTraceEnabled()) {
                    log.trace("UI={} DB={}", noteDto.getVersion(), comparisonHash(juror.getJuror().getNotes()));
                }
                throw new NoteComparisonFailureException();
            }
        } else {
            log.warn("No Juror entry found for PART_NO={}", jurorId);
            throw new NoteNotFoundException("Juror entry not found");
        }
    }

    /**
     * Md5 hash of the content of the notes supplied concatenated with the value of {@link #HASH_SALT}.
     *
     * @param notes Notes before modification.
     * @return Hashcode of <employer>HASH_SALT.concat(notes)</employer>
     * @implNote This value is not for persistence!
     */
    static String comparisonHash(final String notes) {
        final String saltedNotes = HASH_SALT + notes;
        final HashCode hashCode = Hashing.md5().hashString(saltedNotes, StandardCharsets.UTF_8);
        return hashCode.toString();
    }

    @Override
    @Transactional
    public void updatePhoneLog(final JurorPhoneLogDto phoneLogDto,
                               final String jurorId, final String auditUser) {
        final ContactLog phoneLog = ContactLog.builder()
            .jurorNumber(jurorId)
            .notes(phoneLogDto.getNotes())
            .startCall(LocalDateTime.now())
            .username(auditUser)
            .enquiryType(RepositoryUtils.retrieveFromDatabase(IContactCode.GENERAL.getCode(), contactCodeRepository))
            //   .owner(JurorDigitalApplication.JUROR_OWNER)
            .build();
        if (log.isDebugEnabled()) {
            log.debug("User {} added to juror {} phone log: {}", auditUser, jurorId, phoneLog);
        }
        phoneLogRepository.save(phoneLog);

        // JDB-2685: if no staff assigned, assign current login
        DigitalResponse savedResponse = responseRepository.findByJurorNumber(jurorId);
        if (null == savedResponse.getStaff()) {
            assignOnUpdateService.assignToCurrentLogin(savedResponse, auditUser);
        }

        log.info("Phone log inserted for Juror response {}", jurorId);
    }

    @Transactional
    @Override
    public void updateJurorDetailsFirstPerson(final FirstPersonJurorDetailsDto dto,
                                              final String jurorId, final String login)
        throws BureauOptimisticLockingException {
        log.debug("First person response {} juror details edit", jurorId);
        final DigitalResponse domain = responseRepository.findByJurorNumber(jurorId);
        final User staff = userRepository.findByUsername(login);

        validateResponseState(jurorId, login, domain, staff);
        applyOptimisticLocking(domain, dto.getVersion());

        // empty changelog for the upserts
        final ChangeLog changeLog = ChangeLog.builder()
            .jurorNumber(jurorId)
            .staff(staff)
            .type(ChangeLogType.JUROR_DETAILS)
            .notes(dto.getNotes())
            .build();

        /*
         * Update the response domain with the information from the dto.
         */
        updateAndLog(TITLE, domain, dto.getTitle(), changeLog);
        updateAndLog(FIRST_NAME, domain, dto.getFirstName(), changeLog);
        updateAndLog(LAST_NAME, domain, dto.getLastName(), changeLog);
        updateAndLog("addressLine1", domain, dto.getAddress(), changeLog);
        updateAndLog("addressLine2", domain, dto.getAddress2(), changeLog);
        updateAndLog("addressLine3", domain, dto.getAddress3(), changeLog);
        updateAndLog("addressLine4", domain, dto.getAddress4(), changeLog);
        updateAndLog("addressLine5", domain, dto.getAddress5(), changeLog);
        updateAndLog(POSTCODE, domain, dto.getPostcode(), changeLog);
        updateAndLog(DOB, domain, dto.getDob(), changeLog);
        updateAndLog(PHONE_NUMBER, domain, dto.getMainPhone(), changeLog);
        updateAndLog(ALT_PHONE_NUMBER, domain, dto.getAltPhone(), changeLog);
        updateAndLog(EMAIL, domain, dto.getEmailAddress(), changeLog);

        // JDB-2685: if no staff assigned, assign current login
        if (null == domain.getStaff()) {
            assignOnUpdateService.assignToCurrentLogin(domain, login);
        }

        saveUpdatesOptimistically(changeLog, domain);

        if (log.isDebugEnabled()) {
            int totalChanges = 0;
            if (changeLog != null && changeLog.getChangeLogItems() != null) {
                totalChanges = changeLog.getChangeLogItems().size();
            }
            log.debug(MESSAGE, login, totalChanges, jurorId);
        }
        log.info("Bureau user {} updated juror details section for {}", login, jurorId);
    }

    @Transactional
    @Override
    public void updateJurorDetailsThirdParty(final ThirdPartyJurorDetailsDto dto,
                                             final String jurorId, final String login)
        throws BureauOptimisticLockingException {
        log.debug("Third party response {} juror details edit", jurorId);
        final DigitalResponse domain = responseRepository.findByJurorNumber(jurorId);
        final User staff = userRepository.findByUsername(login);

        validateResponseState(jurorId, login, domain, staff);
        applyOptimisticLocking(domain, dto.getVersion());

        // empty changelog for the upserts
        final ChangeLog changeLog = ChangeLog.builder()
            .jurorNumber(jurorId)
            .staff(staff)
            .type(ChangeLogType.JUROR_DETAILS)
            .notes(dto.getNotes())
            .build();
        /*
         * Update the response domain with the information from the dto.
         */
        updateAndLog(TITLE, domain, dto.getTitle(), changeLog);
        updateAndLog(FIRST_NAME, domain, dto.getFirstName(), changeLog);
        updateAndLog(LAST_NAME, domain, dto.getLastName(), changeLog);
        updateAndLog("addressLine1", domain, dto.getAddress(), changeLog);
        updateAndLog("addressLine2", domain, dto.getAddress2(), changeLog);
        updateAndLog("addressLine3", domain, dto.getAddress3(), changeLog);
        updateAndLog("addressLine4", domain, dto.getAddress4(), changeLog);
        updateAndLog("addressLine5", domain, dto.getAddress5(), changeLog);
        updateAndLog(POSTCODE, domain, dto.getPostcode(), changeLog);
        updateAndLog(DOB, domain, dto.getDob(), changeLog);
        updateAndLog(PHONE_NUMBER, domain, dto.getMainPhone(), changeLog);
        updateAndLog(ALT_PHONE_NUMBER, domain, dto.getAltPhone(), changeLog);
        updateAndLog(EMAIL, domain, dto.getEmailAddress(), changeLog);
        //third party unique fields
        updateAndLog(JUROR_PHONE_DETAILS, domain, dto.getUseJurorPhone(), changeLog);
        updateAndLog(JUROR_EMAIL_DETAILS, domain, dto.getUseJurorEmail(), changeLog);
        updateAndLog(THIRD_PARTY_FIRST_NAME, domain, dto.getThirdPartyFirstName(), changeLog);
        updateAndLog(THIRD_PARTY_LAST_NAME, domain, dto.getThirdPartyLastName(), changeLog);
        updateAndLog(RELATIONSHIP, domain, dto.getRelationship(), changeLog);
        updateAndLog(THIRD_PARTY_REASON, domain, dto.getThirdPartyReason(), changeLog);
        updateAndLog(THIRD_PARTY_OTHER_REASON, domain, dto.getThirdPartyOtherReason(), changeLog);
        updateAndLog(THIRD_PARTY_MAIN_PHONE, domain, dto.getThirdPartyMainPhone(), changeLog);
        updateAndLog(THIRD_PARTY_OTHER_PHONE, domain, dto.getThirdPartyAltPhone(), changeLog);
        updateAndLog(THIRD_PARTY_EMAIL_ADDRESS, domain, dto.getThirdPartyEmail(), changeLog);

        // JDB-2685: if no staff assigned, assign current login
        if (null == domain.getStaff()) {
            assignOnUpdateService.assignToCurrentLogin(domain, login);
        }

        saveUpdatesOptimistically(changeLog, domain);

        if (log.isDebugEnabled()) {
            log.debug(
                MESSAGE,
                login,
                changeLog.getChangeLogItems() != null
                    ?
                    changeLog.getChangeLogItems().size()
                    :
                        0,
                jurorId
            );
        }
        log.info("Bureau user {} updated juror details section for {}", login, jurorId);
    }

    @Transactional
    @Override
    public void updateExcusalDeferral(final DeferralExcusalDto dto,
                                      final String jurorId, final String login)
        throws BureauOptimisticLockingException {

        log.debug("Deferral/excusal {} juror details edit", jurorId);
        final DigitalResponse domain = responseRepository.findByJurorNumber(jurorId);
        final User staff = userRepository.findByUsername(login);

        validateResponseState(jurorId, login, domain, staff);
        applyOptimisticLocking(domain, dto.getVersion());

        // empty changelog for the updates
        final ChangeLog changeLog = ChangeLog.builder()
            .jurorNumber(jurorId)
            .staff(staff)
            .type(ChangeLogType.DEFERRAL_EXCUSAL)
            .notes(dto.getNotes())
            .build();

        /*
         * Update the response domain with the information from the dto.
         */
        log.debug("Updating {} as {}", jurorId, dto.getExcusal().name());
        switch (dto.getExcusal()) {
            case EXCUSAL:
                updateAndLog(EXCUSAL_REASON, domain, dto.getReason(), changeLog);
                updateAndLog(DEFERRAL_REASON, domain, null, changeLog);
                updateAndLog(DEFERRAL_DATE, domain, null, changeLog);
                break;
            case DEFERRAL:
                updateAndLog(DEFERRAL_REASON, domain, dto.getReason(), changeLog);
                updateAndLog(DEFERRAL_DATE, domain, dto.getDeferralDates(), changeLog);
                updateAndLog(EXCUSAL_REASON, domain, null, changeLog);
                break;
            case CONFIRMATION:
                log.debug("Removing excusal and deferral information");
                updateAndLog(EXCUSAL_REASON, domain, null, changeLog);
                updateAndLog(DEFERRAL_REASON, domain, null, changeLog);
                updateAndLog(DEFERRAL_DATE, domain, null, changeLog);
                break;
            default:
                log.error("Unsupported DeferralExcusalUpdateType!");
                throw new IllegalStateException("Unsupported DeferralExcusalUpdateType: " + dto.getExcusal().name());
        }

        // JDB-2685: if no staff assigned, assign current login
        if (null == domain.getStaff()) {
            assignOnUpdateService.assignToCurrentLogin(domain, login);
        }

        saveUpdatesOptimistically(changeLog, domain);

        if (log.isDebugEnabled() && null != changeLog.getChangeLogItems()) {
            log.debug(MESSAGE, login, changeLog.getChangeLogItems().size(), jurorId);
        }
        log.info("Bureau user {} updated excusal/deferral section for {}", login, jurorId);
    }

    @Transactional
    @Override
    public void updateSpecialNeeds(final ReasonableAdjustmentsDto dto, final String jurorId, final String login)
        throws BureauOptimisticLockingException {
        log.debug("Reasonable adjustment juror {} edit", jurorId);
        final DigitalResponse domain = responseRepository.findByJurorNumber(jurorId);
        final User staff = userRepository.findByUsername(login);

        validateResponseState(jurorId, login, domain, staff);
        applyOptimisticLocking(domain, dto.getVersion());

        // empty changelog for the updates
        final ChangeLog changeLog = ChangeLog.builder()
            .jurorNumber(jurorId)
            .staff(staff)
            .type(ChangeLogType.REASONABLE_ADJUSTMENTS)
            .notes(dto.getNotes())
            .build();

        // perform the updates
        updateAndLog("reasonableAdjustmentsArrangements", domain, dto.getSpecialArrangements(), changeLog);
        updateAndLogSpecialNeed(jurorId, SpecNeed.LIMITED_MOBILITY, dto.getLimitedMobility(), changeLog);
        updateAndLogSpecialNeed(jurorId, SpecNeed.HEARING_IMPAIRMENT, dto.getHearingImpairment(), changeLog);
        updateAndLogSpecialNeed(jurorId, SpecNeed.DIABETIC, dto.getDiabetes(), changeLog);
        updateAndLogSpecialNeed(jurorId, SpecNeed.SIGHT_IMPAIRMENT, dto.getSightImpairment(), changeLog);
        updateAndLogSpecialNeed(jurorId, SpecNeed.LEARNING_DISABILITY, dto.getLearningDisability(), changeLog);
        updateAndLogSpecialNeed(jurorId, SpecNeed.OTHER, dto.getOther(), changeLog);

        // JDB-2685: if no staff assigned, assign current login
        if (null == domain.getStaff()) {
            assignOnUpdateService.assignToCurrentLogin(domain, login);
        }

        saveUpdatesOptimistically(changeLog, domain);
        log.info("Bureau user {} updated reasonable adjustments section for {}", login, jurorId);
    }

    @Transactional
    @Override
    public void updateCjs(final CjsEmploymentDetailsDto dto, final String jurorId, final String login)
        throws BureauOptimisticLockingException {
        log.debug("CJS employment for juror {} edit", jurorId);
        final DigitalResponse domain = responseRepository.findByJurorNumber(jurorId);
        final User staff = userRepository.findByUsername(login);

        validateResponseState(jurorId, login, domain, staff);
        applyOptimisticLocking(domain, dto.getVersion());

        // empty changelog for the updates
        final ChangeLog changeLog = ChangeLog.builder()
            .jurorNumber(jurorId)
            .staff(staff)
            .type(ChangeLogType.CJS_EMPLOYMENTS)
            .notes(dto.getNotes())
            .build();

        String ncaDatabaseValue = null;
        if (null != dto.getNcaEmployment() && dto.getNcaEmployment()) {
            // we have received a value for the NCA checkbox and it was ticked
            ncaDatabaseValue = CjsEmployment.NCA.description;
        }

        String judiciaryDatabaseValue = null;
        if (null != dto.getJudiciaryEmployment() && dto.getJudiciaryEmployment()) {
            // we have received a value for the Judiciary checkbox and it was ticked
            judiciaryDatabaseValue = CjsEmployment.JUDICIARY.description;
        }

        String hmctsDatabaseValue = null;
        if (null != dto.getHmctsEmployment() && dto.getHmctsEmployment()) {
            // we have received a value for the HMCTS checkbox and it was ticked
            hmctsDatabaseValue = CjsEmployment.HMCTS.description;
        }

        // create CjsEmployment object
        updateAndLogCjs(jurorId, CjsEmployment.POLICE, dto.getPoliceForceDetails(), changeLog);
        updateAndLogCjs(jurorId, CjsEmployment.PRISON_SERVICE, dto.getPrisonServiceDetails(), changeLog);
        updateAndLogCjs(jurorId, CjsEmployment.NCA, ncaDatabaseValue, changeLog);
        updateAndLogCjs(jurorId, CjsEmployment.JUDICIARY, judiciaryDatabaseValue, changeLog);
        updateAndLogCjs(jurorId, CjsEmployment.HMCTS, hmctsDatabaseValue, changeLog);
        updateAndLogCjs(jurorId, CjsEmployment.OTHER, dto.getOtherDetails(), changeLog);

        // JDB-2685: if no staff assigned, assign current login
        if (null == domain.getStaff()) {
            assignOnUpdateService.assignToCurrentLogin(domain, login);
        }

        saveUpdatesOptimistically(changeLog, domain);
        log.info("Bureau user {} updated CJS employments section for {}", login, jurorId);
    }

    @Transactional
    @Override
    public void updateResponseStatus(String jurorId, ProcessingStatus status, Integer version, String login) {

        final List<Integer> poolStatus = Arrays.asList(
            IJurorStatus.RESPONDED,
            IJurorStatus.EXCUSED,
            IJurorStatus.DISQUALIFIED,
            IJurorStatus.DEFERRED);

        log.debug("Start - update response status, when legacy status changed for juror {}", jurorId);

        final DigitalResponse domain = responseRepository.findByJurorNumber(jurorId);
        final JurorPool juror = jurorRepository.findByJurorJurorNumber(jurorId);
        DigitalResponse savedResponse = null;

        if (jurorId != null) {

            savedResponse = responseRepository.findByJurorNumber(jurorId);
        }

        applyOptimisticLocking(domain, version);

        if (juror != null) {
            //update response if pool status = 2,5, 6 & 7
            if (poolStatus.contains(juror.getStatus().getStatus())) {
                domain.setProcessingStatus(ProcessingStatus.CLOSED);
                domain.setProcessingComplete(Boolean.TRUE);
            } else if (juror.getStatus().getStatus() == IJurorStatus.ADDITIONAL_INFO) {

                if (status.equals(ProcessingStatus.AWAITING_COURT_REPLY)) {
                    domain.setProcessingStatus(ProcessingStatus.AWAITING_COURT_REPLY);

                } else if (status.equals(ProcessingStatus.AWAITING_CONTACT)) {
                    domain.setProcessingStatus(ProcessingStatus.AWAITING_CONTACT);

                } else if (status.equals(ProcessingStatus.AWAITING_TRANSLATION)) {
                    domain.setProcessingStatus(ProcessingStatus.AWAITING_TRANSLATION);
                }
            }

            log.trace("updating juror '{}' processing status to '{}'", jurorId, status.getDescription());
            responseRepository.save(domain);
            log.trace("Updated juror '{}' processing status to '{}'", jurorId, status.getDescription());

            if (savedResponse != null) {
                //audit response status change
                responseAuditRepository.save(JurorResponseAuditMod.builder()
                    .jurorNumber(jurorId)
                    .login(login)
                    .oldProcessingStatus(savedResponse.getProcessingStatus())
                    .newProcessingStatus(status)
                    .build());
                log.debug("response audit updated  old processing status {}, new processing status {} for juror {}",
                    jurorId, savedResponse.getProcessingStatus().getDescription(),
                    domain.getProcessingStatus().getDescription()
                );
            } else {
                log.error("SavedResponse is null");
            }

        }

    }

    @Transactional
    @Override
    public void updateJurorEligibility(final JurorEligibilityDto dto,
                                       final String jurorId, final String login) {
        log.debug("Third party response {} juror details edit", jurorId);
        final DigitalResponse domain = responseRepository.findByJurorNumber(jurorId);
        final User staff = userRepository.findByUsername(login);

        validateResponseState(jurorId, login, domain, staff);
        applyOptimisticLocking(domain, dto.getVersion());

        // empty changelog for the upserts
        final ChangeLog changeLog = ChangeLog.builder()
            .jurorNumber(jurorId)
            .staff(staff)
            .type(ChangeLogType.ELIGIBILITY)
            .notes(dto.getNotes())
            .build();
        /*
         * Update the response domain with the information from the dto.
         */
        updateAndLog(RESIDENCY, domain, dto.isResidency(), changeLog);
        updateAndLog(RESIDENCY_DETAIL, domain, dto.getResidencyDetails(), changeLog);
        updateAndLog(MENTAL_HEALTH_ACT, domain, dto.isMentalHealthAct(), changeLog);
        updateAndLog(MENTAL_HEALTH_ACT_DETAILS, domain, dto.getMentalHealthActDetails(), changeLog);
        updateAndLog(BAIL, domain, dto.isBail(), changeLog);
        updateAndLog(BAIL_DETAILS, domain, dto.getBailDetails(), changeLog);
        updateAndLog(CONVICTIONS, domain, dto.isConvictions(), changeLog);
        updateAndLog(CONVICTIONS_DETAILS, domain, dto.getConvictionsDetails(), changeLog);

        // JDB-2685: if no staff assigned, assign current login
        if (null == domain.getStaff()) {
            assignOnUpdateService.assignToCurrentLogin(domain, login);
        }

        saveUpdatesOptimistically(changeLog, domain);

        if (log.isDebugEnabled()) {
            log.debug(MESSAGE, login, changeLog.getChangeLogItems().size(), jurorId);
        }
        log.info("Bureau user {} updated juror eligibility section for {}", login, jurorId);
    }

    /**
     * Enumerated data class for valid Reasonable adjustment types. Used for queries and audit during update.
     *
     * @see #updateAndLogSpecialNeed(String, SpecNeed, String, ChangeLog)
     */
    private enum SpecNeed {
        LIMITED_MOBILITY("L", "Limited Mobility"),
        HEARING_IMPAIRMENT("H", "Hearing Impairment"),
        DIABETIC("I", "Diabetes"),
        SIGHT_IMPAIRMENT("V", "Visual Impairment"),
        /**
         * R=READING (assume this is maps to learning disability).
         */
        LEARNING_DISABILITY("R", "Learning Disability"),
        OTHER("O", OTHER_1);

        private final String code;
        private final String description;

        SpecNeed(String code, String description) {
            this.code = code;
            this.description = description;
        }

        /**
         * Code for the special need.
         *
         * @return The employer key value
         * @see SpecNeed#code for domain primary key.
         */
        public String getCode() {
            return code;
        }

        /**
         * Human readable description of the special need.
         *
         * @return Description text
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * Enumerated data class for valid CJS Employment types. Used for queries and audit during update.
     *
     * @see #updateAndLogCjs(String, CjsEmployment, String, ChangeLog)
     */
    public enum CjsEmployment {
        POLICE("Police Force", "Police Force"),
        PRISON_SERVICE("HM Prison Service", "HM Prison Service"),
        NCA("National Crime Agency", "National Crime Agency"),
        JUDICIARY("Judiciary", "Judiciary"),
        HMCTS("HMCTS", "HMCTS"),
        OTHER(OTHER_1, OTHER_1);

        private final String employer;
        private final String description;

        CjsEmployment(String employer, String description) {
            this.employer = employer;
            this.description = description;
        }

        /**
         * Code for the employment.
         *
         * @return The employer key value
         * @see CjsEmployment#employer for domain primary key.
         */
        public String getEmployer() {
            return employer;
        }

        /**
         * Human readable description of the employment.
         *
         * @return Description text
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * Update a single special need entry with a new value if that value has changed, creating a changelog entry of
     * the event if the
     * value was different.
     *
     * @param jurorId      Juror id whose special needs are being edited
     * @param specNeedType Enumerated type of the special need being updated
     * @param value        Value being set for the special need in the domain object
     * @param changeLog    Change log to add a {@link ChangeLogItem} auditing the changes (if any)
     */
    private void updateAndLogSpecialNeed(final String jurorId, final SpecNeed specNeedType, final String value,
                                         final ChangeLog changeLog) {
        // find existing special need of type
        final String key = specNeedType.getCode();
        final String description = specNeedType.getDescription();
        Optional<JurorReasonableAdjustment> optSpNeeds = bureauJurorSpecialNeedsRepository.findOne(
            byJurorNumberAndCode(jurorId, key));
        final JurorReasonableAdjustment existingSpecNeed = optSpNeeds.orElse(null);
        if (null != value) {
            log.debug("Updating special need employer '{}'", key);
            JurorReasonableAdjustment savedSpecialNeed;
            if (null != existingSpecNeed) {
                log.debug("Updating existing special need {}", existingSpecNeed);
                final String oldValue = existingSpecNeed.getReasonableAdjustmentDetail();
                existingSpecNeed.setReasonableAdjustmentDetail(value);
                savedSpecialNeed = bureauJurorSpecialNeedsRepository.save(existingSpecNeed);
                changeLog.addChangeLogItem(
                    new ChangeLogItem(key, oldValue, key, savedSpecialNeed.getReasonableAdjustmentDetail()));
            } else {
                // insert new need

                savedSpecialNeed = bureauJurorSpecialNeedsRepository.save(JurorReasonableAdjustment.builder()
                    .reasonableAdjustmentDetail(value)
                    .jurorNumber(jurorId)
                    .reasonableAdjustment(reasonableAdjustmentsRepository.findByCode(key))
                    .build());
                changeLog.addChangeLogItem(
                    new ChangeLogItem(null, null, key, savedSpecialNeed.getReasonableAdjustmentDetail()));
            }
            log.debug("Saved {}", savedSpecialNeed);
        } else {
            if (null != existingSpecNeed) {
                log.debug("Deleting {}", description);
                bureauJurorSpecialNeedsRepository.delete(existingSpecNeed);
                log.info("Deleted existing {} '{}'", description, key);
                changeLog.addChangeLogItem(
                    new ChangeLogItem(key, existingSpecNeed.getReasonableAdjustmentDetail(), null, null));
            } else {
                log.trace("No {} employer '{}' to update", description, key);
            }
        }
    }

    /**
     * Update a single CJS employment entry with a new value if that value has changed, creating a changelog entry of
     * the event if the value was different.
     *
     * @param jurorId           Juror id whose CJS employment details are being edited
     * @param cjsEmploymentType Enumerated type of the special need being updated
     * @param value             Value being set for the CJS detail in the domain object
     * @param changeLog         Change log to add a {@link ChangeLogItem} auditing the changes (if any)
     */
    private void updateAndLogCjs(final String jurorId, final CjsEmployment cjsEmploymentType, final String value,
                                 final ChangeLog changeLog) {
        // find existing CJS entry
        final String key = cjsEmploymentType.getEmployer();
        final String description = cjsEmploymentType.getDescription();

        final JurorResponseCjsEmployment existingCjs = cjsRepository.findByJurorNumberAndCjsEmployer(jurorId, key);

        if (null != value) {
            log.debug("Updating CJS employment employer '{}'", key);
            JurorResponseCjsEmployment savedCjs;
            if (null != existingCjs) {
                //found an existing employment
                log.debug("Updating existing CJS employment {}", existingCjs);
                final String oldValue = existingCjs.getCjsEmployerDetails();
                existingCjs.setCjsEmployerDetails(value);
                if (value.compareTo(oldValue) != 0) {
                    // value has changed
                    savedCjs = cjsRepository.save(existingCjs);
                    changeLog.addChangeLogItem(new ChangeLogItem(key, oldValue, key, savedCjs.getCjsEmployerDetails()));
                    log.debug("Saved {}", savedCjs);
                } else {
                    log.trace("No changes for {}", cjsEmploymentType);
                }
            } else {
                // insert new employment
                savedCjs = cjsRepository.save(JurorResponseCjsEmployment.builder()
                    .cjsEmployer(cjsEmploymentType.getEmployer())
                    .cjsEmployerDetails(value)
                    .jurorNumber(jurorId)
                    .build());
                changeLog.addChangeLogItem(new ChangeLogItem(null, null, key, savedCjs.getCjsEmployerDetails()));
            }
        } else {
            if (null != existingCjs) {
                log.debug("Deleting {}", description);
                cjsRepository.delete(existingCjs);
                log.info("Deleted existing {} '{}'", description, key);
                changeLog.addChangeLogItem(new ChangeLogItem(key, existingCjs.getCjsEmployerDetails(), null, null));
            } else {
                log.trace("No {} employer '{}' to update", description, key);
            }
        }
    }

    /**
     * Update a single field with a new value if that value has changed, creating a changelog entry of the event if the
     * value was different.
     *
     * @param fieldName Field name to apply possible change in value
     * @param domain    Domain object to apply changes to
     * @param value     New value for the fieldName in the domain object
     * @param changeLog Change log to add a {@link ChangeLogItem} auditing the changes (if any)
     */
    private void updateAndLog(String fieldName, DigitalResponse domain, Object value, ChangeLog changeLog) {
        final Field field = ReflectionUtils.findField(domain.getClass(), fieldName);
        if (field == null) {
            log.error("Failed to find field '{}' on object {}", fieldName, domain);
            throw new IllegalStateException(
                "Field " + fieldName + " not present in domain class " + domain.getClass().getCanonicalName());
        }
        ReflectionUtils.makeAccessible(field);
        final Object oldValue = ReflectionUtils.getField(field, domain);// old value for change log

        // discern object equality
        boolean equal;
        if (!Objects.isNull(oldValue) && !Objects.isNull(value)
            && oldValue instanceof final LocalDate oldDate && value instanceof final LocalDate newDate) {
            log.trace("Date comparison");
            equal = isSameDate(oldDate, newDate);
        } else {
            log.trace("Object comparison\nOld: {}\nNew: {}", oldValue, value);
            // consider Strings that are either empty or null to be equal!
            equal = (StringUtils.isEmpty(oldValue) && StringUtils.isEmpty(value)) || Objects.equals(oldValue, value);
        }
        // apply changes
        if (!equal) {
            if (log.isTraceEnabled()) {
                log.trace("Change found for {}", fieldName);
            }
            //upsert the field value
            ReflectionUtils.setField(field, domain, value);
            // create a change log entry for the update
            final ChangeLogItem logItem = getReadableChangeLogItem(field.getName(), oldValue, value);
            changeLog.addChangeLogItem(logItem);
            if (log.isDebugEnabled()) {
                log.debug("Change audit: {}", logItem);
            }
        } else {
            log.trace("No change found for {}", fieldName);
        }
    }

    /**
     * Tweak entries in the ChangeLog so that they are more readable to Bureau Officers.
     *
     * @param fieldName Field name to apply change in value
     * @param oldValue  Previous value for the fieldName in the domain object
     * @param newValue  New value for the fieldName in the domain object
     */
    private ChangeLogItem getReadableChangeLogItem(String fieldName, Object oldValue, Object newValue) {
        // we only want to tweak Strings and ignore Dates
        if ((oldValue instanceof LocalDate) || newValue instanceof LocalDate) {
            final DateTimeFormatter dobFormat = DateTimeFormatter.ISO_LOCAL_DATE;
            if (oldValue instanceof LocalDate oldValueLocalDate) {
                oldValue = dobFormat.format(oldValueLocalDate);
            }
            if (newValue instanceof LocalDate newValueLocalDate) {
                newValue = dobFormat.format(newValueLocalDate);
            }
            return new ChangeLogItem(fieldName, oldValue, fieldName, newValue);
        }

        // JDB-2790/JDB-2813 - We want the Change Log to be more readable when juror can attend scheduled hearing date
        if (EXCUSAL_REASON.equals(fieldName)) {
            final String readableValue = "No excusal requested";
            if (StringUtils.isEmpty(oldValue)) {
                oldValue = readableValue;
            } else if (StringUtils.isEmpty(newValue)) {
                newValue = readableValue;
            }
        }

        // JDB-2790/JDB-2813 - We want the Change Log to be more readable when juror can attend scheduled hearing date
        if (DEFERRAL_REASON.equals(fieldName) || DEFERRAL_DATE.equals(fieldName)) {
            final String readableValue = "No deferral requested";
            if (StringUtils.isEmpty(oldValue)) {
                oldValue = readableValue;
            } else if (StringUtils.isEmpty(newValue)) {
                newValue = readableValue;
            }
        }

        return new ChangeLogItem(fieldName, oldValue, fieldName, newValue);
    }

    /**
     * Validate the state of the domain layer actors in the response update scenario.
     *
     * @param jurorId Juror ID for the response being updated
     * @param login   Staff member login performing the update
     * @param domain  Juror response being updated
     * @param staff   Staff entity for the staff member performing the update
     * @throws JurorResponseNotFoundException Response not found in persistence
     * @throws StaffAssignmentException       Staff not found in persistence
     * @throws ResponseAlreadyMergedException Response cannot be updated as it has previously been completed
     */
    private void validateResponseState(final String jurorId, final String login, final DigitalResponse domain,
                                       final User staff) {
        log.debug("Validating response state for {}", jurorId);

        // assert juror response exists
        if (domain == null) {
            log.error("No juror response found for {}", jurorId);
            throw new JurorResponseNotFoundException("Juror response not found");
        }

        // assert juror response has not already been completed (merged back to Juror)
        if (null != domain.getProcessingComplete() && domain.getProcessingComplete()) {
            log.warn("Juror response {} has been completed!", domain.getJurorNumber());
            throw new ResponseAlreadyMergedException();
        }

        // assert staff exists
        if (null == staff) {
            log.error("Could not find Staff {} - invalid state.", login);
            throw new StaffAssignmentException("Staff login '" + login + "' not found!");
        }

        log.debug("Validated response state before updates.");
    }

    /**
     * Apply optimistic locking. Throws a {@link BureauOptimisticLockingException} if the
     * UI supplied version does not match the database version during the transaction. Also force an update to
     * the version field of the parent {@link JurorResponse} via {@link EntityManager#lock(Object, LockModeType)} on the
     * next save.
     *
     * @param domain    Juror response to apply locking to
     * @param uiVersion Version supplied by the UI
     * @implNote Version bump will be evaluated by Hibernate during the next save operation.
     */
    private void applyOptimisticLocking(final DigitalResponse domain, final Integer uiVersion) {
        log.debug("Version: DB={}, UI={}", domain.getVersion(), uiVersion);
        if (domain.getVersion().compareTo(uiVersion) != 0) {
            log.warn("Version does not match!");
            throw new BureauOptimisticLockingException(new ObjectOptimisticLockingFailureException(
                DigitalResponse.class,
                null
            ));
        }
        log.trace("Force incrementing the parent JurorResponse version");
        entityManager.lock(domain, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    /**
     * Save changelog and juror response to the database catching and wrapping a optimistic locking exception.
     *
     * @param changeLog Populated change log
     * @param domain    Updated DETATCHED juror response entity
     */
    private void saveUpdatesOptimistically(final ChangeLog changeLog, final DigitalResponse domain)
        throws BureauOptimisticLockingException {
        try {
            log.debug("Saving updates.");
            changeLogRepository.save(changeLog);
            responseRepository.save(domain);
        } catch (ObjectOptimisticLockingFailureException oolfe) {
            log.info("Failed to update response {}: {}", domain.getJurorNumber(), oolfe.getMessage());
            throw new BureauOptimisticLockingException(oolfe);
        }
    }

    /**
     * Helper method to compare only the date portion of two dates, ignoring the time information.
     *
     * @return Boolean representing whether the dates match, regardless of time
     */
    private boolean isSameDate(LocalDate date1, LocalDate date2) {
        return date1.isEqual(date2);
    }
}
