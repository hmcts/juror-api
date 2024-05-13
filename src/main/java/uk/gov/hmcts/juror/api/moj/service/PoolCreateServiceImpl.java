package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolAddCitizenRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.NilPoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolAdditionalSummonsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolMemberFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.SummonsFormRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CoronerPoolItemDto;
import uk.gov.hmcts.juror.api.moj.controller.response.NilPoolResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestItemDto;
import uk.gov.hmcts.juror.api.moj.controller.response.SummonsFormResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.CoronerPool;
import uk.gov.hmcts.juror.api.moj.domain.CoronerPoolDetail;
import uk.gov.hmcts.juror.api.moj.domain.FilterPoolMember;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.Voters;
import uk.gov.hmcts.juror.api.moj.domain.VotersLocPostcodeTotals;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.PoolCreateException;
import uk.gov.hmcts.juror.api.moj.repository.CoronerPoolDetailRepository;
import uk.gov.hmcts.juror.api.moj.repository.CoronerPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.VotersRepository;
import uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsService;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings({"PMD.TooManyMethods",
    "PMD.PossibleGodClass",
    "PMD.ExcessiveImports",
    "PMD.TooManyFields"})
public class PoolCreateServiceImpl implements PoolCreateService {

    private static final String AGE_DISQ_CODE = "A";

    private static final int LOWER_REQUEST_LIMIT = 30;

    private static final int UPPER_REQUEST_LIMIT = 250;

    @NonNull
    private final PoolRequestRepository poolRequestRepository;
    @NonNull
    private final VotersLocPostcodeTotalsService votersLocPostcodeTotalsService;
    @NonNull
    private final ManageDeferralsService manageDeferralsService;
    @NonNull
    private final JurorPoolRepository jurorPoolRepository;
    @NonNull
    private final JurorRepository jurorRepository;
    @NonNull
    private final VotersRepository votersRepository;
    @NonNull
    private final CourtLocationService courtLocationService;
    @NonNull
    private final PrintDataService printDataService;
    @NonNull
    private final VotersService votersService;
    @NonNull
    private final PoolHistoryRepository poolHistoryRepository;
    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;
    @NonNull
    private final JurorStatusRepository jurorStatusRepository;
    @NonNull
    private final PoolMemberSequenceService poolMemberSequenceService;
    @NonNull
    private final GeneratePoolNumberService generatePoolNumberService;
    @NonNull
    private final PoolTypeRepository poolTypeRepository;
    @NonNull
    private final CourtLocationRepository courtLocationRepository;
    @NonNull
    private final GenerateCoronerPoolNumberService generateCoronerPoolNumberService;
    @NonNull
    private final CoronerPoolDetailRepository coronerPoolDetailRepository;
    @NonNull
    private final CoronerPoolRepository coronerPoolRepository;

    @Override
    public PoolRequestItemDto getPoolRequest(String poolNumber, String owner) {


        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findById(poolNumber);

        if (poolRequestOpt.isEmpty()) {
            log.error("Unable to find a Pool Request record with supplied parameters");
            return null;
        }

        PoolRequest poolRequest = poolRequestOpt.get();

        // find the number of court deferrals used at this stage by querying the Pool members table for members that
        // were deferred
        List<JurorPool> deferredJurors = jurorPoolRepository.findByPoolPoolNumberAndWasDeferredAndIsActive(
            poolRequest.getPoolNumber(), true, true);

        // The court deferrals will have owner not equal to 400
        int deferralsUsed = 0;
        for (JurorPool jurorPool : deferredJurors) {
            if (!SecurityUtil.BUREAU_OWNER.equals(jurorPool.getOwner())) {
                deferralsUsed++;
            }
        }

        PoolRequestItemDto poolRequestItemDto = new PoolRequestItemDto();
        poolRequestItemDto.initPoolRequestItemDto(poolRequest);
        poolRequestItemDto.setCourtSupplied(deferralsUsed);

        return poolRequestItemDto;
    }

    @Override
    public SummonsFormResponseDto summonsForm(SummonsFormRequestDto summonsFormRequestDto) {

        // the number of bureau deferrals from the currently deferred view
        int bureauDeferrals = getBureauDeferrals(summonsFormRequestDto.getCatchmentArea(),
            summonsFormRequestDto.getNextDate());

        // calculate the number required = number requested - number of bureau deferrals
        int noRequired = summonsFormRequestDto.getNoRequested() - bureauDeferrals;

        //retrieve the summary of postcodes and totals within, for non coroners pools
        List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> courtCatchmentSummaryItems =
            votersLocPostcodeTotalsService
                .getCourtCatchmentSummaryItems(summonsFormRequestDto.getCatchmentArea(), false);

        return new SummonsFormResponseDto(bureauDeferrals, noRequired, courtCatchmentSummaryItems);
    }

    /**
     * Retrieve a count of bureau owned deferrals for a given court location and a given "deferred to" date.
     *
     * @param locationCode 3-digit numeric string unique identifier for court locations
     * @param deferredTo   the date the pool is being requested for to check if there are any jurors who have
     *                     deferred to this date
     * @return a count of deferral records matching the predicate criteria
     */
    @Override
    public int getBureauDeferrals(String locationCode, LocalDate deferredTo) {
        return (int) manageDeferralsService.getDeferralsCount(SecurityUtil.BUREAU_OWNER, locationCode, deferredTo);
    }

    /**
     * The create a Pool function needs to do the following (can be broken down into sub functions).
     * - Check that the user has relevant permission to create a pool
     * - verify the yield calculation is within limits - validate the request
     * - Check the lock in the court location table - voters_lock - when creating a pool, there should be only one
     * operation
     * at a time on a particular location
     * - Select the voters from the database using the get_voters function
     * - Mark the selected voters as selected in the Voters table
     * - Add the selected voters into the Pool table
     * - Update the Pool History table with selected voters
     * - Update the Juror History table with selected voters
     * - Update the Bulk Print Data table for summons letters for selected voters
     * - Process deferred jurors - update the Pool History, Juror History, confirm Lett etc
     *
     * @param poolCreateRequestDto Request DT Object from front end
     * @param payload              Authentication payload to indicate user type/level
     */
    @Transactional
    @Override
    public void lockVotersAndCreatePool(BureauJwtPayload payload, PoolCreateRequestDto poolCreateRequestDto) {
        log.info("Processing pool creation for pool number {}", poolCreateRequestDto.getPoolNumber());

        validateCreatePoolRequest(poolCreateRequestDto);
        String locCode = poolCreateRequestDto.getCatchmentArea();

        lockVoters(locCode);

        try {
            createPool(payload, poolCreateRequestDto);
        } finally {
            unlockVoters(locCode);
        }
    }


    private void lockVoters(String locCode) {
        if (courtLocationService.getVotersLock(locCode)) {
            log.info("Voters lock has been obtained for location {}", locCode);
        } else {
            log.info("Failed to obtain voters lock for location {}", locCode);
            throw new PoolCreateException.UnableToObtainVotersLock();
        }
    }

    private void unlockVoters(String locCode) {
        if (courtLocationService.releaseVotersLock(locCode)) {
            log.info("Voters lock has been released for location {}", locCode);
        }
    }


    /**
     * The additional Summons function needs to do the following (can be broken down into sub functions).
     * - Check that the user has relevant permission to summon additional citizens to a pool
     * - verify the yield calculation is within limits - validate the request
     * - Check the lock in the court location table - voters_lock - when creating a pool, there should be only one
     * operation.
     * at a time on a particular location
     * - Select the voters from the database using the get_voters function
     * - Mark the selected voters as selected in the Voters table
     * - Add the selected voters into the Pool table
     * - Update the Pool History table with selected voters - add pool members
     * - Update the Juror History table with selected voters
     * - Update the Bulk Print Data table for summons letters for selected voters
     */
    @Override
    public void lockVotersAndSummonAdditionalCitizens(BureauJwtPayload payload,
                                                      PoolAdditionalSummonsDto poolAdditionalSummonsDto) {

        log.info("Processing additional summons for pool number {}", poolAdditionalSummonsDto.getPoolNumber());

        String locCode = poolAdditionalSummonsDto.getCatchmentArea();

        //validate the yield calculation
        checkYield(locCode, poolAdditionalSummonsDto.getNoRequested(), poolAdditionalSummonsDto.getCitizensToSummon(),
            poolAdditionalSummonsDto.getCitizensSummoned());

        lockVoters(locCode);

        try {
            summonAdditionalCitizens(payload, poolAdditionalSummonsDto);
        } finally {
            unlockVoters(locCode);
        }
    }

    @Transactional
    @Override
    public void createPool(BureauJwtPayload payload, PoolCreateRequestDto poolCreateRequestDto) {

        // Get a list of Pool members from voters table
        List<JurorPool> jurorPools = getJurorPools(payload.getLogin(), payload.getOwner(), poolCreateRequestDto);

        // find the actual number of jurors added and pass to pool history (minus the disq. on selection)
        int numSelected = jurorPools.stream().mapToInt(jurorPool -> Objects.equals(jurorPool.getStatus().getStatus(),
                IJurorStatus.DISQUALIFIED)
                ? 0
                : 1)
            .sum();

        String owner = payload.getOwner();
        String userId = payload.getLogin();
        updatePoolHistory(poolCreateRequestDto.getPoolNumber(), userId, numSelected,
            PoolHistory.NEW_POOL_REQUEST_SUFFIX, HistoryCode.PHSI);

        updateJurorHistory(owner, userId, jurorPools);
        processBureauDeferrals(poolCreateRequestDto, userId, true);
    }

    @Transactional
    public void summonAdditionalCitizens(BureauJwtPayload payload, PoolAdditionalSummonsDto poolAdditionalSummonsDto) {

        //populate the PoolCreateRequestDto object from poolAdditionalSummonsDto
        PoolCreateRequestDto poolCreateRequestDto = setupPoolRequestDto(poolAdditionalSummonsDto);

        // Get a list of Pool members from voters table
        List<JurorPool> jurorPools = getJurorPools(payload.getLogin(), payload.getOwner(), poolCreateRequestDto);
        // find the actual number of jurors added and pass to pool history (minus the disq. on selection)
        int numSelected = jurorPools.stream().mapToInt(member -> Objects.equals(member.getStatus().getStatus(),
            IJurorStatus.DISQUALIFIED
        )
            ? 0
            : 1).sum();

        String owner = payload.getOwner();
        String userId = payload.getLogin();

        updatePoolHistory(poolCreateRequestDto.getPoolNumber(), userId, numSelected,
            PoolHistory.ADD_POOL_MEMBERS_SUFFIX, HistoryCode.PHSI);
        updateJurorHistory(owner, userId, jurorPools);
        processBureauDeferrals(poolCreateRequestDto, userId, false);
    }

    private PoolCreateRequestDto setupPoolRequestDto(PoolAdditionalSummonsDto poolAdditionalSummonsDto) {

        String poolNumber = poolAdditionalSummonsDto.getPoolNumber();
        PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase(poolNumber, poolRequestRepository);

        PoolCreateRequestDto poolCreateRequestDto = new PoolCreateRequestDto();

        poolCreateRequestDto.setPoolNumber(poolAdditionalSummonsDto.getPoolNumber());
        poolCreateRequestDto.setNoRequested(poolAdditionalSummonsDto.getNoRequested());
        poolCreateRequestDto.setCatchmentArea(poolAdditionalSummonsDto.getCatchmentArea());
        poolCreateRequestDto.setPostcodes(poolAdditionalSummonsDto.getPostcodes());
        poolCreateRequestDto.setCitizensToSummon(poolAdditionalSummonsDto.getCitizensToSummon());

        //set values from existing pool request
        poolCreateRequestDto.setStartDate(poolRequest.getReturnDate());
        poolCreateRequestDto.setAttendTime(poolRequest.getAttendTime());

        poolCreateRequestDto.setBureauDeferrals(poolAdditionalSummonsDto.getBureauDeferrals());

        return poolCreateRequestDto;
    }

    private void updatePoolHistory(String poolNumber, String userId, int numSelected,
                                   String suffix, HistoryCode historyCode) {
        log.debug(String.format("Update Pool History table for Pool : %s", poolNumber));
        poolHistoryRepository.save(new PoolHistory(poolNumber, LocalDateTime.now(), historyCode, userId,
            numSelected + suffix));
    }

    private void updatePoolHistory(String poolNumber, String login, String otherInformation) {
        poolHistoryRepository.save(new PoolHistory(poolNumber, LocalDateTime.now(), HistoryCode.PREQ, login,
            otherInformation));
    }

    private void updateJurorHistory(String owner, String userId, List<JurorPool> jurorPools) {

        List<JurorHistory> historyList = new ArrayList<>();
        jurorPools.forEach(jurorPool -> {
            Juror juror = jurorPool.getJuror();
            log.trace(String.format(
                "Update Participant History table for newly summoned juror: %s",
                juror.getJurorNumber()
            ));

            JurorHistory.JurorHistoryBuilder jurorHistBuilder = JurorHistory.builder()
                .jurorNumber(juror.getJurorNumber())
                .poolNumber(jurorPool.getPoolNumber())
                .createdBy(userId);

            // check if pool member is disqualified on selection
            if (Objects.equals(jurorPool.getStatus(), IJurorStatus.DISQUALIFIED)) {
                jurorHistBuilder.historyCode(HistoryCodeMod.DISQUALIFY_POOL_MEMBER);
                jurorHistBuilder.otherInformation(HistoryCodeMod.DISQUALIFY_POOL_MEMBER.getDescription());
            } else {
                jurorHistBuilder.otherInformation(HistoryCodeMod.PRINT_SUMMONS.getDescription());
                jurorHistBuilder.historyCode(HistoryCodeMod.PRINT_SUMMONS);
            }
            historyList.add(jurorHistBuilder.build());
        });
        jurorHistoryRepository.saveAll(historyList);
    }

    private List<JurorPool> getJurorPools(String login, String owner, PoolCreateRequestDto poolCreateRequestDto) {

        List<JurorPool> jurorPools = new ArrayList<>();
        final Date attendanceDate = Date.valueOf(poolCreateRequestDto.getStartDate());
        final String poolNumber = poolCreateRequestDto.getPoolNumber();

        final String locCode = poolCreateRequestDto.getCatchmentArea();
        try {
            // Randomly select a number of voters from Voters table
            Map<String, String> votersMap = votersService.getVoters(owner, poolCreateRequestDto);
            List<String> jurorNumbers = votersMap.keySet().stream().toList();
            final int size = jurorNumbers.size();

            // throw an exception if we couldn't find the required number of voters
            if (size < poolCreateRequestDto.getCitizensToSummon()) {
                throw new RuntimeException();
            }

            int sequenceNumber =
                poolMemberSequenceService.getPoolMemberSequenceNumber(poolCreateRequestDto.getPoolNumber());
            PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase(poolNumber, poolRequestRepository);

            int jurorsFound = 0;
            for (String jurorNumber : jurorNumbers) {
                Voters voter = votersRepository.findByJurorNumber(jurorNumber);
                if (voter.getFlags() == null) {
                    jurorsFound++;
                }
                votersService.markVoterAsSelected(voter, attendanceDate);
                String paddedSequenceNumber = poolMemberSequenceService.leftPadInteger(sequenceNumber);
                JurorPool jurorPool = createJurorPool(login, owner, voter, poolCreateRequestDto,
                    paddedSequenceNumber, poolRequest
                );
                jurorPools.add(jurorPool);

                // Increment the previous sequence number by one to get the new sequence number
                sequenceNumber++;

                if (jurorsFound == poolCreateRequestDto.getCitizensToSummon()) {
                    break;  // we've found the number of jurors required, no need to process any further.
                }
            }

            if (jurorsFound < poolCreateRequestDto.getCitizensToSummon()) {
                throw new RuntimeException(); // we were unable to find the required number of jurors who can serve.
            }

            // Saving records (bulk)
            jurorRepository.saveAll(jurorPools.stream().map(JurorPool::getJuror).toList());
            jurorPoolRepository.saveAll(jurorPools);

            // create a summons letter for juror
            List<JurorPool> summonedJurors = jurorPools.stream()
                .filter(jurorPool -> !Objects.equals(jurorPool.getStatus().getStatus(), IJurorStatus.DISQUALIFIED))
                .toList();

            if (!summonedJurors.isEmpty()) {
                printDataService.bulkPrintSummonsLetter(summonedJurors);
            }
            // increment the pool total by the number of new pool members
            poolRequest.setNewRequest('N');
            poolRequestRepository.save(poolRequest);

        } catch (Exception e) {
            log.error("Exception occurred when adding members to pool - {}", e.getMessage());
            throw new PoolCreateException.UnableToCreatePool();
        } finally {
            //make sure to unlock the voters lock
            unlockVoters(locCode);
        }

        return jurorPools;
    }

    private JurorPool createJurorPool(String login, String owner, Voters voter,
                                      PoolCreateRequestDto poolCreateRequestDto,
                                      String sequenceNumber, PoolRequest poolRequest) {

        if (poolRequest == null) {
            log.error(
                "Could not find a matching Pool request for pool number {}",
                poolCreateRequestDto.getPoolNumber()
            );
            throw new PoolCreateException.UnableToCreatePool();
        }

        Juror juror = new Juror();
        JurorPool jurorPool = new JurorPool();

        LocalDate attendDate = poolCreateRequestDto.getStartDate();

        Optional<JurorStatus> jurorStatusOpt;
        if (voter.getFlags() == null) {
            jurorStatusOpt = jurorStatusRepository.findById(IJurorStatus.SUMMONED);
            jurorPool.setNextDate(attendDate);
        } else {
            // we need to disqualify the juror on selection
            jurorStatusOpt = jurorStatusRepository.findById(IJurorStatus.DISQUALIFIED);
            juror.setSummonsFile(DISQUALIFIED_ON_SELECTION);
            juror.setDisqualifyCode(AGE_DISQ_CODE);
            juror.setDisqualifyDate(LocalDate.now());
            // leave next date as null
        }

        JurorStatus jurorStatus = jurorStatusOpt.orElseThrow(PoolCreateException.InvalidPoolStatus::new);
        jurorPool.setStatus(jurorStatus);

        jurorPool.setOwner(owner);
        jurorPool.setPool(poolRequest);

        juror.setJurorNumber(voter.getJurorNumber());
        juror.setPollNumber(voter.getPollNumber());
        juror.setTitle(voter.getTitle());
        juror.setFirstName(voter.getFirstName());
        juror.setLastName(voter.getLastName());
        juror.setAddressLine1(voter.getAddress());
        juror.setAddressLine2(voter.getAddress2());
        juror.setAddressLine3(voter.getAddress3());
        juror.setAddressLine4(voter.getAddress4());
        juror.setAddressLine5(voter.getAddress5());
        juror.setPostcode(voter.getPostcode());
        juror.setDateOfBirth(voter.getDateOfBirth());
        juror.setResponded(false);
        juror.setContactPreference(null);
        jurorPool.setIsActive(true);

        // pool sequence
        jurorPool.setPoolSequence(sequenceNumber);

        jurorPool.setUserEdtq(login);
        jurorPool.setLastUpdate(LocalDateTime.now());

        jurorPool.setJuror(juror);
        log.info("Pool member {} added to the Pool Member table", juror.getJurorNumber());

        return jurorPool;
    }

    private void validateCreatePoolRequest(PoolCreateRequestDto poolCreateRequestDto) {

        if (poolCreateRequestDto.getNoRequested() <= 0) {
            //invalid no of citizens to summon, raise an exception
            throw new PoolCreateException.InvalidNoOfCitizensToSummon();
        }

        // validate the Yield
        checkYield(
            poolCreateRequestDto.getCatchmentArea(),
            poolCreateRequestDto.getNoRequested(),
            poolCreateRequestDto.getCitizensToSummon()
        );
    }

    private void checkYield(String catchmentArea, int noRequested, int citizensToSummon) {
        checkYield(catchmentArea, noRequested, citizensToSummon, 0);
    }

    private void checkYield(String catchmentArea, int noRequested, int citizensToSummon, int citizensSummoned) {
        // obtain the yield value
        BigDecimal yield = courtLocationService.getYieldForCourtLocation(catchmentArea);
        BigDecimal maxVoters = yield.multiply(BigDecimal.valueOf(noRequested));

        if ((citizensToSummon + citizensSummoned) > maxVoters.intValue()) {
            //summoned too many citizens, raise an exception
            throw new PoolCreateException.InvalidNoOfCitizensToSummonForYield();
        }
    }

    @Override
    public PaginatedList<FilterPoolMember> getJurorPoolsList(BureauJwtPayload payload,
                                                             PoolMemberFilterRequestQuery search) {
        return PaginationUtil.toPaginatedList(
            jurorPoolRepository.fetchFilteredPoolMembers(search, payload.getOwner()),
            search,
            PoolMemberFilterRequestQuery.SortField.JUROR_NUMBER,
            SortMethod.ASC,
            tuple -> {
                FilterPoolMember.FilterPoolMemberBuilder builder = FilterPoolMember.builder()
                    .jurorNumber(tuple.get(QJurorPool.jurorPool.juror.jurorNumber))
                    .firstName(tuple.get(QJurorPool.jurorPool.juror.firstName))
                    .lastName(tuple.get(QJurorPool.jurorPool.juror.lastName))
                    .status(tuple.get(QJurorStatus.jurorStatus.statusDesc));

                if (SecurityUtil.BUREAU_OWNER.equals(payload.getOwner())) {
                    builder.postcode(tuple.get(QJurorPool.jurorPool.juror.postcode));
                } else {
                    builder.attendance(tuple.get(jurorPoolRepository.ATTENDANCE))
                        .checkedInToday(tuple.get(jurorPoolRepository.CHECKED_IN_TODAY))
                        .checkedIn(tuple.get(QAppearance.appearance.timeIn))
                        .nextDate(tuple.get(QJurorPool.jurorPool.nextDate));
                }

                return builder.build();
            },
            500L
        );
    }

    @Override
    public List<String> getThinJurorPoolsList(String poolNumber, String owner) {
        return jurorPoolRepository.fetchThinPoolMembers(poolNumber, owner);
    }

    @Override
    public List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> getAvailableVotersByLocation(String areaCode,
                                                                                                boolean isCoronerPool) {

        Optional<CourtLocation> courtLocation = courtLocationRepository.findByLocCode(areaCode);
        if (courtLocation.isEmpty()) {
            throw new PoolCreateException.CourtLocationNotFound();
        }

        return votersLocPostcodeTotalsService.getCourtCatchmentSummaryItems(areaCode, isCoronerPool);
    }

    private void processBureauDeferrals(PoolCreateRequestDto poolCreateRequestDto, String userId, boolean isNewPool) {
        int bureauDeferrals = poolCreateRequestDto.getBureauDeferrals();
        if (bureauDeferrals > 0) {
            String poolNumber = poolCreateRequestDto.getPoolNumber();
            PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase(poolNumber, poolRequestRepository);
            int deferralsUsed = manageDeferralsService.useBureauDeferrals(poolRequest, bureauDeferrals, userId);
            if (deferralsUsed > 0) {
                updatePoolHistory(poolCreateRequestDto.getPoolNumber(), userId, deferralsUsed,
                    isNewPool
                        ? PoolHistory.NEW_POOL_REQUEST_SUFFIX
                        : PoolHistory.ADD_POOL_MEMBERS_SUFFIX,
                    HistoryCode.PHSI);
            }
        }
    }

    private void processCourtDeferrals(PoolRequest poolRequest, int courtDeferrals, String userId) {
        if (courtDeferrals > 0) {
            int deferralsUsed = manageDeferralsService.useCourtDeferrals(poolRequest, courtDeferrals, userId);
            if (deferralsUsed > 0) {
                updatePoolHistory(poolRequest.getPoolNumber(), userId, deferralsUsed,
                    PoolHistory.ADD_POOL_MEMBERS_SUFFIX, HistoryCode.PHSI);
            }
        }
    }

    @Override
    public NilPoolResponseDto checkForDeferrals(String owner, NilPoolRequestDto nilPoolRequestDto) {

        // validate court location
        CourtLocation location = getLocation(nilPoolRequestDto);
        if (location == null) {
            return null;
        }

        String locationCode = location.getLocCode();
        NilPoolResponseDto nilPoolResponseDto = new NilPoolResponseDto();
        nilPoolResponseDto.setLocationCode(locationCode);
        nilPoolResponseDto.setLocationName(location.getName());
        LocalDate attendanceDate = nilPoolRequestDto.getAttendanceDate();

        long deferralsCount = manageDeferralsService.getDeferralsCount(owner, locationCode, attendanceDate);

        nilPoolResponseDto.setDeferrals((int) deferralsCount);

        if (deferralsCount == 0) {
            String poolNumber = generatePoolNumberService.generatePoolNumber(locationCode, attendanceDate);
            nilPoolResponseDto.setPoolNumber(poolNumber);
            log.debug("No deferrals found for location {} and date {}, new pool number {}", locationCode,
                attendanceDate, poolNumber);
        } else {
            log.debug("{} deferrals found for location {} and date {}", deferralsCount, locationCode, attendanceDate);
        }

        return nilPoolResponseDto;
    }

    private CourtLocation getLocation(NilPoolRequestDto nilPoolRequestDto) {

        String locationCode = nilPoolRequestDto.getLocationCode();
        if (locationCode == null || locationCode.isEmpty()) {

            String locName = nilPoolRequestDto.getLocationName();

            if (locName == null || locName.isEmpty()) {
                //we have a problem, can't determine deferrals with neither court location nor name
                //send back an error response
                log.error("Location code and name are both blank for Nil Pool check for date {}",
                    nilPoolRequestDto.getAttendanceDate());
                return null;
            }
            return courtLocationService.getCourtLocationByName(locName);
        } else {
            return courtLocationService.getCourtLocation(locationCode);
        }

    }

    @Override
    public void createNilPool(String owner, NilPoolRequestDto nilPoolRequestDto) {
        // validate court location
        CourtLocation courtLocation = getLocation(nilPoolRequestDto);
        if (courtLocation == null) {
            throw new PoolCreateException.UnableToCreatePool();
        }

        final String poolNumber = nilPoolRequestDto.getPoolNumber();
        final LocalDate attendanceDate = nilPoolRequestDto.getAttendanceDate();
        final LocalTime attendanceTime = nilPoolRequestDto.getAttendanceTime();
        final String poolTypeStr = nilPoolRequestDto.getPoolType();


        String courtLocationCode = courtLocation.getLocCode();

        log.debug("Creating a Nil Pool for court {}, attendance date {} and pool number {} ", courtLocationCode,
            attendanceDate, poolNumber);

        // Setting the newRequest flag to N as this is not a 'normal' pool
        Character newRequest = 'N';

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setNilPool(true);
        poolRequest.setPoolNumber(poolNumber);
        // immediately transfer ownership to the bureau for positive confirmation
        poolRequest.setOwner(JurorDigitalApplication.JUROR_OWNER);
        poolRequest.setNewRequest(newRequest);
        poolRequest.setReturnDate(attendanceDate);
        poolRequest.setAttendTime(LocalDateTime.of(attendanceDate, attendanceTime));
        poolRequest.setNumberRequested(0); // As this is a nil pool...


        log.debug("Retrieve the Court Location object from the database for: " + courtLocationCode);
        poolRequest.setCourtLocation(courtLocationService.getCourtLocation(courtLocationCode));

        log.debug("Retrieve the Pool Type object from the database for: " + poolTypeStr);
        Optional<PoolType> poolType = poolTypeRepository.findById(poolTypeStr);
        poolRequest.setPoolType(poolType.orElse(null));

        poolRequestRepository.save(poolRequest);
    }

    @Transactional
    @Override
    public void convertNilPool(PoolRequestDto poolRequestDto, BureauJwtPayload payload) {

        final String poolNumber = poolRequestDto.getPoolNumber();
        log.debug("Converting Nil Pool record {}", poolNumber);

        PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase(poolNumber, poolRequestRepository);

        int totalJurorsRequired = updateNilPoolRequest(poolRequestDto, poolRequest);

        // save the total jurors requested for this pool
        poolRequest.setTotalNoRequired(totalJurorsRequired);
        poolRequestRepository.saveAndFlush(poolRequest);

        String otherInformation = String.format("%s (Pool Total Update)", totalJurorsRequired);
        updatePoolHistory(poolNumber, payload.getLogin(), otherInformation);

        processCourtDeferrals(poolRequest, poolRequestDto.getDeferralsUsed(), payload.getLogin());

        log.debug("Finished converting Nil Pool record {}", poolNumber);
    }


    private int updateNilPoolRequest(PoolRequestDto poolRequestDto, PoolRequest poolRequest) {
        Optional<PoolType> poolType = poolTypeRepository.findById(poolRequestDto.getPoolType());
        poolRequest.setPoolType(poolType.orElse(null));

        int deferralsUsed = poolRequestDto.getDeferralsUsed();

        int totalJurorsRequired = poolRequestDto.getNumberRequested();
        int jurorsRequested = totalJurorsRequired - deferralsUsed;

        poolRequest.setNumberRequested(jurorsRequested);
        poolRequest.setNilPool(false);

        poolRequestRepository.saveAndFlush(poolRequest);
        return totalJurorsRequired;
    }

    @Transactional(readOnly = true)
    @Override
    public CoronerPoolItemDto getCoronerPool(String poolNumber) {
        log.debug("Entered get coroner pool function");

        CoronerPool coronerPool = getCoronerPoolRecord(poolNumber);

        CoronerPoolItemDto coronerPoolItemDto = new CoronerPoolItemDto();
        setUpCoronerPoolItemDto(coronerPoolItemDto, coronerPool);

        // Get list of all citizens selected in this pool
        List<CoronerPoolDetail> coronerPoolDetails = coronerPoolDetailRepository.findAllByPoolNumber(poolNumber);

        final int coronerDetailsSize = coronerPoolDetails.size();

        coronerPoolItemDto.setTotalAdded(coronerDetailsSize);
        List<CoronerPoolItemDto.CoronerDetails> coronerPoolDetailList = new ArrayList<>();

        if (coronerDetailsSize > 0) {
            coronerPoolDetails.forEach(detail -> {
                CoronerPoolItemDto.CoronerDetails coronerDetails = CoronerPoolItemDto.CoronerDetails.builder()
                    .jurorNumber(detail.getJurorNumber())
                    .firstName(detail.getFirstName())
                    .title(detail.getTitle())
                    .lastName(detail.getLastName())
                    .addressLineOne(detail.getAddressLine1())
                    .addressLineTwo(detail.getAddressLine2())
                    .addressLineThree(detail.getAddressLine3())
                    .addressLineFour(detail.getAddressLine4())
                    .addressLineFive(detail.getAddressLine5())
                    .postcode(detail.getPostcode())
                    .build();
                coronerPoolDetailList.add(coronerDetails);
            });
        }

        coronerPoolItemDto.setCoronerDetailsList(coronerPoolDetailList);
        log.debug("finished get coroner pool function");
        return coronerPoolItemDto;
    }

    private CoronerPool getCoronerPoolRecord(String poolNumber) {
        Optional<CoronerPool> coronerPoolOpt = coronerPoolRepository.findById(poolNumber);

        if (coronerPoolOpt.isEmpty()) {
            log.debug(String.format("Unable to find a coroner pool with number %s", poolNumber));
            throw new PoolCreateException.CoronerPoolNotFound(poolNumber);
        }

        return coronerPoolOpt.get();
    }

    private void setUpCoronerPoolItemDto(CoronerPoolItemDto coronerPoolItemDto, CoronerPool coronerPool) {

        coronerPoolItemDto.setPoolNumber(coronerPool.getPoolNumber());
        coronerPoolItemDto.setLocCode(coronerPool.getCourtLocation().getLocCode());
        coronerPoolItemDto.setCourtName(coronerPool.getCourtLocation().getName());
        coronerPoolItemDto.setNoRequested(coronerPool.getNumberRequested());
        coronerPoolItemDto.setName(coronerPool.getName());
        coronerPoolItemDto.setDateRequested(coronerPool.getRequestDate());
        coronerPoolItemDto.setEmailAddress(coronerPool.getEmail());
        coronerPoolItemDto.setPhone(coronerPool.getPhoneNumber());
    }

    @Transactional
    @Override
    public String createCoronerPool(String owner, CoronerPoolRequestDto coronerPoolRequestDto) {
        log.debug("Entered create coroner pool function");

        validateCoronerPoolRequest(coronerPoolRequestDto);

        final String newPoolNumber = generateCoronerPoolNumberService.generateCoronerPoolNumber();

        CoronerPool coronerPool = new CoronerPool();

        coronerPool.setPoolNumber(newPoolNumber);
        CourtLocation courtLocation = courtLocationService.getCourtLocation(coronerPoolRequestDto.getLocationCode());
        coronerPool.setCourtLocation(courtLocation);
        coronerPool.setNumberRequested(coronerPoolRequestDto.getNoRequested());
        coronerPool.setRequestDate(coronerPoolRequestDto.getRequestDate());
        coronerPool.setServiceDate(coronerPoolRequestDto.getRequestDate());
        coronerPool.setName(coronerPoolRequestDto.getName());
        coronerPool.setEmail(coronerPoolRequestDto.getEmailAddress());
        coronerPool.setPhoneNumber(coronerPoolRequestDto.getPhone());

        coronerPoolRepository.save(coronerPool);

        log.debug("Finished create coroner pool function");

        return newPoolNumber;
    }

    private void validateCoronerPoolRequest(CoronerPoolRequestDto coronerPoolRequestDto) {
        final int noRequested = coronerPoolRequestDto.getNoRequested();

        if (noRequested < LOWER_REQUEST_LIMIT || noRequested > UPPER_REQUEST_LIMIT) {
            throw new PoolCreateException.InvalidNoOfJurorsRequested(LOWER_REQUEST_LIMIT, UPPER_REQUEST_LIMIT);
        }
    }

    @Transactional
    @Override
    public void addCitizensToCoronerPool(String owner,
                                         CoronerPoolAddCitizenRequestDto coronerPoolAddCitizenRequestDto) {
        log.debug("Entered add citizens to coroner pool function");

        final String poolNumber = coronerPoolAddCitizenRequestDto.getPoolNumber();

        List<CoronerPoolAddCitizenRequestDto.PostCodeAndNumbers> postCodesAndNumbers = getPostCodesAndNumbers(
            coronerPoolAddCitizenRequestDto, poolNumber);

        validateNumberOfCitizensToAdd(poolNumber, postCodesAndNumbers);

        final String locCode = coronerPoolAddCitizenRequestDto.getLocCode();

        postCodesAndNumbers.forEach(item -> {

            try {
                lockVoters(locCode);
                // Randomly select a number of voters from Voters table
                Map<String, String> votersMap = votersService.getVotersForCoronerPool(item.getPostcode(),
                    item.getNumberToAdd(), locCode);
                List<String> jurorNumbers = votersMap.keySet().stream().toList();
                final int size = jurorNumbers.size();
                final int requiredMembers = item.getNumberToAdd();

                // throw an exception if we couldn't find the required number of voters
                if (size < requiredMembers) {
                    throw new RuntimeException();
                }
                for (int index = 0;
                     index < requiredMembers;
                     index++) {
                    Voters voter = votersRepository.findByJurorNumber(jurorNumbers.get(index));

                    votersService.markVoterAsSelected(voter, Date.valueOf(LocalDate.now()));

                    createCoronerJurorPool(poolNumber, voter);
                }
            } catch (Exception e) {
                log.error("Exception occurred when adding members to coroner pool - {}", e.getMessage());
                throw new PoolCreateException.UnableToCreatePool();
            } finally {
                //make sure to unlock the voters lock
                unlockVoters(locCode);
            }
        });

        log.debug("Completed add citizens to coroner pool function");
    }

    private void validateNumberOfCitizensToAdd(
        String poolNumber,
        List<CoronerPoolAddCitizenRequestDto.PostCodeAndNumbers> postCodesAndNumbers) {
        // determine if adding all the values will go over the limit of total allowed
        int sum = postCodesAndNumbers.stream()
            .mapToInt(CoronerPoolAddCitizenRequestDto.PostCodeAndNumbers::getNumberToAdd).sum();
        int currentTotal = coronerPoolDetailRepository.countByPoolNumber(poolNumber);

        if ((sum + currentTotal) > UPPER_REQUEST_LIMIT) {
            log.debug("Excessive number of citizens to be added to Coroner pool {}", poolNumber);
            throw new PoolCreateException.InvalidAddCitizensToCoronersPool(poolNumber);
        }
    }

    private static List<CoronerPoolAddCitizenRequestDto.PostCodeAndNumbers> getPostCodesAndNumbers(
        CoronerPoolAddCitizenRequestDto coronerPoolAddCitizenRequestDto, String poolNumber) {

        List<CoronerPoolAddCitizenRequestDto.PostCodeAndNumbers> postCodeAndNumbersList =
            coronerPoolAddCitizenRequestDto.getPostcodeAndNumbers();

        if (postCodeAndNumbersList.isEmpty()) {
            // invalid map supplied
            log.debug("Invalid Postcodes and numbers map supplied for pool {}", poolNumber);
            throw new PoolCreateException.InvalidAddCitizensToCoronersPool(poolNumber);
        }
        return postCodeAndNumbersList;
    }

    private void createCoronerJurorPool(String poolNumber, Voters voter) {
        CoronerPoolDetail coronerPoolDetail = new CoronerPoolDetail();
        coronerPoolDetail.setPoolNumber(poolNumber);
        coronerPoolDetail.setJurorNumber(voter.getJurorNumber());
        coronerPoolDetail.setTitle(voter.getTitle());
        coronerPoolDetail.setFirstName(voter.getFirstName());
        coronerPoolDetail.setLastName(voter.getLastName());
        coronerPoolDetail.setAddressLine1(voter.getAddress());
        coronerPoolDetail.setAddressLine2(voter.getAddress2());
        coronerPoolDetail.setAddressLine3(voter.getAddress3());
        coronerPoolDetail.setAddressLine4(voter.getAddress4());
        coronerPoolDetail.setAddressLine5(voter.getAddress5());
        coronerPoolDetail.setPostcode(voter.getPostcode());

        coronerPoolDetailRepository.save(coronerPoolDetail);
    }

}
