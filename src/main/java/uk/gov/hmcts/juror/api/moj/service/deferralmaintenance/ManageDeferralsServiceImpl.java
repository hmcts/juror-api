package uk.gov.hmcts.juror.api.moj.service.deferralmaintenance;

import com.querydsl.core.Tuple;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.service.JurorResponseAlreadyCompletedException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralAllocateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralDatesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralReasonRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferredJurorMoveRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance.BulkDisqualifyRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance.ProcessJurorPostponementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.AgeDisqualifiedJurorDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.BulkDisqualifyResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.DeferralAgeDisqualificationResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.CurrentlyDeferred;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCode;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.CurrentlyDeferredException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.PoolRequestException;
import uk.gov.hmcts.juror.api.moj.repository.CurrentlyDeferredRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.AssignOnUpdateServiceMod;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;
import uk.gov.hmcts.juror.api.moj.service.PoolMemberSequenceService;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyMergeService;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.NumberUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.moj.domain.CurrentlyDeferredQueries.filterByCourtAndDate;
import static uk.gov.hmcts.juror.api.moj.utils.NumberUtils.unboxIntegerValues;

@Slf4j
@Service
@SuppressWarnings({"PMD.ExcessiveImports",
    "PMD.PossibleGodClass",
    "PMD.TooManyMethods",
    "PMD.TooManyFields",
    "PMD.CyclomaticComplexity"})
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDeferralsServiceImpl implements ManageDeferralsService {

    private static final String POSTPONE_REASON_CODE = "P";
    private static final String POSTPONE_INFO = "Add Postpone";

    private final CurrentlyDeferredRepository currentlyDeferredRepository;
    private final WelshCourtLocationRepository welshCourtLocationRepository;
    private final JurorRepository jurorRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final PoolRequestRepository poolRequestRepository;
    private final PoolHistoryRepository poolHistoryRepository;
    private final JurorHistoryRepository jurorHistoryRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final PoolMemberSequenceService poolMemberSequenceService;
    private final JurorDigitalResponseRepositoryMod digitalResponseRepository;
    private final JurorPaperResponseRepositoryMod paperResponseRepository;
    private final AssignOnUpdateServiceMod assignOnUpdateService;
    private final SummonsReplyMergeService mergeService;
    private final JurorResponseAuditRepositoryMod jurorResponseAuditRepositoryMod;
    private final JurorHistoryService jurorHistoryService;
    private final PrintDataService printDataService;
    private final JurorPoolService jurorPoolService;
    private final JurorAppearanceService jurorAppearanceService;
    private final JurorResponseService jurorResponseService;

    @Override
    public long getDeferralsCount(String owner, String locationCode, LocalDate deferredTo) {
        return currentlyDeferredRepository.count(filterByCourtAndDate(owner, locationCode, deferredTo));
    }

    @Override
    @Transactional
    public int useCourtDeferrals(PoolRequest newPool, int deferralsRequested, String userId) {
        CourtLocation courtLocation = newPool.getCourtLocation();
        LocalDate attendanceDate = newPool.getReturnDate();

        Iterator<CurrentlyDeferred> courtDeferralsIterator = currentlyDeferredRepository.findAll(
            filterByCourtAndDate(courtLocation.getOwner(), courtLocation.getLocCode(), attendanceDate)).iterator();

        int deferralsUsed = processDeferredJurors(deferralsRequested, courtDeferralsIterator, newPool, userId);
        log.info(String.format("%d deferred juror(s) have been added to Pool: %s", deferralsUsed,
                               newPool.getPoolNumber()
        ));
        return deferralsUsed;
    }

    @Override
    @Transactional
    public DeferralAgeDisqualificationResponseDto processJurorDeferral(BureauJwtPayload payload,
                                                                       String jurorNumber,
                                                                       DeferralReasonRequestDto deferralReasonDto) {
        final String auditorUsername = payload.getLogin();
        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        validateJurorPool(deferralReasonDto.getPoolNumber(), jurorPool);

        // determine service start date for age check - use target pool return date if moving to active pool,
        // otherwise use the requested deferral date
        LocalDate currentServiceStartDate = jurorPool.getReturnDate();
        LocalDate newDate = deferralReasonDto.getDeferralDate();

        if (!StringUtils.isEmpty(deferralReasonDto.getPoolNumber())) {
            Optional<PoolRequest> targetPool = poolRequestRepository.findByPoolNumber(
                deferralReasonDto.getPoolNumber());
            if (targetPool.isPresent()) {
                newDate = targetPool.get().getReturnDate();
            }
        }

        LocalDate dob = JurorUtils.resolveDateOfBirth(
            jurorPool.getJuror(), digitalResponseRepository, paperResponseRepository,
            deferralReasonDto.getReplyMethod());
        if (JurorUtils.isAgeDisqualified(dob, newDate)) {
            return DeferralAgeDisqualificationResponseDto.builder()
                .eligible(0)
                .ageDisqualified(List.of(
                    AgeDisqualifiedJurorDto.builder()
                        .jurorNumber(jurorNumber)
                        .dob(dob)
                        .currentServiceStartDate(currentServiceStartDate)
                        .newDate(newDate)
                        .build()
                ))
                .build();
        }

        // process the response first so any updated juror details are saved
        if (deferralReasonDto.getReplyMethod() != null) {
            updateJurorResponse(jurorNumber, deferralReasonDto, auditorUsername);
        }

        if (!StringUtils.isEmpty(deferralReasonDto.getPoolNumber())) {

            // only check the DOB if there is no reply method as the DOB may not be present yet
            if (deferralReasonDto.getReplyMethod() == null) {
                checkDobPresent(jurorNumber, jurorPool);
            }

            // update old record
            setDeferralPoolMember(jurorPool, deferralReasonDto, auditorUsername, true);
            Optional<PoolRequest> poolRequest = poolRequestRepository.findByPoolNumber(
                deferralReasonDto.getPoolNumber());

            JurorPool newJurorPool;
            if (poolRequest.isPresent()) {
                PoolRequest request = poolRequest.get();
                newJurorPool = addMemberToNewPool(request, jurorPool, auditorUsername,
                          poolMemberSequenceService.getPoolMemberSequenceNumber(poolRequest.get().getPoolNumber()));
            } else {
                throw new MojException.NotFound("Could not find supplied pool number", null);
            }

            removeMemberFromOldPool(jurorPool);

            updateJurorHistory(jurorPool, jurorPool.getPoolNumber(), auditorUsername,
                               JurorHistory.RESPONDED, HistoryCodeMod.RESPONDED_POSITIVELY);
            updateJurorHistory(jurorPool, newJurorPool.getPoolNumber(), auditorUsername,
                               "Add defer - " + jurorPool.getDeferralCode(), HistoryCodeMod.DEFERRED_POOL_MEMBER);
            updateJurorHistory(newJurorPool, newJurorPool.getPoolNumber(), auditorUsername,
                               JurorHistory.ADDED, HistoryCodeMod.DEFERRED_POOL_MEMBER);

            printDeferralLetter(payload.getOwner(), jurorPool);
            printConfirmationLetter(payload.getOwner(), newJurorPool);
        } else {
            // this is for the deferral journey to move them to deferred state
            setupDeferralEntry(deferralReasonDto, auditorUsername, jurorPool);
            printDeferralLetter(payload.getOwner(), jurorPool);
        }

        return DeferralAgeDisqualificationResponseDto.builder()
            .eligible(1)
            .ageDisqualified(List.of())
            .build();
    }

    @Override
    @Transactional
    public DeferralAgeDisqualificationResponseDto changeJurorDeferralDate(BureauJwtPayload payload,
                                                                          String jurorNumber,
                                                                          DeferralReasonRequestDto deferralReasonDto) {
        final String auditorUsername = payload.getLogin();

        log.info("Processing deferral request for juror: {}", jurorNumber);

        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);

        validateJurorPool(deferralReasonDto.getPoolNumber(), jurorPool);

        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        ManageDeferralsService.checkIfJurorHasAttendances(jurorAppearanceService, jurorNumber);

        // determine service start date for age check
        LocalDate currentServiceStartDate = jurorPool.getReturnDate();
        LocalDate newDate = deferralReasonDto.getDeferralDate();

        if (!StringUtils.isEmpty(deferralReasonDto.getPoolNumber())) {
            Optional<PoolRequest> targetPool = poolRequestRepository.findByPoolNumber(
                deferralReasonDto.getPoolNumber());
            if (targetPool.isPresent()) {
                newDate = targetPool.get().getReturnDate();
            }
        }

        LocalDate dob = JurorUtils.resolveDateOfBirth(
            jurorPool.getJuror(), digitalResponseRepository, paperResponseRepository,null);
        if (JurorUtils.isAgeDisqualified(dob, newDate)) {
            return DeferralAgeDisqualificationResponseDto.builder()
                .eligible(0)
                .ageDisqualified(List.of(
                    AgeDisqualifiedJurorDto.builder()
                        .jurorNumber(jurorNumber)
                        .dob(dob)
                        .currentServiceStartDate(currentServiceStartDate)
                        .newDate(newDate)
                        .build()
                ))
                .build();
        }

        if (!StringUtils.isEmpty(deferralReasonDto.getPoolNumber())) {

            checkDobPresent(jurorNumber, jurorPool);

            setDeferralPoolMember(jurorPool, deferralReasonDto, auditorUsername, false);
            Optional<PoolRequest> poolRequest = poolRequestRepository.findByPoolNumber(
                deferralReasonDto.getPoolNumber());

            JurorPool newJurorPool = new JurorPool();
            if (poolRequest.isPresent()) {
                newJurorPool = addMemberToNewPool(
                    poolRequest.get(),
                    jurorPool,
                    auditorUsername,
                    poolMemberSequenceService.getPoolMemberSequenceNumber(poolRequest.get().getPoolNumber())
                );
            }

            removeMemberFromOldPool(jurorPool);

            updateJurorHistory(jurorPool, jurorPool.getPoolNumber(), auditorUsername, JurorHistory.RESPONDED,
                               HistoryCodeMod.RESPONDED_POSITIVELY);
            updateJurorHistory(jurorPool, newJurorPool.getPoolNumber(), auditorUsername,
                               "Add defer - " + jurorPool.getDeferralCode(), HistoryCodeMod.DEFERRED_POOL_MEMBER);
            updateJurorHistory(newJurorPool, newJurorPool.getPoolNumber(), auditorUsername, JurorHistory.ADDED,
                               HistoryCodeMod.DEFERRED_POOL_MEMBER);

            printConfirmationLetter(payload.getOwner(), newJurorPool);
            printDeferralLetter(payload.getOwner(), jurorPool);

        } else {
            // this is for the deferral journey to move them to DEFER_DBF
            setDeferralPoolMember(jurorPool, deferralReasonDto, auditorUsername, false);
            jurorPoolRepository.save(jurorPool);

            if (jurorPool.getCourt() == null || jurorPool.getCourt().getLocCode() == null) {
                throw new MojException.NotFound(
                    String.format("Court location for pool member %s cannot be found",
                                  jurorPool.getJurorNumber()), null);
            }

            updateJurorHistory(jurorPool, jurorPool.getPoolNumber(), auditorUsername, JurorHistory.ADDED,
                               HistoryCodeMod.DEFERRED_POOL_MEMBER);

            printDeferralLetter(payload.getOwner(), jurorPool);
        }

        return DeferralAgeDisqualificationResponseDto.builder()
            .eligible(1)
            .ageDisqualified(List.of())
            .build();
    }

    @Override
    @Transactional
    public DeferralAgeDisqualificationResponseDto allocateJurorsToActivePool(BureauJwtPayload payload,
                                                                             DeferralAllocateRequestDto dto) {
        final String auditorUsername = payload.getLogin();

        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findById(dto.getPoolNumber());
        PoolRequest poolRequest = poolRequestOpt.orElseThrow(() ->
                 new MojException.NotFound(String.format("Cannot find pool request - %s", dto.getPoolNumber()), null));

        final LocalDate serviceStartDate = poolRequest.getReturnDate();

        List<AgeDisqualifiedJurorDto> ageDisqualified = new ArrayList<>();
        int eligibleCount = 0;

        for (String jurorNumber : dto.jurors) {

            log.trace("Juror {} - adding pool member to requested active pool", jurorNumber);
            JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);

            checkDobPresent(jurorNumber, jurorPool);

            JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

            LocalDate dob = JurorUtils.resolveDateOfBirth(
                jurorPool.getJuror(), digitalResponseRepository, paperResponseRepository,null);
            if (JurorUtils.isAgeDisqualified(dob, serviceStartDate)) {
                ageDisqualified.add(
                    AgeDisqualifiedJurorDto.builder()
                        .jurorNumber(jurorNumber)
                        .dob(dob)
                        .currentServiceStartDate(jurorPool.getReturnDate())
                        .newDate(serviceStartDate)
                        .build()
                );
                continue;
            }

            JurorPool newJurorPool = addMemberToNewPool(poolRequest, jurorPool, payload.getLogin(),
                        poolMemberSequenceService.getPoolMemberSequenceNumber(poolRequest.getPoolNumber()));

            log.trace("Juror {} - updating juror history", jurorNumber);
            updateJurorHistory(newJurorPool, newJurorPool.getPoolNumber(), auditorUsername, JurorHistory.ADDED,
                               HistoryCodeMod.DEFERRED_POOL_MEMBER);

            printConfirmationLetter(payload.getOwner(), newJurorPool);
            eligibleCount++;
        }

        return DeferralAgeDisqualificationResponseDto.builder()
            .eligible(eligibleCount)
            .ageDisqualified(ageDisqualified)
            .build();
    }

    @Override
    @Transactional
    public DeferralAgeDisqualificationResponseDto processJurorPostponement(BureauJwtPayload payload,
                                                                           ProcessJurorPostponementRequestDto request) {
        final String auditorUsername = payload.getLogin();
        final String reasonCode = request.getExcusalReasonCode();

        log.info("Processing postponement request for juror(s): {}", request.jurorNumbers);

        request.jurorNumbers.forEach(jurorNumber ->
                 ManageDeferralsService.checkIfJurorHasAttendances(jurorAppearanceService, jurorNumber)
        );

        List<AgeDisqualifiedJurorDto> ageDisqualified = new ArrayList<>();
        int eligibleCount = 0;

        for (String jurorNumber : request.jurorNumbers) {
            JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
            JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

            if (jurorPool.getPoolNumber().equalsIgnoreCase(request.getPoolNumber())) {
                throw new MojException.BadRequest("Cannot postpone to the same pool", null);
            } else if (!POSTPONE_REASON_CODE.equals(reasonCode)) {
                throw new MojException.BadRequest("Invalid reason code for postponement", null);
            }

            // determine service start date for age check - use target pool return date if moving to active pool,
            // otherwise use the requested deferral date
            LocalDate currentServiceStartDate = jurorPool.getReturnDate();
            LocalDate newDate = request.getDeferralDate();

            if (!StringUtils.isEmpty(request.getPoolNumber())) {
                Optional<PoolRequest> targetPool = poolRequestRepository.findByPoolNumber(request.getPoolNumber());
                if (targetPool.isPresent()) {
                    newDate = targetPool.get().getReturnDate();
                }
            }

            LocalDate dob = JurorUtils.resolveDateOfBirth(
                jurorPool.getJuror(), digitalResponseRepository, paperResponseRepository,null);
            if (JurorUtils.isAgeDisqualified(dob, newDate)) {
                ageDisqualified.add(
                    AgeDisqualifiedJurorDto.builder()
                        .jurorNumber(jurorNumber)
                        .dob(dob)
                        .currentServiceStartDate(currentServiceStartDate)
                        .newDate(newDate)
                        .build()
                );
                continue;
            }

            // start the process to postpone and move the juror to the active pool
            if (!StringUtils.isEmpty(request.getPoolNumber())) {

                // checking if DOB is present when postponing into a pool as police check will be made
                checkDobPresent(jurorPool.getJurorNumber(), jurorPool);

                // update old record
                setDeferralPoolMember(jurorPool, request, auditorUsername, true);

                PoolRequest poolRequest =
                    poolRequestRepository.findByPoolNumber(request.getPoolNumber()).orElseThrow(() ->
                            new MojException.NotFound("Could not find supplied pool number", null));

                int sequenceNumber =
                    poolMemberSequenceService.getPoolMemberSequenceNumber(poolRequest.getPoolNumber());

                JurorPool newJurorPool = addPostponedMemberToNewPool(poolRequest, jurorPool, auditorUsername,
                                                                     sequenceNumber);

                removeMemberFromOldPool(jurorPool);

                // update hist for old record - postponed to new pool
                updateJurorHistory(jurorPool, newJurorPool.getPoolNumber(), auditorUsername, POSTPONE_INFO,
                                   HistoryCodeMod.DEFERRED_POOL_MEMBER);

                // update hist for new record - added to new pool
                updateJurorHistory(newJurorPool, newJurorPool.getPoolNumber(), auditorUsername, JurorHistory.ADDED,
                                   HistoryCodeMod.DEFERRED_POOL_MEMBER);

                if (payload.getUserType().equals(UserType.BUREAU)) {
                    printConfirmationLetter(payload.getOwner(), newJurorPool);
                }
            } else {
                // move juror into to DEFER_DBF and update history
                setupDeferralEntry(request, auditorUsername, jurorPool);
            }

            jurorHistoryService.createPostponementLetterHistory(jurorPool, "");

            if (payload.getUserType().equals(UserType.BUREAU)) {
                printPostponementLetter(payload.getOwner(), jurorPool);
            }

            eligibleCount++;
        }

        log.info("Postponement complete. Eligible: {}, Age disqualified: {}",
                 eligibleCount, ageDisqualified.size());

        return DeferralAgeDisqualificationResponseDto.builder()
            .eligible(eligibleCount)
            .ageDisqualified(ageDisqualified)
            .build();
    }

    @Override
    @Transactional
    public DeferralAgeDisqualificationResponseDto moveDeferredJuror(DeferredJurorMoveRequestDto requestDto) {
        log.info("Processing deferred juror move request for juror(s): {} to pool {}", requestDto.getJurorNumbers(),
                 requestDto.getPoolNumber());

        PoolRequest newPool = poolRequestRepository.findByPoolNumber(requestDto.getPoolNumber()).orElseThrow(
            () -> new MojException.NotFound("Could not find supplied pool number", null));

        JurorStatus jurorStatus = jurorStatusRepository.findById(IJurorStatus.REASSIGNED)
            .orElseThrow(() -> new MojException.NotFound("Juror status not found", null));

        final LocalDate serviceStartDate = newPool.getReturnDate();

        List<AgeDisqualifiedJurorDto> ageDisqualified = new ArrayList<>();
        int eligibleCount = 0;

        for (String jurorNumber : requestDto.getJurorNumbers()) {
            JurorPool currentJurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
            JurorPoolUtils.checkOwnershipForCurrentUser(currentJurorPool, SecurityUtil.getActiveOwner());

            // check if juror is in deferred status
            if (currentJurorPool.getStatus().getStatus() != IJurorStatus.DEFERRED) {
                throw new MojException.BadRequest("Juror is not in deferred status", null);
            }

            // check the juror is moving to a different pool
            validateJurorPool(newPool.getPoolNumber(), currentJurorPool);

            LocalDate dob = JurorUtils.resolveDateOfBirth(
                currentJurorPool.getJuror(), digitalResponseRepository, paperResponseRepository,null);
            if (JurorUtils.isAgeDisqualified(dob, serviceStartDate)) {
                ageDisqualified.add(
                    AgeDisqualifiedJurorDto.builder()
                        .jurorNumber(jurorNumber)
                        .dob(dob)
                        .currentServiceStartDate(currentJurorPool.getReturnDate())
                        .newDate(serviceStartDate)
                        .build()
                );
                continue;
            }

            createMovedDeferredJurorPool(jurorNumber, newPool, currentJurorPool);

            // de-activate the current juror pool record
            currentJurorPool.setIsActive(false);
            currentJurorPool.setUserEdtq(SecurityUtil.getActiveLogin());
            currentJurorPool.setStatus(jurorStatus);
            jurorPoolRepository.save(currentJurorPool);

            // add juror history event to old pool member
            jurorHistoryService.createReassignPoolMemberHistory(currentJurorPool, newPool.getPoolNumber(),
                                                                newPool.getCourtLocation());

            eligibleCount++;
        }

        log.info("Deferred juror(s) move complete. Eligible: {}, Age disqualified: {}",
                 eligibleCount, ageDisqualified.size());

        return DeferralAgeDisqualificationResponseDto.builder()
            .eligible(eligibleCount)
            .ageDisqualified(ageDisqualified)
            .build();
    }

    @Override
    @Transactional
    public BulkDisqualifyResponseDto bulkDisqualifyForAge(BureauJwtPayload payload,
                                                          BulkDisqualifyRequestDto requestDto) {
        int disqualifiedCount = 0;
        List<BulkDisqualifyResponseDto.DisqualifiedJurorDto> disqualified = new ArrayList<>();
        List<BulkDisqualifyResponseDto.DisqualifiedJurorDto> failedToDisqualify = new ArrayList<>();

        for (String jurorNumber : requestDto.getJurorNumbers()) {
            try {
                JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
                JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

                final LocalDate dob = JurorUtils.resolveDateOfBirth(
                    jurorPool.getJuror(), digitalResponseRepository, paperResponseRepository, null);
                final LocalDate currentServiceStartDate = jurorPool.getReturnDate();

                jurorResponseService.closeOpenResponseRecord(jurorNumber, payload.getLogin());

                Juror juror = jurorPool.getJuror();
                juror.setResponded(true);
                juror.setDisqualifyDate(LocalDate.now());
                juror.setDisqualifyCode(DisqualifyCode.A.getCode());
                juror.setUserEdtq(payload.getLogin());
                jurorRepository.save(juror);

                JurorStatus disqualifiedStatus = jurorStatusRepository.findById(IJurorStatus.DISQUALIFIED)
                    .orElseThrow(() -> new MojException.NotFound("Juror status not found", null));

                jurorPool.setStatus(disqualifiedStatus);
                jurorPool.setNextDate(null);
                jurorPool.setUserEdtq(payload.getLogin());
                jurorPoolRepository.save(jurorPool);

                jurorHistoryService.createDisqualifyHistory(jurorPool, DisqualifyCode.A.getCode());

                if (JurorDigitalApplication.JUROR_OWNER.equals(jurorPool.getOwner())) {
                    printDataService.printWithdrawalLetter(jurorPool);
                }

                disqualified.add(BulkDisqualifyResponseDto.DisqualifiedJurorDto.builder()
                                     .jurorNumber(jurorNumber)
                                     .dob(dob)
                                     .currentServiceStartDate(currentServiceStartDate)
                                     .newDate(null)
                                     .build());

                disqualifiedCount++;
            } catch (Exception e) {
                log.error("Failed to disqualify juror {} for age: {}", jurorNumber, e.getMessage());

                failedToDisqualify.add(BulkDisqualifyResponseDto.DisqualifiedJurorDto.builder()
                                           .jurorNumber(jurorNumber)
                                           .build());
            }
        }

        log.info("Bulk age disqualification complete. Disqualified: {}, Failed: {}",
                 disqualifiedCount, failedToDisqualify.size());

        return BulkDisqualifyResponseDto.builder()
            .disqualifiedCount(disqualifiedCount)
            .disqualified(disqualified)
            .failedToDisqualify(failedToDisqualify)
            .build();
    }

    @Override
    public DeferralListDto getDeferralsByCourtLocationCode(BureauJwtPayload payload, String courtLocation) {
        List<DeferralListDto.DeferralListDataDto> deferralsList = new ArrayList<>();
        List<Tuple> result = currentlyDeferredRepository.getDeferralsByCourtLocationCode(payload, courtLocation);
        for (Tuple t : result) {
            DeferralListDto.DeferralListDataDto deferral = new DeferralListDto.DeferralListDataDto(
                t.get(0, String.class),
                t.get(1, String.class),
                t.get(2, String.class),
                t.get(3, String.class),
                t.get(4, String.class),
                t.get(5, LocalDate.class)
            );
            deferralsList.add(deferral);
        }
        return new DeferralListDto(deferralsList);
    }

    @Override
    public DeferralOptionsDto findActivePoolsForCourtLocation(BureauJwtPayload payload, String courtLocation) {
        DeferralOptionsDto.OptionSummaryDto poolSummary = new DeferralOptionsDto.OptionSummaryDto();
        LocalDate weekCommencing = DateUtils.getStartOfWeekFromDate(LocalDate.now());
        poolSummary.setWeekCommencing(weekCommencing);

        List<Tuple> activePoolsData = poolRequestRepository.findActivePoolsForDateRange(
            payload.getOwner(),
            courtLocation,
            weekCommencing,
            null,
            false
        );
        List<DeferralOptionsDto.DeferralOptionDto> optionsDtos = new ArrayList<>();
        mapActivePoolStatsToDto(activePoolsData, optionsDtos, payload.getOwner());

        poolSummary.setDeferralOptions(optionsDtos);
        List<DeferralOptionsDto.OptionSummaryDto> optionSummaryDtos = new ArrayList<>();
        optionSummaryDtos.add(poolSummary);

        DeferralOptionsDto dto = new DeferralOptionsDto();
        dto.setDeferralPoolsSummary(optionSummaryDtos);

        return dto;
    }

    @Override
    public DeferralOptionsDto findActivePoolsForDates(DeferralDatesRequestDto deferralDatesRequestDto,
                                                      String jurorNumber,
                                                      BureauJwtPayload payload) {
        log.trace("Juror {}: Enter findActivePoolsForDates", jurorNumber);
        String owner = payload.getOwner();

        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        String currentCourtLocation = jurorPool.getCourt().getLocCode();
        DeferralOptionsDto response = new DeferralOptionsDto();
        log.debug("Juror {}: Find available pools for court location {} to defer into ", jurorNumber,
                  currentCourtLocation);

        List<LocalDate> preferredDates = deferralDatesRequestDto.getDeferralDates();
        response.setDeferralPoolsSummary(populateDeferralOptionsDto(currentCourtLocation, owner, preferredDates));

        log.trace("Juror {}: Deferral Options DTO populated: {}", jurorNumber, response);
        log.trace("Juror {}: Exit findActivePoolsForDates", jurorNumber);
        return response;
    }

    @Override
    public DeferralOptionsDto findActivePoolsForDatesAndLocCode(DeferralDatesRequestDto deferralDatesRequestDto,
                                                                String jurorNumber, String locationCode,
                                                                BureauJwtPayload payload) {
        log.trace("Location Code {}: Enter findActivePoolsForDates", locationCode);

        if (locationCode == null) {
            throw new MojException.BadRequest("Location code not provided", null);
        }

        String owner = payload.getOwner();
        if (!JurorDigitalApplication.JUROR_OWNER.equalsIgnoreCase(owner)
            && !payload.getStaff().getCourts().contains(locationCode)) {
            throw new MojException.Forbidden("User does not have access to this court location", null);
        }

        DeferralOptionsDto response = new DeferralOptionsDto();
        log.debug("Juror {}: Find available pools for court location {} to defer into ", jurorNumber, locationCode);

        List<LocalDate> preferredDates = deferralDatesRequestDto.getDeferralDates();
        response.setDeferralPoolsSummary(populateDeferralOptionsDto(locationCode, owner, preferredDates));

        log.trace("Juror {}: Deferral Options DTO populated: {}", jurorNumber, response);
        log.trace("Juror {}: Exit findActivePoolsForDates", jurorNumber);
        return response;
    }

    @Override
    public List<String> getPreferredDeferralDates(String jurorNumber, BureauJwtPayload payload) {
        log.trace("Juror {}: Enter getPreferredDeferralDates", jurorNumber);

        JurorPoolUtils.checkMultipleRecordReadAccess(jurorPoolRepository, jurorNumber, payload.getOwner());

        DigitalResponse digitalResponse = DataUtils.getJurorDigitalResponse(jurorNumber, digitalResponseRepository);
        String preferredDatesRaw = digitalResponse.getDeferralDate();
        List<String> preferredDates = new ArrayList<>();

        if (StringUtils.isEmpty(preferredDatesRaw)) {
            log.warn("Juror {}: No deferral dates provided on the digital response", jurorNumber);
        } else {
            String delimiter = ", ";
            String[] splitDatesString = preferredDatesRaw.split(delimiter);
            for (String date : splitDatesString) {
                log.debug("Juror {}: Parsing date {} to ISO format", jurorNumber, date);
                try {
                    preferredDates.add(DateUtils.convertLocalisedDateToIso(date));
                } catch (DateTimeParseException ex) {
                    log.error(
                        "Juror {}: Unable to parse preferred deferral date {} to ISO format",
                        jurorNumber,
                        date
                    );
                }
            }
        }

        log.trace("Juror {}: Exit getPreferredDeferralDates", jurorNumber);
        return preferredDates;
    }

    @Override
    public DeferralOptionsDto getAvailablePoolsByCourtLocationCodeAndJurorNumber(BureauJwtPayload payload,
                                                                                 String courtLocationCode,
                                                                                 String jurorNumber) {
        log.trace("Juror {}: Enter getAvailablePoolsByCourtLocationCodeAndJurorNumber", jurorNumber);

        List<String> preferredDeferralDatesAsString = getPreferredDeferralDates(jurorNumber, payload);
        if (preferredDeferralDatesAsString.isEmpty()) {
            throw new MojException.NotFound(String.format("Juror  %s: No deferral dates provided in the response ",
                                                          jurorNumber), null);
        }

        List<LocalDate> preferredDeferralDates = preferredDeferralDatesAsString.stream()
            .map(LocalDate::parse)
            .toList();

        DeferralOptionsDto deferralOptions = new DeferralOptionsDto();
        deferralOptions.setDeferralPoolsSummary(populateDeferralOptionsDto(courtLocationCode,
                                                                           payload.getOwner(),
                                                                           preferredDeferralDates));

        log.trace("Juror {}: Exit getAvailablePoolsByCourtLocationCodeAndJurorNumber", jurorNumber);
        return deferralOptions;
    }

    @Override
    public void deleteDeferral(BureauJwtPayload payload, String jurorNumber) {

        String customErrorMessage = String.format("Cannot find deferred record for juror number %s - ", jurorNumber);

        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        Optional<CurrentlyDeferred> currentlyDeferred = currentlyDeferredRepository.findById(jurorNumber);

        if (currentlyDeferred.isPresent()) {
            setDeletedDeferralJurorPool(jurorPool, payload.getLogin());
        } else {
            throw new MojException.NotFound(customErrorMessage, null);
        }
    }

    @Override
    public int useBureauDeferrals(PoolRequest newPool, int bureauDeferrals, String userId) {
        String owner = newPool.getOwner();
        String courtLocation = newPool.getCourtLocation().getLocCode();
        LocalDate attendanceDate = newPool.getReturnDate();

        Iterator<CurrentlyDeferred> bureauDeferralsIterator = currentlyDeferredRepository
            .findAll(filterByCourtAndDate(owner, courtLocation, attendanceDate)).iterator();

        int deferralsUsed = processBureauDeferredJurors(bureauDeferrals, bureauDeferralsIterator, newPool, userId);
        log.info(String.format("%d deferred juror(s) have been added to Pool: %s", deferralsUsed,
                               newPool.getPoolNumber()
        ));
        return deferralsUsed;
    }

    public void setDeferralPoolMember(JurorPool jurorPool, DeferralReasonRequestDto dto, String auditorUsername,
                                      Boolean incrementNoDefPos) {

        ManageDeferralsService.clearOnCallIfRequired(jurorPool); // clear on_call if set

        jurorPool.setDeferralDate(dto.getDeferralDate());
        jurorPool.setNextDate(null);
        jurorPool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.DEFERRED, jurorStatusRepository));
        jurorPool.setUserEdtq(auditorUsername);
        String reasonCode = dto.getExcusalReasonCode();
        jurorPool.setDeferralCode(reasonCode);

        Juror juror = jurorPool.getJuror();
        juror.setResponded(true);
        juror.setUserEdtq(auditorUsername);

        if (POSTPONE_REASON_CODE.equalsIgnoreCase(reasonCode)) {
            jurorPool.setPostpone(true);
        } else if (Boolean.TRUE.equals(incrementNoDefPos)) {
            if (Objects.isNull(juror.getNoDefPos())) {
                juror.setNoDefPos(1);
            } else {
                juror.setNoDefPos(juror.getNoDefPos() + 1);
            }
        }

        jurorRepository.save(juror);
        jurorPoolRepository.save(jurorPool);
    }

    public void setDeletedDeferralJurorPool(JurorPool jurorPool, String auditorUsername) {

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        jurorPool.setDeferralDate(null);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setUserEdtq(auditorUsername);
        jurorPool.setDeferralCode(null);
        jurorPool.setNextDate(jurorPool.getPool().getReturnDate());

        jurorPoolRepository.save(jurorPool);

        Juror juror = jurorPool.getJuror();
        juror.setExcusalDate(null);
        juror.setNoDefPos(juror.getNoDefPos() - 1);
        juror.setUserEdtq(auditorUsername);

        jurorRepository.save(juror);
    }



    private void validateJurorPool(String poolNumber, JurorPool jurorPool) {
        if (jurorPool.getPoolNumber().equalsIgnoreCase(poolNumber)) {
            throw new MojException.BusinessRuleViolation("Cannot change deferral to the existing pool",
                             MojException.BusinessRuleViolation.ErrorCode.CANNOT_DEFER_TO_EXISTING_POOL);
        }
    }

    private void printDeferralLetter(String owner, JurorPool jurorPool) {
        if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            printDataService.removeQueuedLetterForJuror(jurorPool, List.of(FormCode.ENG_DEFERRAL,
                                                                           FormCode.BI_DEFERRAL));
            printDataService.printDeferralLetter(jurorPool);
            jurorHistoryService.createDeferredLetterHistory(jurorPool);
        }
    }

    private void printConfirmationLetter(String owner, JurorPool jurorPool) {
        if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            printDataService.removeQueuedLetterForJuror(jurorPool, List.of(FormCode.ENG_CONFIRMATION,
                                                                           FormCode.BI_CONFIRMATION));
            Juror juror = jurorPool.getJuror();
            if (juror.getPoliceCheck() != null && juror.getPoliceCheck().isChecked()) {
                printDataService.printConfirmationLetter(jurorPool);
                jurorHistoryService.createConfirmationLetterHistory(jurorPool, "Confirmation Letter");
            }
        }
    }

    private void printPostponementLetter(String owner, JurorPool jurorPool) {
        if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            printDataService.printPostponeLetter(jurorPool);
            jurorHistoryService.createPostponementLetterHistory(jurorPool, "Postponed Letter");
        }
    }

    private void setupDeferralEntry(DeferralReasonRequestDto deferralReasonDto, String auditorUsername,
                                    JurorPool jurorPool) {
        setDeferralPoolMember(jurorPool, deferralReasonDto, auditorUsername, true);
        jurorPoolRepository.save(jurorPool);

        if (jurorPool.getCourt() == null || jurorPool.getCourt().getLocCode() == null) {
            throw new MojException.NotFound(
                String.format("Court location for pool member %s cannot be found",
                              jurorPool.getJurorNumber()), null);
        }

        String otherInfo = POSTPONE_REASON_CODE.equalsIgnoreCase(deferralReasonDto.getExcusalReasonCode())
            ? POSTPONE_INFO
            : JurorHistory.ADDED;
        updateJurorHistory(jurorPool, jurorPool.getPoolNumber(), auditorUsername, otherInfo,
                           HistoryCodeMod.DEFERRED_POOL_MEMBER);
    }

    private void updateJurorResponse(String jurorNumber, DeferralReasonRequestDto deferralReasonDto,
                                     String auditorUsername) {
        AbstractJurorResponse jurorResponse = null;

        if (deferralReasonDto.getReplyMethod().equals(ReplyMethod.DIGITAL)) {
            jurorResponse = DataUtils.getJurorDigitalResponse(jurorNumber, digitalResponseRepository);
        } else if (deferralReasonDto.getReplyMethod().equals(ReplyMethod.PAPER)) {
            jurorResponse = DataUtils.getJurorPaperResponse(jurorNumber, paperResponseRepository);
        }

        if (BooleanUtils.isTrue(jurorResponse.getProcessingComplete())) {
            final String message = String.format("Response %s has been previously merged", jurorNumber);
            log.error("Response {} has previously been completed at {}", jurorNumber,
                      jurorResponse.getCompletedAt());
            throw new JurorResponseAlreadyCompletedException(message);
        }

        jurorResponse.setProcessingStatus(jurorResponseAuditRepositoryMod, ProcessingStatus.CLOSED);

        if (deferralReasonDto.getReplyMethod() == ReplyMethod.DIGITAL) {
            assert jurorResponse instanceof DigitalResponse;
            DigitalResponse digitalResponse = (DigitalResponse) jurorResponse;
            assignOnUpdateService.assignToCurrentLogin(digitalResponse, auditorUsername);
            mergeService.mergeDigitalResponse(digitalResponse, auditorUsername);
        } else {
            assert jurorResponse instanceof PaperResponse;
            mergeService.mergePaperResponse((PaperResponse) jurorResponse, auditorUsername);
        }
    }

    private int processDeferredJurors(int deferralsRequested, Iterator<CurrentlyDeferred> deferralsIterator,
                                      PoolRequest newPool, String userId) {
        int deferralsUsed = 0;
        CurrentlyDeferred deferralRecord;

        int sequenceNumber = poolMemberSequenceService.getPoolMemberSequenceNumber(newPool.getPoolNumber());

        while (deferralsUsed < deferralsRequested && deferralsIterator.hasNext()) {
            deferralRecord = deferralsIterator.next();
            try {
                final JurorPool deferredJurorPool = getPoolMember(deferralRecord, newPool.getReturnDate());
                final JurorPool newJurorPool = addMemberToNewPool(newPool, deferredJurorPool, userId, sequenceNumber);
                sequenceNumber++;

                removeMemberFromOldPool(deferredJurorPool);
                updateJurorHistory(deferredJurorPool, newPool.getPoolNumber(), userId, JurorHistory.ADDED,
                                   HistoryCodeMod.DEFERRED_POOL_MEMBER);

                printConfirmationLetter(deferralRecord.getOwner(), newJurorPool);

                deferralsUsed++;
                log.trace(String.format("Deferred juror %s has been added to Pool: %s",
                                        deferralRecord.getJurorNumber(), newPool.getPoolNumber()));
            } catch (PoolRequestException.PoolRequestNotFound | CurrentlyDeferredException.DeferredMemberNotFound ex) {
                log.error(String.format("An error occurred trying to add a deferred juror to the new Pool: %s - %s",
                                        newPool.getPoolNumber(), ex.getMessage()));
            }
        }
        return deferralsUsed;
    }

    private int processBureauDeferredJurors(int deferralsRequested,
                                            Iterator<CurrentlyDeferred> bureauDeferralsIterator,
                                            PoolRequest poolRequest, String userId) {
        int deferralsUsed = processDeferredJurors(deferralsRequested, bureauDeferralsIterator, poolRequest, userId);
        updatePoolHistory(poolRequest, deferralsUsed, userId);
        return deferralsUsed;
    }

    private JurorPool getPoolMember(CurrentlyDeferred courtDeferral, LocalDate attendanceDate) {
        Optional<JurorPool> jurorPool =
            jurorPoolRepository.findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(
                courtDeferral.getJurorNumber(), courtDeferral.getOwner(), attendanceDate);

        return jurorPool.orElseThrow(() -> new CurrentlyDeferredException.DeferredMemberNotFound(
            courtDeferral.getJurorNumber()));
    }

    private JurorPool addMemberToNewPool(PoolRequest poolRequest, JurorPool deferredPoolMember,
                                         String userId, int sequenceNumber) {
        log.trace(String.format("Create new Pool Member from deferred juror: %s",
                                deferredPoolMember.getJurorNumber()));
        JurorPool newJurorPool = new JurorPool();
        BeanUtils.copyProperties(deferredPoolMember, newJurorPool, "pool");

        Optional<PoolRequest> managedPoolRequest = poolRequestRepository.findById(poolRequest.getPoolNumber());

        if (managedPoolRequest.isPresent()) {
            setupPoolMemberAttributes(managedPoolRequest.get(), userId, sequenceNumber, newJurorPool);
        } else {
            setupPoolMemberAttributes(poolRequest, userId, sequenceNumber, newJurorPool);
        }

        deferredPoolMember.setIsActive(false);

        jurorPoolRepository.saveAndFlush(newJurorPool);
        poolRequestRepository.save(poolRequest);

        return newJurorPool;
    }

    private JurorPool addPostponedMemberToNewPool(PoolRequest poolRequest, JurorPool deferredPoolMember,
                                                  String userId, int sequenceNumber) {
        log.trace(String.format("Create new Pool Member from postponed juror: %s",
                                deferredPoolMember.getJurorNumber()));

        JurorPool newJurorPool = new JurorPool();
        BeanUtils.copyProperties(deferredPoolMember, newJurorPool, "pool");

        setupPoolMemberAttributes(poolRequest, userId, sequenceNumber, newJurorPool);
        newJurorPool.setPostpone(null);

        jurorPoolRepository.saveAndFlush(newJurorPool);
        poolRequestRepository.save(poolRequest);

        return newJurorPool;
    }

    private void setupPoolMemberAttributes(PoolRequest poolRequest, String userId, int sequenceNumber,
                                           JurorPool newJurorPool) {
        newJurorPool.setPool(poolRequest);
        newJurorPool.setDeferralDate(null);
        newJurorPool.setWasDeferred(true);
        newJurorPool.setIsActive(true);
        newJurorPool.setNextDate(poolRequest.getReturnDate());
        newJurorPool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository));
        newJurorPool.setUserEdtq(userId);
        newJurorPool.setDeferralCode(null);
        newJurorPool.setReminderSent(null);
        newJurorPool.setPoolSequence(poolMemberSequenceService.leftPadInteger(sequenceNumber));

        jurorPoolRepository.save(newJurorPool);

        Juror juror = newJurorPool.getJuror();
        juror.setExcusalDate(null);
        juror.setExcusalRejected(null);
        juror.setDisqualifyDate(null);
        juror.setDisqualifyCode(null);

        jurorRepository.save(juror);
    }

    private void removeMemberFromOldPool(JurorPool deferredPoolMember) {
        log.trace(String.format("Logically delete Juror: %s", deferredPoolMember.getJurorNumber()));

        PoolRequest oldPoolRequest = getPoolRequest(deferredPoolMember.getPoolNumber());
        poolRequestRepository.saveAndFlush(oldPoolRequest);

        deferredPoolMember.setIsActive(false);

        jurorPoolRepository.saveAndFlush(deferredPoolMember);

        log.info(String.format("Deferred Juror: %s is no longer active in Pool: %s",
                               deferredPoolMember.getJurorNumber(), deferredPoolMember.getPoolNumber()));
    }

    private PoolRequest getPoolRequest(String poolNumber) {
        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findByPoolNumber(poolNumber);
        if (poolRequestOpt.isPresent()) {
            return poolRequestOpt.get();
        } else {
            throw new PoolRequestException.PoolRequestNotFound(poolNumber);
        }
    }

    private void updatePoolHistory(PoolRequest newPool, int deferralsUsed, String userId) {
        if (deferralsUsed > 0) {
            poolHistoryRepository.save(new PoolHistory(newPool.getPoolNumber(), LocalDateTime.now(), HistoryCode.PHDI,
                                                       userId, deferralsUsed + PoolHistory.NEW_POOL_REQUEST_SUFFIX));
        }
    }

    private void updateJurorHistory(JurorPool deferredJuror, String poolNumber, String userId, String info,
                                    HistoryCodeMod historyCode) {
        log.trace(String.format("Update Participant History table for deferred juror: %s",
                                deferredJuror.getJurorNumber()));

        JurorHistory jurorHistory = JurorHistory.builder()
            .historyCode(historyCode)
            .jurorNumber(deferredJuror.getJurorNumber())
            .poolNumber(poolNumber)
            .createdBy(userId)
            .otherInformation(info)
            .build();

        jurorHistoryRepository.save(jurorHistory);
    }

    private void createMovedDeferredJurorPool(String jurorNumber, PoolRequest newPool, JurorPool currentJurorPool) {
        log.trace(String.format("Create new Pool Member from deferred juror: %s", jurorNumber));
        JurorPool newJurorPool = new JurorPool();
        BeanUtils.copyProperties(currentJurorPool, newJurorPool, "pool");

        ManageDeferralsService.clearOnCallIfRequired(newJurorPool); // clear on_call copied from old record

        newJurorPool.setPool(newPool);
        newJurorPool.setUserEdtq(SecurityUtil.getActiveLogin());

        int sequenceNumber = poolMemberSequenceService.getPoolMemberSequenceNumber(newPool.getPoolNumber());
        newJurorPool.setPoolSequence(poolMemberSequenceService.leftPadInteger(sequenceNumber));

        jurorPoolRepository.save(newJurorPool);
    }

    private List<DeferralOptionsDto.OptionSummaryDto> populateDeferralOptionsDto(String currentCourtLocation,
                                                                                 String owner,
                                                                                 List<LocalDate> preferredDates) {
        log.debug("Owner: {}, Court Location: {} - Check available active pools for preferred Dates {}", owner,
                  currentCourtLocation, preferredDates);

        List<DeferralOptionsDto.OptionSummaryDto> poolSummaryList = new ArrayList<>();
        final int additionalWorkingDays = 4;

        for (LocalDate preferredDate : preferredDates) {
            LocalDate weekCommencing = DateUtils.getStartOfWeekFromDate(preferredDate);
            LocalDate weekEnding = weekCommencing.plusDays(additionalWorkingDays);

            DeferralOptionsDto.OptionSummaryDto poolSummary = new DeferralOptionsDto.OptionSummaryDto();
            poolSummary.setWeekCommencing(weekCommencing);

            List<Tuple> activePoolsData = poolRequestRepository.findActivePoolsForDateRange(owner,
                                        currentCourtLocation, weekCommencing, weekEnding, false);

            log.debug("Found {} available active pools for preferred date: {}", activePoolsData.size(), preferredDate);
            List<DeferralOptionsDto.DeferralOptionDto> deferralOptions = new ArrayList<>();

            if (activePoolsData.isEmpty()) {
                DeferralOptionsDto.DeferralOptionDto deferralOption = new DeferralOptionsDto.DeferralOptionDto();
                poolSummary.setWeekCommencing(preferredDate);
                deferralOption.setUtilisation(currentlyDeferredRepository.count(filterByCourtAndDate(owner,
                                             currentCourtLocation, preferredDate)));
                deferralOption.setUtilisationDescription(PoolUtilisationDescription.IN_MAINTENANCE);
                deferralOptions.add(deferralOption);
            } else {
                mapActivePoolStatsToDto(activePoolsData, deferralOptions, owner);
            }
            poolSummary.setDeferralOptions(deferralOptions);
            poolSummaryList.add(poolSummary);
        }

        return poolSummaryList;
    }

    private void mapActivePoolStatsToDto(List<Tuple> activePoolsData,
                                         List<DeferralOptionsDto.DeferralOptionDto> deferralOptions,
                                         String owner) {
        log.trace("Enter mapActivePoolStatsToDto");
        for (Tuple activePool : activePoolsData) {
            DeferralOptionsDto.DeferralOptionDto deferralOption = new DeferralOptionsDto.DeferralOptionDto();
            deferralOption.setPoolNumber(activePool.get(0, String.class));
            deferralOption.setServiceStartDate(activePool.get(1, LocalDate.class));

            int confirmedPoolMembers = NumberUtils.unboxIntegerValues(activePool.get(3, Integer.class));

            if (owner.equalsIgnoreCase(JurorDigitalApplication.JUROR_OWNER)) {
                log.debug("Calculate current pool utilisation stats for {}", activePool.get(0, String.class));
                int bureauUtilisation = calculateUtilisation(activePool.get(2, Integer.class), confirmedPoolMembers);
                log.debug("Calculate current pool utilisation calculated as {}", bureauUtilisation);

                deferralOption.setUtilisation(Math.abs(bureauUtilisation));
                deferralOption.setUtilisationDescription(bureauUtilisation < 0
                                                             ? PoolUtilisationDescription.SURPLUS
                                                             : PoolUtilisationDescription.NEEDED);
            } else {
                deferralOption.setUtilisation(confirmedPoolMembers);
                deferralOption.setUtilisationDescription(PoolUtilisationDescription.CONFIRMED);
            }
            deferralOptions.add(deferralOption);
        }
        log.trace("Exit mapActivePoolStatsToDto");
    }

    private int calculateUtilisation(Integer requested, int poolMemberCount) {
        int numberRequested = unboxIntegerValues(requested);
        return numberRequested - poolMemberCount;
    }

    private void checkDobPresent(String jurorNumber, JurorPool jurorPool) {
        if (jurorPool.getJuror().getDateOfBirth() == null) {
            throw new MojException.BusinessRuleViolation("Date of birth is missing for juror number: "
                         + jurorNumber, MojException.BusinessRuleViolation.ErrorCode.JUROR_DATE_OF_BIRTH_REQUIRED);
        }
    }
}
