package uk.gov.hmcts.juror.api.moj.service.poolmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorManagementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorManagementResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement.ReassignPoolMembersResultDto;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.GeneratePoolNumberService;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.PoolMemberSequenceService;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings({"PMD.TooManyMethods",
    "PMD.ExcessiveImports",
    "PMD.GodClass"})
public class JurorManagementServiceImpl implements JurorManagementService {

    private final PoolRequestRepository poolRequestRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final GeneratePoolNumberService generatePoolNumberService;
    private final PoolMemberSequenceService poolMemberSequenceService;
    private final ResponseInspector responseInspector;
    private final PrintDataService printDataService;
    private final JurorHistoryService jurorHistoryService;
    private final ReissueLetterService reissueLetterService;
    private final JurorAppearanceService appearanceService;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReassignPoolMembersResultDto reassignJurors(BureauJwtPayload payload,
                                                       JurorManagementRequestDto jurorManagementRequestDto) {
        log.trace("Entered reassignJurors method");

        final String owner = payload.getOwner();

        // validate the request DTO, cannot reassign to the same pool in the same court - bad request.
        validateRequest(jurorManagementRequestDto);

        final CourtLocation sendingCourtLocation =
            RepositoryUtils.unboxOptionalRecord(courtLocationRepository.findByLocCode(
                jurorManagementRequestDto.getSourceCourtLocCode()), jurorManagementRequestDto.getSourceCourtLocCode());

        final CourtLocation receivingCourtLocation =
            RepositoryUtils.unboxOptionalRecord(
                courtLocationRepository.findByLocCode(
                    jurorManagementRequestDto.getReceivingCourtLocCode()),
                jurorManagementRequestDto.getReceivingCourtLocCode()
            );

        final PoolRequest sourcePoolRequest =
            RepositoryUtils.unboxOptionalRecord(
                poolRequestRepository.findByPoolNumber(jurorManagementRequestDto.getSourcePoolNumber()
                ), jurorManagementRequestDto.getSourcePoolNumber());

        PoolRequest targetPoolRequest;
        if (jurorManagementRequestDto.getReceivingPoolNumber() != null) {
            targetPoolRequest =
                RepositoryUtils.unboxOptionalRecord(
                    poolRequestRepository.findByPoolNumber(
                        jurorManagementRequestDto.getReceivingPoolNumber()),
                    jurorManagementRequestDto.getReceivingPoolNumber()
                );
        } else {
            if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
                throw new MojException.BadRequest("Receiving Pool Number is required for Bureau users", null);
            }
            SecurityUtil.validateCourtLocationPermitted(jurorManagementRequestDto.getReceivingCourtLocCode());
            targetPoolRequest = createTargetPoolRequest(jurorManagementRequestDto, sourcePoolRequest,
                receivingCourtLocation);
        }

        final String sourcePoolNumber = sourcePoolRequest.getPoolNumber();
        final String targetPoolNumber = targetPoolRequest.getPoolNumber();

        if (sourcePoolNumber == null || targetPoolNumber == null) {
            throw new MojException.NotFound("Could not find Source or Target Pool request", null);
        }

        if (!sourcePoolRequest.getOwner().equals(owner) || !targetPoolRequest.getOwner().equals(owner)) {
            throw new MojException.BadRequest("Users can only reassign between owned pools", null);
        }

        List<String> jurorNumbersList = jurorManagementRequestDto.getJurorNumbers();

        // ensure pool numbers are in the right format
        JurorUtils.validateJurorNumbers(jurorNumbersList);

        // verify the jurors are in the pool at present and with the right status
        List<JurorPool> sourceJurorPools =
            JurorPoolUtils.getSourceJurorsForPool(jurorManagementRequestDto.getJurorNumbers(), sourcePoolNumber,
                sendingCourtLocation, jurorPoolRepository);

        log.debug("{} Pool Members found for the {} juror numbers provided", sourceJurorPools.size(),
            jurorManagementRequestDto.getJurorNumbers().stream().distinct().count()
        );

        final String currentUser = payload.getLogin();
        int reassignedJurorsCount = 0;
        for (JurorPool sourceJurorPool : sourceJurorPools) {
            String jurorNumber = sourceJurorPool.getJurorNumber();
            log.info("Juror: {} - reassigning from Pool: {} to Pool: {}", jurorNumber, sourcePoolNumber,
                targetPoolNumber);


            try {
                JurorPool targetJurorPool;

                // check if juror has appearances in target pool
                if (appearanceService.hasAttendancesInPool(jurorNumber, targetPoolNumber)) {
                    // if the juror has attendances in the target pool, we need to update the existing record
                    // deleting the record will cause issues with the attendances
                    log.info("Juror {} has attendances in target pool {} for reassignment", jurorNumber,
                             targetPoolNumber);

                    targetJurorPool = updateTargetPool(sourceJurorPool, owner, jurorNumber, targetPoolNumber,
                                                       currentUser);

                } else {

                    // remove the existing record for juror if it is present in target pool number
                    deleteExistingJurorPool(owner, jurorNumber, targetPoolNumber);

                    // copy the pool members data over to a new record in the new court location
                    targetJurorPool = createReassignedJurorPool(
                        sourceJurorPool, receivingCourtLocation,
                        targetPoolRequest,
                        currentUser
                    );
                }

                if (SecurityUtil.isCourt()
                    && !sendingCourtLocation.getLocCode().equals(receivingCourtLocation.getLocCode())) {
                    // set the reassign date on target juror pool only when reassigning to different court location
                    sourceJurorPool.setReassignDate(LocalDate.now());
                }

                updateSourceJurorPool(sourceJurorPool, currentUser);

                // add juror history event to old pool member
                jurorHistoryService.createReassignPoolMemberHistory(sourceJurorPool, targetPoolNumber,
                    receivingCourtLocation);

                // queue a summons confirmation letter only if juror is Bureau owned, has responded and is police
                // checked
                if (SecurityUtil.isBureau()) {
                    if (targetJurorPool.getStatus().getStatus() == IJurorStatus.RESPONDED
                        && targetJurorPool.getJuror().getPoliceCheck().isChecked()) {
                        printDataService.printConfirmationLetter(targetJurorPool);
                    }
                    if (targetJurorPool.getStatus().getStatus() == IJurorStatus.SUMMONED) {
                        reissueLetterService.updatePendingLetters(
                            jurorNumber,
                            Set.of(FormCode.ENG_SUMMONS, FormCode.BI_SUMMONS)
                        );
                    }
                }

                reassignedJurorsCount++;

            } catch (MojException exception) {
                log.error("An error occurred reassigning Pool Member {}", jurorNumber);
            }
        }

        poolRequestRepository.saveAndFlush(targetPoolRequest);

        log.trace("Finished reassignJurors method");

        return new ReassignPoolMembersResultDto(reassignedJurorsCount, targetPoolNumber);
    }

    private JurorPool updateTargetPool(JurorPool sourceJurorPool, String owner, String jurorNumber,
                                       String targetPoolNumber, String currentUser) {
        JurorPool targetJurorPool = jurorPoolRepository
            .findByOwnerAndJurorJurorNumberAndPoolPoolNumber(owner, jurorNumber, targetPoolNumber)
            .orElseThrow(() -> new MojException.InternalServerError(
                "Cannot find jurorPool record for juror " + jurorNumber, null));

        // keep the status of the juror the same
        targetJurorPool.setStatus(sourceJurorPool.getStatus());
        targetJurorPool.setIsActive(true);
        targetJurorPool.setUserEdtq(currentUser);
        jurorPoolRepository.save(targetJurorPool);

        return targetJurorPool;
    }

    private void validateRequest(JurorManagementRequestDto jurorManagementRequestDto) {
        if (jurorManagementRequestDto.getSourcePoolNumber().equals(jurorManagementRequestDto.getReceivingPoolNumber())
            && jurorManagementRequestDto.getSourceCourtLocCode()
            .equals(jurorManagementRequestDto.getReceivingCourtLocCode())) {
            String message = String.format("Pool Number %s, Location Code %s : Cannot reassign pool members into same"
                    + " pool and location", jurorManagementRequestDto.getSourcePoolNumber(),
                jurorManagementRequestDto.getSourceCourtLocCode()
            );
            log.error(message);
            throw new MojException.BadRequest(message, null);
        }
    }

    private JurorPool createReassignedJurorPool(JurorPool sourceJurorPool, CourtLocation receivingCourtLocation,
                                                PoolRequest targetPool, String currentUser) {
        log.trace("Enter createReassignedJurorPool");
        JurorPool newTargetJurorPool = new JurorPool();

        BeanUtils.copyProperties(sourceJurorPool, newTargetJurorPool,
            JurorManagementConstants.POOL_MEMBER_IGNORE_PROPERTIES);

        /* Bureau users can only reassign bureau owned jurors to bureau owned pools (but to any court location).
        Court users can only reassign court owned jurors, to any pool, as long as the pool is requested for a court
        location within their primary group - therefore the target juror_pool owner value will always be the same as the
        source juror_pool for reassignment */
        newTargetJurorPool.setOwner(sourceJurorPool.getOwner());
        newTargetJurorPool.setPool(targetPool);
        newTargetJurorPool.setNextDate(targetPool.getReturnDate());
        newTargetJurorPool.setUserEdtq(currentUser);

        newTargetJurorPool.setStatus(sourceJurorPool.getStatus());
        // keep the status of the juror the same

        // some default values
        Juror newTargetJuror = newTargetJurorPool.getJuror();
        newTargetJuror.setNotifications(0);

        int nextPoolSequenceNumber = poolMemberSequenceService
            .getPoolMemberSequenceNumber(targetPool.getPoolNumber());
        newTargetJurorPool.setPoolSequence(poolMemberSequenceService.leftPadInteger(nextPoolSequenceNumber));

        log.debug("Juror: {} - New Pool Member record created for Court Location: {}. Reassign to Pool: {}",
            sourceJurorPool.getJurorNumber(), receivingCourtLocation.getName(), targetPool.getPoolNumber());

        jurorPoolRepository.save(newTargetJurorPool);
        log.trace("Exit createReassignedJurorPool");

        return newTargetJurorPool;
    }

    private void deleteExistingJurorPool(String owner, String jurorNumber, String poolNumber) {
        log.trace("Enter deleteExistingJurorPool");

        // check if the pool member has a record already in target pool
        Optional<JurorPool> existingJurorPool =
            jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(owner,
                jurorNumber, poolNumber
            );

        existingJurorPool.ifPresent(jurorPoolRepository::delete);

        log.trace("Exit deleteExistingJurorPool");
    }

    private void updateSourceJurorPool(JurorPool sourceJurorPool, String currentUser) {
        log.trace("Enter updateSourceJurorPool");

        sourceJurorPool.setStatus(RepositoryUtils
            .retrieveFromDatabase(IJurorStatus.REASSIGNED, jurorStatusRepository));
        sourceJurorPool.setUserEdtq(currentUser);
        sourceJurorPool.setNextDate(null);
        sourceJurorPool.setIsActive(false);

        log.debug("Juror: {} - Pool Member reassigned from Pool: {} by {}", sourceJurorPool.getJurorNumber(),
            sourceJurorPool.getPoolNumber(), currentUser
        );

        jurorPoolRepository.save(sourceJurorPool);
        log.trace("Exit updateSourceJurorPool");
    }

    private JurorManagementResponseDto mapValidationResultsToDto(List<String> requestedJurorPools, Map<String,
        Triple<String, String, String>> validationFailures) {
        log.trace("Enter mapValidationResultsToDto");
        JurorManagementResponseDto responseDto = new JurorManagementResponseDto();

        if (validationFailures.isEmpty()) {
            responseDto.setAvailableForMove(requestedJurorPools.stream().distinct().toList());
        } else {
            List<JurorManagementResponseDto.ValidationFailure> failureList = new ArrayList<>();

            for (Map.Entry<String, Triple<String, String, String>> entry : validationFailures.entrySet()) {
                JurorManagementResponseDto.ValidationFailure validationFailure =
                    new JurorManagementResponseDto.ValidationFailure();

                validationFailure.setJurorNumber(entry.getKey());
                validationFailure.setFailureReason(entry.getValue().a);
                validationFailure.setFirstName(entry.getValue().b);
                validationFailure.setLastName(entry.getValue().c);

                failureList.add(validationFailure);
            }

            responseDto.setAvailableForMove(requestedJurorPools.stream().distinct().filter(jurorPool ->
                !validationFailures.containsKey(
                    jurorPool)).toList());
            responseDto.setUnavailableForMove(failureList);
        }
        log.trace("Exit mapValidationResultsToDto");
        return responseDto;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int transferPoolMembers(BureauJwtPayload payload, JurorManagementRequestDto requestDto) {
        log.trace("Enter transferPoolMembers");

        String activeUserOwner = payload.getOwner();

        validateTransferRequest(activeUserOwner, requestDto);

        // check the current user has write access to manage the source pool
        PoolRequest sourcePoolRequest = getSourcePoolRequest(requestDto.getSourcePoolNumber());
        CourtLocation sendingCourtLocation = getSendingCourtLocation(requestDto.getSendingCourtLocCode());

        // current user does not need access to the receiving court location
        CourtLocation receivingCourtLocation = RepositoryUtils.unboxOptionalRecord(
            courtLocationRepository.findByLocCode(requestDto.getReceivingCourtLocCode()),
            requestDto.getReceivingCourtLocCode());

        validateCourtLocationPermission(sendingCourtLocation, activeUserOwner, receivingCourtLocation.getOwner());

        // create a new pool in the new court location
        PoolRequest targetPoolRequest = createTargetPoolRequest(requestDto, sourcePoolRequest, receivingCourtLocation);

        String sourcePoolNumber = sourcePoolRequest.getPoolNumber();
        String targetPoolNumber = targetPoolRequest.getPoolNumber();

        List<JurorPool> sourceJurorPools = getSourceJurorPools(requestDto.getJurorNumbers(),
            sourcePoolNumber, sendingCourtLocation);

        log.debug("{} Pool Members found for the {} juror numbers provided", sourceJurorPools.size(),
            requestDto.getJurorNumbers().size());

        int successfulTransferCount = 0;
        String currentUser = payload.getLogin();

        for (JurorPool sourceJurorPool : sourceJurorPools) {
            Juror sourceJuror = sourceJurorPool.getJuror();
            String jurorNumber = sourceJuror.getJurorNumber();
            log.info("Juror: {} - transferring from Pool: {} to Pool: {}", jurorNumber,
                sourcePoolNumber, targetPoolNumber);
            try {
                LocalDate dateOfBirth = sourceJuror.getDateOfBirth();
                if (dateOfBirth == null) {
                    log.info("Juror {} has no date of birth on record so cannot validate", jurorNumber);
                } else {
                    validateJurorAge(jurorNumber, dateOfBirth, requestDto.getServiceStartDate());
                }
                // handle the scenario where a pool member is being transferred back to their original court location
                logicallyDeleteExistingJurorPool(jurorNumber, receivingCourtLocation);

                // copy the pool members data over to a new record in the new court location

                JurorPool oldActiveJurorPool =
                    jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(jurorNumber, true,
                        receivingCourtLocation.getOwner());
                //If the juror is being transferred back to the original
                // ensure old transferred record is no longer active
                if (oldActiveJurorPool != null
                    && oldActiveJurorPool.getStatus().getStatus() == IJurorStatus.TRANSFERRED) {
                    oldActiveJurorPool.setIsActive(false);
                    jurorPoolRepository.save(oldActiveJurorPool);
                }

                JurorPool targetJurorPool = createTransferredJurorPool(sourceJurorPool, receivingCourtLocation,
                    targetPoolRequest, currentUser);

                updateSourceJurorPoolTransfer(sourceJurorPool, currentUser);

                // add history event to juror
                jurorHistoryService.createTransferCourtHistory(sourceJurorPool, targetJurorPool);

                successfulTransferCount++;
            } catch (MojException exception) {
                log.error("An error occurred transferring Pool Member {}", jurorNumber);
            }
        }

        poolRequestRepository.saveAndFlush(targetPoolRequest);

        return successfulTransferCount;
    }

    @Override
    @Transactional
    public JurorManagementResponseDto validatePoolMembers(BureauJwtPayload payload,
                                                          JurorManagementRequestDto requestDto) {
        log.trace("Enter validatePoolMembers");
        int requestedJurorCount = (int) requestDto.getJurorNumbers().stream().distinct().count();
        final Map<String, Triple<String, String, String>> failedTransfers = new ConcurrentHashMap<>();

        CourtLocation sendingCourtLocation = getSendingCourtLocation(requestDto.getSendingCourtLocCode());

        List<JurorPool> sourceJurorPools = jurorPoolRepository
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(requestDto.getJurorNumbers(), true,
                requestDto.getSourcePoolNumber(), sendingCourtLocation);

        List<JurorPool> inactiveJurorPools = jurorPoolRepository
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(requestDto.getJurorNumbers(), false,
                requestDto.getSourcePoolNumber(), sendingCourtLocation);

        log.debug("{} Pool Members found for the {} juror numbers provided", sourceJurorPools.size(),
            requestedJurorCount);

        // active record validation
        if (sourceJurorPools.size() != requestDto.getJurorNumbers().size()) {
            List<String> foundJurorNumbers = sourceJurorPools.stream().map(JurorPool::getJurorNumber).toList();
            requestDto.getJurorNumbers().stream()
                .filter(jurorNumber ->
                    !foundJurorNumbers.contains(jurorNumber))
                .forEach(jurorNumber -> {
                    Optional<JurorPool> inactiveJuror = inactiveJurorPools.stream().filter(
                        jurorPool -> jurorNumber.equals(jurorPool.getJurorNumber())).findFirst();
                    if (inactiveJuror.isPresent()) {
                        failedTransfers.put(jurorNumber, new Triple<>(JurorManagementConstants.NO_ACTIVE_RECORD_MESSAGE,
                            inactiveJuror.get().getJuror().getFirstName(),
                            inactiveJuror.get().getJuror().getLastName()));
                    } else {
                        failedTransfers.put(jurorNumber, new Triple<>(JurorManagementConstants.NO_ACTIVE_RECORD_MESSAGE,
                            "", ""));
                    }
                });
        }

        sourceJurorPools.forEach(jurorPool -> {
            // status validation (acceptable status values are 1 (summoned) or 2 (responded)
            if (jurorPool.getStatus().getStatus() > 2 && !(jurorPool.getStatus().getStatus() == 7
                && requestDto.getDeferralMaintenance() != null && requestDto.getDeferralMaintenance().equals(true))) {
                failedTransfers.put(jurorPool.getJurorNumber(),
                    new Triple<>(String.format(JurorManagementConstants.INVALID_STATUS_MESSAGE, jurorPool.getStatus()
                        .getStatusDesc()), jurorPool.getJuror().getFirstName(), jurorPool.getJuror().getLastName()));
                return;
            }

            // age validation
            Juror juror = jurorPool.getJuror();
            LocalDate dateOfBirth = juror.getDateOfBirth();

            if (dateOfBirth == null) {
                log.info("Juror {} has no date of birth on record so cannot validate", juror.getJurorNumber());
            } else {
                validateJurorAge(jurorPool, dateOfBirth, requestDto.getServiceStartDate(), failedTransfers);
            }
        });

        return mapValidationResultsToDto(requestDto.getJurorNumbers(), failedTransfers);
    }


    private void validateTransferRequest(String owner, JurorManagementRequestDto requestDto) {

        if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            // transferring jurors between courts can only be actioned by the court, never by the Bureau
            throw new MojException.Forbidden("Current user has insufficient permission to "
                + "transfer pool members", null);
        }


        JurorUtils.validateJurorNumbers(requestDto.getJurorNumbers());
    }

    private PoolRequest getSourcePoolRequest(String sourcePoolNumber) {
        return RepositoryUtils.unboxOptionalRecord(
            poolRequestRepository.findById(sourcePoolNumber), sourcePoolNumber);
    }

    private CourtLocation getSendingCourtLocation(String sourceCourtLocCode) {
        log.trace("Enter getPoolRequest");
        return RepositoryUtils.unboxOptionalRecord(courtLocationRepository.findByLocCode(sourceCourtLocCode),
            sourceCourtLocCode);
    }

    private void validateCourtLocationPermission(CourtLocation sourceCourtLocation, String activeUserOwner,
                                                 String targetCourtOwner) {
        log.debug("Check whether the current user owns the court location");
        if (!sourceCourtLocation.getOwner().equalsIgnoreCase(activeUserOwner)) {
            throw new MojException.Forbidden(String.format("Current user does not have permission to transfer from "
                + "Court Location: %s", sourceCourtLocation.getLocCode()), null);
        }

        if (sourceCourtLocation.getOwner().equalsIgnoreCase(targetCourtOwner)) {
            throw new MojException.BadRequest(String.format("Cannot transfer a user within the same primary court "
                + "group (%s). Please use Reassign instead.", sourceCourtLocation.getOwner()), null);
        }

    }

    private List<JurorPool> getSourceJurorPools(List<String> jurorNumbers, String sourcePoolNumber,
                                                CourtLocation sendingCourtLocation) {
        List<Integer> validSourceStatusList = new ArrayList<>();
        validSourceStatusList.add(IJurorStatus.SUMMONED);
        validSourceStatusList.add(IJurorStatus.RESPONDED);

        log.debug("Find summoned/responded jurors from Pool: {} to be transferred", sourcePoolNumber);

        return jurorPoolRepository
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(jurorNumbers, true,
                sourcePoolNumber, sendingCourtLocation, validSourceStatusList);
    }

    private PoolRequest createTargetPoolRequest(JurorManagementRequestDto requestDto, PoolRequest sourcePoolRequest,
                                                CourtLocation receivingCourtLocation) {
        log.trace("Create target pool request for transferring/reassigning pool members to {}",
            receivingCourtLocation.getLocCode());
        PoolRequest targetPoolRequest = new PoolRequest();
        targetPoolRequest.setOwner(receivingCourtLocation.getOwner());
        targetPoolRequest.setPoolNumber(
            generatePoolNumberService.generatePoolNumber(receivingCourtLocation.getLocCode(),
                requestDto.getServiceStartDate()));
        targetPoolRequest.setCourtLocation(receivingCourtLocation);
        // target pool request will not need transferring and filling by the bureau so default NEW_REQUEST to 'N'
        targetPoolRequest.setNewRequest(JurorManagementConstants.DEFAULT_POOL_NEW_REQUEST);
        targetPoolRequest.setReturnDate(requestDto.getServiceStartDate());
        targetPoolRequest.setPoolType(sourcePoolRequest.getPoolType());
        // explicitly set number requested to null (not 0) so we can differentiate between these pools and nil pools
        targetPoolRequest.setNumberRequested(null);
        log.trace("Pool: {} created to receive transferred pool members", targetPoolRequest.getPoolNumber());
        return poolRequestRepository.save(targetPoolRequest);
    }

    private void validateJurorAge(String jurorNumber, LocalDate dateOfBirth, LocalDate serviceStartDate) {
        int age = JurorUtils.getJurorAgeAtHearingDate(dateOfBirth, serviceStartDate);

        int lowerAgeLimit = responseInspector.getYoungestJurorAgeAllowed();
        int upperAgeLimit = responseInspector.getTooOldJurorAge();

        if (age < lowerAgeLimit || age >= upperAgeLimit) {
            throw new MojException.BadRequest(String.format("Juror %s does not satisfy the age criteria for "
                + "the proposed service start date", jurorNumber), null);
        }
    }

    private void validateJurorAge(JurorPool jurorPool, LocalDate dateOfBirth, LocalDate serviceStartDate,
                                  Map<String, Triple<String, String, String>> validationFailures) {
        int age = JurorUtils.getJurorAgeAtHearingDate(dateOfBirth, serviceStartDate);

        int lowerAgeLimit = responseInspector.getYoungestJurorAgeAllowed();
        int upperAgeLimit = responseInspector.getTooOldJurorAge();

        if (age < lowerAgeLimit) {
            validationFailures.put(jurorPool.getJurorNumber(), new Triple<>(
                JurorManagementConstants.BELOW_AGE_LIMIT_MESSAGE, jurorPool.getJuror().getFirstName(),
                jurorPool.getJuror().getLastName()));
        } else if (age >= upperAgeLimit) {
            validationFailures.put(jurorPool.getJurorNumber(), new Triple<>(
                JurorManagementConstants.ABOVE_AGE_LIMIT_MESSAGE, jurorPool.getJuror().getFirstName(),
                jurorPool.getJuror().getLastName()));
        }
    }

    /**
     * To cover the scenario where a Pool Member is being transferred back to their original court location - the
     * previous pool member record should be logically deleted (IS_ACTIVE = 'N') and their status code should be
     * updated to 10 (Reassigned) - this is because there can only be one, active pool member per court location.
     *
     * @param jurorNumber            9-digit numeric string to identify a juror
     * @param receivingCourtLocation object representation of the court location the juror is being transferred to
     *                               (receiving court)
     */
    private void logicallyDeleteExistingJurorPool(String jurorNumber, CourtLocation receivingCourtLocation) {
        log.trace("Enter logicallyDeletePreviousJurorPools");
        JurorPool targetJurorPool = jurorPoolRepository.findByJurorNumberAndIsActiveAndCourt(jurorNumber, true,
            receivingCourtLocation);

        if (targetJurorPool != null) {
            log.debug("Juror: {} - Existing active Pool Member found in receiving court {}", jurorNumber,
                receivingCourtLocation.getLocCode()
            );
            if (targetJurorPool.getStatus().getStatus() == IJurorStatus.TRANSFERRED) {
                targetJurorPool.setIsActive(false);
                jurorPoolRepository.save(targetJurorPool);
            } else {
                throw new MojException.InternalServerError(
                    String.format("Juror: %s - An active juror record exists "
                            + "at court location %s but has a current status of %s", jurorNumber,
                        receivingCourtLocation.getLocCode(), targetJurorPool.getStatus().getStatusDesc()
                    ),
                    null
                );
            }
        }

        log.trace("Exit logicallyDeletePreviousJurorPools");
    }

    private JurorPool createTransferredJurorPool(JurorPool sourceJurorPool, CourtLocation receivingCourtLocation,
                                                 PoolRequest targetPool, String currentUser) {
        log.trace("Enter createTransferredJurorPool");
        JurorPool newTargetJurorPool = new JurorPool();
        BeanUtils.copyProperties(sourceJurorPool, newTargetJurorPool,
            JurorManagementConstants.POOL_MEMBER_IGNORE_PROPERTIES
        );

        newTargetJurorPool.setOwner(receivingCourtLocation.getOwner());
        newTargetJurorPool.setPool(targetPool);
        newTargetJurorPool.setNextDate(targetPool.getReturnDate());
        newTargetJurorPool.setUserEdtq(currentUser);
        newTargetJurorPool.setOnCall(false);
        newTargetJurorPool.setReminderSent(null);

        newTargetJurorPool.setStatus(RepositoryUtils
            .retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository));

        int nextPoolSequenceNumber = poolMemberSequenceService
            .getPoolMemberSequenceNumber(targetPool.getPoolNumber());
        newTargetJurorPool.setPoolSequence(poolMemberSequenceService.leftPadInteger(nextPoolSequenceNumber));

        log.debug("Juror: {} - New Pool Member record created for Court Location: {}. Transferred to Pool: {}",
            sourceJurorPool.getJurorNumber(), receivingCourtLocation.getName(), targetPool.getPoolNumber());

        return jurorPoolRepository.save(newTargetJurorPool);
    }

    private void updateSourceJurorPoolTransfer(JurorPool sourceJurorPool, String currentUser) {
        log.trace("Enter updateSourceJurorPool");

        sourceJurorPool.setStatus(RepositoryUtils
            .retrieveFromDatabase(IJurorStatus.TRANSFERRED, jurorStatusRepository));
        sourceJurorPool.setUserEdtq(currentUser);
        sourceJurorPool.setNextDate(null);
        sourceJurorPool.setTransferDate(LocalDate.now());

        log.debug("Juror: {} - Pool Member transferred from Pool: {} by {}", sourceJurorPool.getJurorNumber(),
            sourceJurorPool.getPoolNumber(), currentUser
        );

        jurorPoolRepository.save(sourceJurorPool);
        log.trace("Exit updateSourceJurorPool");
    }


}


