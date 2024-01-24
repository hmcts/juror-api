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
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorCJS;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorCJSRepository;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeed;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeedsRepository;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLog;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLogItem;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLogRepository;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLogType;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PhoneLog;
import uk.gov.hmcts.juror.api.bureau.domain.PhoneLogRepository;
import uk.gov.hmcts.juror.api.bureau.domain.THistoryCode;
import uk.gov.hmcts.juror.api.bureau.domain.TSpecialRepository;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.CJSEmploymentDetailsDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.DeferralExcusalDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.FirstPersonJurorDetailsDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.JurorEligibilityDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.JurorNoteDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.JurorPhoneLogDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.ReasonableAdjustmentsDto;
import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.ThirdPartyJurorDetailsDto;
import static uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeedQueries.byJurorNumberAndCode;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.ADDRESS;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.ADDRESS2;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.ADDRESS3;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.ADDRESS4;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.ADDRESS5;
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
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.SPECIAL_NEEDS_ARRANGEMENTS;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_EMAIL_ADDRESS;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_FIRST_NAME;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_LAST_NAME;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_MAIN_PHONE;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_OTHER_PHONE;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_OTHER_REASON;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.THIRD_PARTY_REASON;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.TITLE;
import static uk.gov.hmcts.juror.api.juror.domain.JurorResponse.adjustTimeOnDate;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseUpdateServiceImpl implements ResponseUpdateService {
    static final String HASH_SALT = "445NlwAglWA78Vh9DKbVwN5vPHsvy2kA";
    public static final String UPDATED_NOTES = "Updated notes";
    private static final String MESSAGE = "User {} applied {} total changes to response {}";
    private static final String OTHER_1 = "Other";
    private final PoolRepository poolRepository;
    private final PartHistRepository partHistRepository;
    private final PhoneLogRepository phoneLogRepository;
    private final JurorResponseRepository responseRepository;
    private final ChangeLogRepository changeLogRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final BureauJurorSpecialNeedsRepository bureauJurorSpecialNeedsRepository;
    private final TSpecialRepository tSpecialRepository;
    private final BureauJurorCJSRepository cjsRepository;
    private final AssignOnUpdateService assignOnUpdateService;
    private final JurorResponseAuditRepository responseAuditRepository;


    @Override
    @Transactional(readOnly = true)
    public JurorNoteDto notesByJurorNumber(final String jurorId) {
        final Pool pool = poolRepository.findByJurorNumber(jurorId);

        if (pool != null) {
            final String notes = pool.getNotes();
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
        final Pool pool = poolRepository.findByJurorNumber(jurorId);

        if (pool != null) {
            if (comparisonHash(pool.getNotes()).compareTo(noteDto.getVersion()) == 0) {
                // hashcode matches, save changes to notes
                pool.setNotes(noteDto.getNotes());
                final Pool updatedPool = poolRepository.save(pool);

                // JDB-2685: if no staff assigned, assign current login
                JurorResponse jurorResponse = responseRepository.findByJurorNumber(jurorId);
                if (null == jurorResponse.getStaff()) {
                    assignOnUpdateService.assignToCurrentLogin(jurorResponse, auditUser);
                }

                // 2. persist response
                if (log.isTraceEnabled()) {
                    log.trace("Persisting staff assignment update for response: {}", jurorResponse);
                }
                responseRepository.save(jurorResponse);

                if (log.isDebugEnabled()) {
                    log.debug("Updated note for juror {}: {}", jurorId, updatedPool.getNotes());
                }

                // audit the change to the notes column
                partHistRepository.save(PartHist.builder()
                    .userId(auditUser)
                    .poolNumber(updatedPool.getPoolNumber())
                    .info(UPDATED_NOTES)
                    .historyCode(THistoryCode.POOL_EDIT)
                    .jurorNumber(updatedPool.getJurorNumber())
                    .build());
            } else {
                log.debug("Note failed hash comparison.");
                if (log.isTraceEnabled()) {
                    log.trace("UI={} DB={}", noteDto.getVersion(), comparisonHash(pool.getNotes()));
                }
                throw new NoteComparisonFailureException();
            }
        } else {
            log.warn("No POOL entry found for PART_NO={}", jurorId);
            throw new NoteNotFoundException("POOL entry not found");
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
        final PhoneLog phoneLog = PhoneLog.builder()
            .jurorNumber(jurorId)
            .notes(phoneLogDto.getNotes())
            .startCall(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()))
            .username(auditUser)
            .phoneCode(DEFAULT_PHONE_CODE)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build();
        if (log.isDebugEnabled()) {
            log.debug("User {} added to juror {} phone log: {}", auditUser, jurorId, phoneLog);
        }
        phoneLogRepository.save(phoneLog);

        // JDB-2685: if no staff assigned, assign current login
        JurorResponse savedResponse = responseRepository.findByJurorNumber(jurorId);
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
        final JurorResponse domain = responseRepository.findByJurorNumber(jurorId);
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
        updateAndLog(ADDRESS, domain, dto.getAddress(), changeLog);
        updateAndLog(ADDRESS2, domain, dto.getAddress2(), changeLog);
        updateAndLog(ADDRESS3, domain, dto.getAddress3(), changeLog);
        updateAndLog(ADDRESS4, domain, dto.getAddress4(), changeLog);
        updateAndLog(ADDRESS5, domain, dto.getAddress5(), changeLog);
        updateAndLog(POSTCODE, domain, dto.getPostcode(), changeLog);
        updateAndLog(DOB, domain, dto.getDobTimestamp(), changeLog);
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
        final JurorResponse domain = responseRepository.findByJurorNumber(jurorId);
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
        updateAndLog(ADDRESS, domain, dto.getAddress(), changeLog);
        updateAndLog(ADDRESS2, domain, dto.getAddress2(), changeLog);
        updateAndLog(ADDRESS3, domain, dto.getAddress3(), changeLog);
        updateAndLog(ADDRESS4, domain, dto.getAddress4(), changeLog);
        updateAndLog(ADDRESS5, domain, dto.getAddress5(), changeLog);
        updateAndLog(POSTCODE, domain, dto.getPostcode(), changeLog);
        updateAndLog(DOB, domain, dto.getDobTimestamp(), changeLog);
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
        final JurorResponse domain = responseRepository.findByJurorNumber(jurorId);
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
        final JurorResponse domain = responseRepository.findByJurorNumber(jurorId);
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
        updateAndLog(SPECIAL_NEEDS_ARRANGEMENTS, domain, dto.getSpecialArrangements(), changeLog);
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
    public void updateCjs(final CJSEmploymentDetailsDto dto, final String jurorId, final String login)
        throws BureauOptimisticLockingException {
        log.debug("CJS employment for juror {} edit", jurorId);
        final JurorResponse domain = responseRepository.findByJurorNumber(jurorId);
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

        List<Long> poolStatus = Arrays.asList(2L, 5L, 6L, 7L);

        log.debug("Start - update response status, when legacy status changed for juror {}", jurorId);

        final JurorResponse domain = responseRepository.findByJurorNumber(jurorId);
        final Pool pool = poolRepository.findByJurorNumber(jurorId);
        JurorResponse savedResponse = null;

        if (jurorId != null) {

            savedResponse = responseRepository.findByJurorNumber(jurorId);
        }

        applyOptimisticLocking(domain, version);

        if (pool != null) {
            //update response if pool status = 2,5, 6 & 7
            if (poolStatus.contains(pool.getStatus())) {
                domain.setProcessingStatus(ProcessingStatus.CLOSED);
                domain.setProcessingComplete(Boolean.TRUE);
            } else if (pool.getStatus().equals(11L)) {

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
                responseAuditRepository.save(JurorResponseAudit.builder()
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
        final JurorResponse domain = responseRepository.findByJurorNumber(jurorId);
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
        Optional<BureauJurorSpecialNeed> optSpNeeds = bureauJurorSpecialNeedsRepository.findOne(
            byJurorNumberAndCode(jurorId, key));
        final BureauJurorSpecialNeed existingSpecNeed = optSpNeeds.isPresent()
            ?
            optSpNeeds.get()
            :
                null;
        if (null != value) {
            log.debug("Updating special need employer '{}'", key);
            BureauJurorSpecialNeed savedSpecialNeed;
            if (null != existingSpecNeed) {
                log.debug("Updating existing special need {}", existingSpecNeed);
                final String oldValue = existingSpecNeed.getDetail();
                existingSpecNeed.setDetail(value);
                savedSpecialNeed = bureauJurorSpecialNeedsRepository.save(existingSpecNeed);
                changeLog.addChangeLogItem(new ChangeLogItem(key, oldValue, key, savedSpecialNeed.getDetail()));
            } else {
                // insert new need

                savedSpecialNeed = bureauJurorSpecialNeedsRepository.save(BureauJurorSpecialNeed.builder()
                    .detail(value)
                    .jurorNumber(jurorId)
                    .specialNeed(tSpecialRepository.findByCode(
                        key))
                    .build());
                changeLog.addChangeLogItem(new ChangeLogItem(null, null, key, savedSpecialNeed.getDetail()));
            }
            log.debug("Saved {}", savedSpecialNeed);
        } else {
            if (null != existingSpecNeed) {
                log.debug("Deleting {}", description);
                bureauJurorSpecialNeedsRepository.delete(existingSpecNeed);
                log.info("Deleted existing {} '{}'", description, key);
                changeLog.addChangeLogItem(new ChangeLogItem(key, existingSpecNeed.getDetail(), null, null));
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

        final BureauJurorCJS existingCjs = cjsRepository.findByJurorNumberAndEmployer(jurorId, key);

        if (null != value) {
            log.debug("Updating CJS employment employer '{}'", key);
            BureauJurorCJS savedCjs;
            if (null != existingCjs) {
                //found an existing employment
                log.debug("Updating existing CJS employment {}", existingCjs);
                final String oldValue = existingCjs.getDetails();
                existingCjs.setDetails(value);
                if (value.compareTo(oldValue) != 0) {
                    // value has changed
                    savedCjs = cjsRepository.save(existingCjs);
                    changeLog.addChangeLogItem(new ChangeLogItem(key, oldValue, key, savedCjs.getDetails()));
                    log.debug("Saved {}", savedCjs);
                } else {
                    log.trace("No changes for {}", cjsEmploymentType);
                }
            } else {
                // insert new employment
                savedCjs = cjsRepository.save(BureauJurorCJS.builder()
                    .employer(cjsEmploymentType.getEmployer())
                    .details(value)
                    .jurorNumber(jurorId)
                    .build());
                changeLog.addChangeLogItem(new ChangeLogItem(null, null, key, savedCjs.getDetails()));
            }
        } else {
            if (null != existingCjs) {
                log.debug("Deleting {}", description);
                cjsRepository.delete(existingCjs);
                log.info("Deleted existing {} '{}'", description, key);
                changeLog.addChangeLogItem(new ChangeLogItem(key, existingCjs.getDetails(), null, null));
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
    private void updateAndLog(String fieldName, JurorResponse domain, Object value, ChangeLog changeLog) {
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
            && oldValue instanceof final Date oldDate && value instanceof final Date newDate) {
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
        if ((oldValue instanceof Date) || newValue instanceof Date) {
            final DateTimeFormatter dobFormat = DateTimeFormatter.ISO_LOCAL_DATE;
            if (oldValue instanceof Date) {
                oldValue = (adjustTimeOnDate((Date) oldValue)).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .format(dobFormat);
            }
            if (newValue instanceof Date) {
                newValue = (adjustTimeOnDate((Date) newValue)).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .format(dobFormat);
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
    private void validateResponseState(final String jurorId, final String login, final JurorResponse domain,
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
    private void applyOptimisticLocking(final JurorResponse domain, final Integer uiVersion) {
        log.debug("Version: DB={}, UI={}", domain.getVersion(), uiVersion);
        if (domain.getVersion().compareTo(uiVersion) != 0) {
            log.warn("Version does not match!");
            throw new BureauOptimisticLockingException(new ObjectOptimisticLockingFailureException(
                JurorResponse.class,
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
    private void saveUpdatesOptimistically(final ChangeLog changeLog, final JurorResponse domain)
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
     * @param date1
     * @param date2
     * @return Boolean representing whether the dates match, regardless of time
     */
    private boolean isSameDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
            && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
