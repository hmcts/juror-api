package uk.gov.hmcts.juror.api.moj.service.deferralmaintenance;

import com.querydsl.core.Tuple;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.service.JurorResponseAlreadyCompletedException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralAllocateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralDatesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralReasonRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance.ProcessJurorPostponementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.DeferralResponseDto;
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
import uk.gov.hmcts.juror.api.moj.service.AssignOnUpdateServiceMod;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.PoolMemberSequenceService;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyMergeService;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.NumberUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

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

/**
 * Court deferrals are records owned by the individual courts, usually when a potential juror has had to defer
 * in the last week leading up to the trial (when the Pool has been transferred back to the court to manage)
 * A deferred juror will be stored as a record in the DEFER_DBF table with a DEFER_TO date property. When a new Pool is
 * requested for the date a juror has previously deferred to, they will be available to be automatically included in the
 * newly requested Pool (immediately summonsed by the court).
 * <p/>
 * If the court choose not to use all the available deferred jurors for a given date in the newly requested Pool,
 * then the remaining deferrals are subject to additional management via a separate process.
 */
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

    @NonNull
    private final CurrentlyDeferredRepository currentlyDeferredRepository;
    @NonNull
    private final WelshCourtLocationRepository welshCourtLocationRepository;
    @NonNull
    private final JurorRepository jurorRepository;
    @NonNull
    private final JurorPoolRepository jurorPoolRepository;
    @NonNull
    private final PoolRequestRepository poolRequestRepository;
    @NonNull
    private final PoolHistoryRepository poolHistoryRepository;
    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;
    @NonNull
    private final JurorStatusRepository jurorStatusRepository;
    @NonNull
    private final PoolMemberSequenceService poolMemberSequenceService;
    @NotNull
    private final JurorDigitalResponseRepositoryMod digitalResponseRepository;
    @NotNull
    private final JurorPaperResponseRepositoryMod paperResponseRepository;
    @NotNull
    private final AssignOnUpdateServiceMod assignOnUpdateService;
    @NotNull
    private final SummonsReplyMergeService mergeService;
    @NotNull
    private final JurorResponseAuditRepository auditRepository;
    @NonNull
    private final JurorHistoryService jurorHistoryService;
    @NonNull
    private final PrintDataService printDataService;

    /**
     * When Jurors defer their service to a future date, a record gets added to the currently_deferred table.
     * Both Court officers and Bureau officers can process a deferral, creating two types:
     * - Court deferrals, which court officers and bureau officers both have access to
     * - Bureau deferrals, which only bureau officers have access to
     * The two types are deciphered by the Owner value - Bureau deferrals will have an owner value of '400' whereas
     * Court deferrals will have an owner value matching the owner value of their court location.
     *
     * @param owner        3-digit numeric string indicating whether the record is a bureau or a court deferral
     * @param locationCode 3-digit numeric string unique identifier for the court location
     * @param deferredTo   the date the pool is being requested for to check if there are any jurors who have
     *                     deferred to this date
     *
     * @return a count of deferral records matching the predicate criteria
     */
    @Override
    public long getDeferralsCount(String owner, String locationCode, LocalDate deferredTo) {
        return currentlyDeferredRepository.count(filterByCourtAndDate(owner, locationCode, deferredTo));
    }

    /**
     * Use a number of records from the currently deferred view to create new Juror Pool records for a newly
     * requested Pool. When a deferred juror is used in a newly created Pool, the following system processes occur:
     * <p/>
     * <ul>
     *     <li>Logically delete the previous Juror Pool record (set is active to false)</li>
     *     <li>Update the Pool Total of the previous Pool Member's associated Pool Request (reduce by one)</li>
     *     <li>Create a new Juror Pool record (OWNER = Court Location Owner) for the newly requested Pool</li>
     *     <li>Update the Pool Total of the newly requested Pool (increment by one)</li>
     *     <li>Insert a record in the PART_HIST table for each used deferral</li>
     * </ul>
     *
     * @param newPool            a newly requested Pool instance
     * @param deferralsRequested the number of court deferrals requested to be used in a new Pool
     *
     * @return the number of court deferrals actually used
     */
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
    public void processJurorDeferral(BureauJwtPayload payload, String jurorNumber,
                                     DeferralReasonRequestDto deferralReasonDto) {
        String auditorUsername = payload.getLogin();
        JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        // if not empty then we need to move the juror to the active pool
        if (!StringUtils.isEmpty(deferralReasonDto.poolNumber)) {
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
                // cannot process this deferral as the new pool couldn't be found
                throw new MojException.NotFound("Could not find supplied pool number",
                    null);
            }

            removeMemberFromOldPool(jurorPool);

            // update hist (for old and new)
            updateJurorHistory(jurorPool, jurorPool.getPoolNumber(), auditorUsername,
                JurorHistory.RESPONDED, HistoryCodeMod.RESPONDED_POSITIVELY);
            updateJurorHistory(jurorPool, newJurorPool.getPoolNumber(), auditorUsername,
                "Add defer - " + jurorPool.getDeferralCode(), HistoryCodeMod.DEFERRED_POOL_MEMBER);
            updateJurorHistory(newJurorPool, newJurorPool.getPoolNumber(), auditorUsername,
                JurorHistory.ADDED, HistoryCodeMod.DEFERRED_POOL_MEMBER);

            printDeferralAndConfirmationLetters(payload.getOwner(), jurorPool, newJurorPool);
        } else {
            //this is for the deferral journey to move them to deferred state
            setupDeferralEntry(deferralReasonDto, auditorUsername, jurorPool);

            printDeferralLetter(payload.getOwner(), jurorPool);
        }

        if (deferralReasonDto.replyMethod != null) {
            updateJurorResponse(jurorNumber, deferralReasonDto, auditorUsername);
        }
    }

    @Override
    @Transactional
    public void changeJurorDeferralDate(BureauJwtPayload payload, String jurorNumber,
                                        DeferralReasonRequestDto deferralReasonDto) {
        String auditorUsername = payload.getLogin();
        JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        // if not empty then we need to move the juror to the active pool
        if (!StringUtils.isEmpty(deferralReasonDto.poolNumber)) {

            // update old record
            setDeferralPoolMember(jurorPool, deferralReasonDto, auditorUsername, false);
            Optional<PoolRequest> poolRequest = poolRequestRepository.findByPoolNumber(
                deferralReasonDto.getPoolNumber());

            JurorPool newJurorPool = new JurorPool();
            if (poolRequest.isPresent()) {
                PoolRequest request = poolRequest.get();
                newJurorPool = addMemberToNewPool(
                    poolRequest.get(),
                    jurorPool,
                    auditorUsername,
                    poolMemberSequenceService.getPoolMemberSequenceNumber(poolRequest.get().getPoolNumber())
                );
            }

            removeMemberFromOldPool(jurorPool);

            // update hist (for old and new)
            updateJurorHistory(jurorPool, jurorPool.getPoolNumber(), auditorUsername, JurorHistory.RESPONDED,
                HistoryCodeMod.RESPONDED_POSITIVELY);

            updateJurorHistory(jurorPool, newJurorPool.getPoolNumber(), auditorUsername,
                "Add defer - " + jurorPool.getDeferralCode(), HistoryCodeMod.DEFERRED_POOL_MEMBER);

            updateJurorHistory(newJurorPool, newJurorPool.getPoolNumber(), auditorUsername, JurorHistory.ADDED,
                HistoryCodeMod.DEFERRED_POOL_MEMBER);

            printDeferralAndConfirmationLetters(payload.getOwner(), jurorPool, newJurorPool);

        } else {
            //this is for the deferral journey to move them to DEFER_DBF
            setDeferralPoolMember(jurorPool, deferralReasonDto, auditorUsername, false);
            jurorPoolRepository.save(jurorPool);

            if (jurorPool.getCourt() == null || jurorPool.getCourt().getLocCode() == null) {
                throw new MojException.NotFound(
                    String.format("Court location for pool member %s cannot be found", jurorPool.getJurorNumber()),
                    null);
            }

            // this will update the juror history for deferred juror
            updateJurorHistory(jurorPool, jurorPool.getPoolNumber(), auditorUsername, JurorHistory.ADDED,
                HistoryCodeMod.DEFERRED_POOL_MEMBER);

            printDeferralLetter(payload.getOwner(), jurorPool);
        }
    }

    @Override
    @Transactional
    public void allocateJurorsToActivePool(BureauJwtPayload payload, DeferralAllocateRequestDto dto) {
        final String auditorUsername = payload.getLogin();

        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findById(dto.getPoolNumber());
        PoolRequest poolRequest = poolRequestOpt.orElseThrow(() ->
            new MojException.NotFound(String.format("Cannot find pool request - %s", dto.getPoolNumber()),
                null));

        for (String jurorNumber : dto.jurors) {

            // Add deferred member to active pool
            log.trace("Juror {} - adding pool member to requested active pool", jurorNumber);
            JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
            JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

            JurorPool newJurorPool = addMemberToNewPool(poolRequest, jurorPool, payload.getLogin(),
                poolMemberSequenceService.getPoolMemberSequenceNumber(poolRequest.getPoolNumber()));

            // update juror history
            log.trace("Juror {} - updating juror history", jurorNumber);
            updateJurorHistory(newJurorPool, newJurorPool.getPoolNumber(), auditorUsername, JurorHistory.ADDED,
                HistoryCodeMod.DEFERRED_POOL_MEMBER);

            printConfirmationLetter(payload.getOwner(), newJurorPool);
        }
    }

    @Override
    @Transactional
    public DeferralResponseDto processJurorPostponement(BureauJwtPayload payload,
                                                        ProcessJurorPostponementRequestDto request) {
        final String auditorUsername = payload.getLogin();
        final String reasonCode = request.getExcusalReasonCode();

        int countJurorsPostponed = 0;
        for (String jurorNumber : request.jurorNumbers) {
            // validation
            JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
            JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

            if (jurorPool.getPoolNumber().equalsIgnoreCase(request.getPoolNumber())) {
                throw new MojException.BadRequest("Cannot postpone to the same pool",
                    null);
            } else if (!POSTPONE_REASON_CODE.equals(reasonCode)) {
                throw new MojException.BadRequest("Invalid reason code for postponement",
                    null);
            }

            // start the process to postpone and move the juror to the active pool
            if (!StringUtils.isEmpty(request.poolNumber)) {
                // update old record
                setDeferralPoolMember(jurorPool, request, auditorUsername, true);

                PoolRequest poolRequest =
                    poolRequestRepository.findByPoolNumber(request.getPoolNumber()).orElseThrow(() ->
                        new MojException.NotFound("Could not find supplied pool number",
                            null));

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

                // Confirmation needs newJurorPool for attendance dates
                if (payload.getUserType().equals(UserType.BUREAU)) {
                    printConfirmationLetter(payload.getOwner(), newJurorPool);
                }
            } else {
                // move juror into to DEFER_DBF and update history
                setupDeferralEntry(request, auditorUsername, jurorPool);
            }

            updateJurorHistory(jurorPool, jurorPool.getPoolNumber(), auditorUsername, "",
                HistoryCodeMod.POSTPONED_LETTER);

            if (payload.getUserType().equals(UserType.BUREAU)) {
                printPostponementLetter(payload.getOwner(), jurorPool);
            }
            countJurorsPostponed++;
        }

        return DeferralResponseDto.builder().countJurorsPostponed(countJurorsPostponed).build();
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
        LocalDate weekCommencing = DateUtils.getStartOfWeekFromDate(LocalDate.now().plusWeeks(1));
        poolSummary.setWeekCommencing(weekCommencing);

        //MIN Date needed (start of the next working week), max date can be null which should get all of them
        List<Tuple> activePoolsData = poolRequestRepository.findActivePoolsForDateRange(
            payload.getOwner(),
            courtLocation,
            weekCommencing,
            null,
            false
        );
        List<DeferralOptionsDto.DeferralOptionDto> optionsDtos = new ArrayList<>();
        mapActivePoolStatsToDto(activePoolsData, optionsDtos, payload.getOwner());

        // setting up the summary with the populated data
        poolSummary.setDeferralOptions(optionsDtos);
        List<DeferralOptionsDto.OptionSummaryDto> optionSummaryDtos = new ArrayList<>();
        optionSummaryDtos.add(poolSummary);

        DeferralOptionsDto dto = new DeferralOptionsDto();
        dto.setDeferralPoolsSummary(optionSummaryDtos);

        return dto;
    }

    /**
     * When a juror requests to be deferred they should provide preferred dates they wish to defer their jury service to
     * On receiving these dates, we can query for any active pools within the working week of the requested deferral
     * date
     * Deferral dates are expected to always be for a Monday (start of the working week).
     *
     * @param deferralDatesRequestDto request DTO containing a list of requested dates - expect a minimum of 1 and a
     *                                maximum of 3 dates to be supplied. All dates should be a Monday (start of the
     *                                working week) even if it is a bank-holiday
     *
     * @return a list of active pools within 5 working days of the requested deferral date(s)
     */
    @Override
    public DeferralOptionsDto findActivePoolsForDates(DeferralDatesRequestDto deferralDatesRequestDto,
                                                      String jurorNumber,
                                                      BureauJwtPayload payload) {
        log.trace("Juror {}: Enter findActivePoolsForDates", jurorNumber);
        String owner = payload.getOwner();

        JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
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
            throw new MojException.Forbidden("User does not have access to this court location",
                null);
        }

        DeferralOptionsDto response = new DeferralOptionsDto();
        log.debug("Juror {}: Find available pools for court location {} to defer into ", jurorNumber,
            locationCode);

        List<LocalDate> preferredDates = deferralDatesRequestDto.getDeferralDates();
        response.setDeferralPoolsSummary(populateDeferralOptionsDto(locationCode, owner, preferredDates));

        log.trace("Juror {}: Deferral Options DTO populated: {}", jurorNumber, response);
        log.trace("Juror {}: Exit findActivePoolsForDates", jurorNumber);
        return response;
    }

    @Override
    public List<String> getPreferredDeferralDates(String jurorNumber, BureauJwtPayload payload) {
        log.trace("Juror {}: Enter getPreferredDeferralDates", jurorNumber);

        // check if the current user has permission to view the juror record and their preferred deferral dates
        JurorPoolUtils.checkMultipleRecordReadAccess(jurorPoolRepository, jurorNumber, payload.getOwner());

        // Get the preferred deferral dates from the digital summons reply
        DigitalResponse digitalResponse = DataUtils.getJurorDigitalResponse(jurorNumber,
            digitalResponseRepository);
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

        //Get the juror's preferred deferral dates
        List<String> preferredDeferralDatesAsString = getPreferredDeferralDates(jurorNumber, payload);
        if (preferredDeferralDatesAsString.isEmpty()) {
            throw new MojException.NotFound(String.format("Juror  %s: No deferral dates provided in the response ",
                jurorNumber), null);
        }

        List<LocalDate> preferredDeferralDates = preferredDeferralDatesAsString.stream()
            .map(LocalDate::parse)
            .toList();

        //Get the pools for the given juror number and preferred dates
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

        JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        Optional<CurrentlyDeferred> currentlyDeferred = currentlyDeferredRepository.findById(jurorNumber);

        if (currentlyDeferred.isPresent()) {
            setDeletedDeferralJurorPool(jurorPool, payload.getLogin());
        } else {
            throw new MojException.NotFound(customErrorMessage, null);
        }
    }

    /**
     * Use a number of records from the currently_deferred table to create new Pool Member records for a newly created
     * Pool. When a deferred juror is used in a newly created Pool, the following system processes occur:
     * <p/>
     * <ul>
     *     <li>Logically delete the previous Pool Member record (set is active to 'N')</li>
     *     <li>Update the Pool Total of the previous Pool Member's associated Pool Request (reduce by one)</li>
     *     <li>Create a new Bureau owned Pool Member record (OWNER = 400) for the newly requested Pool</li>
     *     <li>Update the Pool Total of the newly requested Pool (increment by one)</li>
     *     <li>Insert a record in the PART_HIST table for each used deferral</li>
     *     <li>Insert a record in the POOL_HIST table to summarise the deferrals used</li>
     *     <li>Insert/Update records in the CONFRIM_LETT table for each used deferral</li>
     * </ul>
     *
     * @param newPool         a Pool Request instance, owned by the Bureau
     * @param bureauDeferrals the number of bureau deferrals requested to be used in this Pool
     * @param userId          the current user's username (for auditing in history tables)
     */
    @Override
    public int useBureauDeferrals(PoolRequest newPool, int bureauDeferrals, String userId) {
        String owner = newPool.getOwner(); //should be 400 - Bureau
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
            // don't want to increment this count for postponement as we can use it to determine if juror had
            // reached limit of 2 deferrals.
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

        jurorPoolRepository.save(jurorPool);

        Juror juror = jurorPool.getJuror();
        juror.setExcusalDate(null);
        juror.setNoDefPos(juror.getNoDefPos() - 1);
        juror.setUserEdtq(auditorUsername);

        jurorRepository.save(juror);
    }

    private void printDeferralAndConfirmationLetters(String owner, JurorPool jurorPool, JurorPool newJurorPool) {
        // send letters via bulk print for Bureau users only
        if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            printDataService.checkLetterInBulkPrint(jurorPool.getJurorNumber(), getLetterCode(newJurorPool),
                LocalDate.now(), false);

            printDataService.printDeferralLetter(jurorPool);
            jurorHistoryService.createDeferredLetterHistory(jurorPool);
            Juror juror = jurorPool.getJuror();
            if (juror.getPoliceCheck() != null && juror.getPoliceCheck().isChecked()) {
                printDataService.printConfirmationLetter(newJurorPool);
                jurorHistoryService.createConfirmationLetterHistory(newJurorPool, "Confirmation Letter");
            }
        }
    }

    private void printDeferralLetter(String owner, JurorPool jurorPool) {
        // send letter via bulk print for Bureau users only
        if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            printDataService.checkLetterInBulkPrint(jurorPool.getJurorNumber(), getLetterCode(jurorPool),
                LocalDate.now(), false);

            printDataService.printDeferralLetter(jurorPool);
            jurorHistoryService.createDeferredLetterHistory(jurorPool);
        }
    }

    private String getLetterCode(JurorPool jurorPool) {
        CourtLocation courtLocation = jurorPool.getCourt();

        return CourtLocationUtils.isWelshCourtLocation(welshCourtLocationRepository,
            courtLocation.getLocCode())
            ? FormCode.BI_DEFERRAL.getCode() : FormCode.ENG_DEFERRAL.getCode();
    }

    private void printConfirmationLetter(String owner, JurorPool jurorPool) {
        if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            // send letter via bulk print for Bureau users only
            Juror juror = jurorPool.getJuror();
            if (juror.getPoliceCheck() != null && juror.getPoliceCheck().isChecked()) {
                printDataService.printConfirmationLetter(jurorPool);
                jurorHistoryService.createConfirmationLetterHistory(jurorPool, "Confirmation Letter");
            }
        }
    }

    private void printPostponementLetter(String owner, JurorPool jurorPool) {
        if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            // Postponement needs the old jurorPool for deferral dates
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
                String.format(
                    "Court location for pool member %s cannot be found",
                    jurorPool.getJurorNumber()
                ), null);
        }

        String otherInfo = POSTPONE_REASON_CODE.equalsIgnoreCase(deferralReasonDto.getExcusalReasonCode())
            ? POSTPONE_INFO
            : JurorHistory.ADDED;
        // this will update the juror history for deferred juror
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

        // check to see whether the response has been completed already
        if (BooleanUtils.isTrue(jurorResponse.getProcessingComplete())) {
            final String message = String.format("Response %s has been previously merged", jurorNumber);
            log.error(
                "Response {} has previously been completed at {}",
                jurorNumber,
                jurorResponse.getCompletedAt()
            );
            throw new JurorResponseAlreadyCompletedException(message);
        }

        // there was code for digital to set up the optimistic locking, different from paper
        // this is not needed as it will be covered with e-tag header

        final ProcessingStatus auditStatus = jurorResponse.getProcessingStatus();

        jurorResponse.setProcessingStatus(ProcessingStatus.CLOSED);

        // assign staff (digital)
        if (deferralReasonDto.getReplyMethod() == ReplyMethod.DIGITAL) {
            assert jurorResponse instanceof DigitalResponse;
            DigitalResponse digitalResponse = (DigitalResponse) jurorResponse;

            assignOnUpdateService.assignToCurrentLogin(digitalResponse, auditorUsername);

            final JurorResponseAudit responseAudit = auditRepository.save(JurorResponseAudit.builder()
                .jurorNumber(jurorResponse.getJurorNumber())
                .login(auditorUsername)
                .oldProcessingStatus(auditStatus)
                .newProcessingStatus(jurorResponse.getProcessingStatus())
                .build());

            log.trace("Audit entry: {}", responseAudit);
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

                if (JurorDigitalApplication.JUROR_OWNER.equals(deferralRecord.getOwner())) {
                    printDataService.printConfirmationLetter(newJurorPool);
                    jurorHistoryService.createConfirmationLetterHistory(newJurorPool, "Confirmation Letter");
                }

                deferralsUsed++;
                log.trace(String.format("Deferred juror %s has been added to Pool: %s", deferralRecord.getJurorNumber(),
                    newPool.getPoolNumber()
                ));
            } catch (PoolRequestException.PoolRequestNotFound | CurrentlyDeferredException.DeferredMemberNotFound ex) {
                log.error(String.format("An error occurred trying to add a deferred juror to the new Pool: %s - %s",
                    newPool.getPoolNumber(), ex.getMessage()
                ));
            }
        }
        return deferralsUsed;
    }

    private int processBureauDeferredJurors(int deferralsRequested, Iterator<CurrentlyDeferred> bureauDeferralsIterator,
                                            PoolRequest poolRequest, String userId) {
        int deferralsUsed = processDeferredJurors(deferralsRequested, bureauDeferralsIterator, poolRequest, userId);
        updatePoolHistory(poolRequest, deferralsUsed, userId);
        return deferralsUsed;
    }

    private JurorPool getPoolMember(CurrentlyDeferred courtDeferral, LocalDate attendanceDate) {
        Optional<JurorPool> jurorPool =
            jurorPoolRepository.findByJurorJurorNumberAndOwnerAndDeferralDate(courtDeferral.getJurorNumber(),
                courtDeferral.getOwner(), attendanceDate);

        return jurorPool.orElseThrow(() -> new CurrentlyDeferredException.DeferredMemberNotFound(
            courtDeferral.getJurorNumber()));
    }

    private JurorPool addMemberToNewPool(PoolRequest poolRequest, JurorPool deferredPoolMember,
                                         String userId, int sequenceNumber) {
        log.trace(String.format("Create new Pool Member from deferred juror: %s", deferredPoolMember.getJurorNumber()));
        JurorPool newJurorPool = new JurorPool();
        BeanUtils.copyProperties(deferredPoolMember, newJurorPool, "pool");

        setupPoolMemberAttributes(poolRequest, userId, sequenceNumber, newJurorPool);

        deferredPoolMember.setIsActive(false);  // deactivate the old record

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
        newJurorPool.setPostpone(null); // reset the postponed flag

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
            deferredPoolMember.getJurorNumber(), deferredPoolMember.getPoolNumber()
        ));
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
                userId, deferralsUsed + PoolHistory.NEW_POOL_REQUEST_SUFFIX
            ));
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

    private List<DeferralOptionsDto.OptionSummaryDto> populateDeferralOptionsDto(String currentCourtLocation,
                                                                                 String owner,
                                                                                 List<LocalDate> preferredDates) {
        log.debug("Owner: {}, Court Location: {} - Check available active pools for preferred Dates {}", owner,
            currentCourtLocation, preferredDates
        );

        List<DeferralOptionsDto.OptionSummaryDto> poolSummaryList = new ArrayList<>();
        final int additionalWorkingDays = 4;

        for (LocalDate preferredDate : preferredDates) {
            LocalDate weekCommencing = DateUtils.getStartOfWeekFromDate(preferredDate);
            LocalDate weekEnding = weekCommencing.plusDays(additionalWorkingDays);

            DeferralOptionsDto.OptionSummaryDto poolSummary = new DeferralOptionsDto.OptionSummaryDto();
            poolSummary.setWeekCommencing(weekCommencing);

            List<Tuple> activePoolsData = poolRequestRepository.findActivePoolsForDateRange(owner,
                currentCourtLocation, weekCommencing, weekEnding, false);

            log.debug("Found {} available active pools for preferred date: {}", activePoolsData.size(),
                preferredDate
            );
            List<DeferralOptionsDto.DeferralOptionDto> deferralOptions = new ArrayList<>();

            if (activePoolsData.isEmpty()) {
                DeferralOptionsDto.DeferralOptionDto deferralOption = new DeferralOptionsDto.DeferralOptionDto();
                deferralOption.setUtilisation(currentlyDeferredRepository.count(filterByCourtAndDate(owner,
                    currentCourtLocation, weekCommencing)));
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
                int bureauUtilisation = calculateUtilisation(activePool.get(2, Integer.class),
                    confirmedPoolMembers);
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
}
