package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.bureau.service.BureauService;
import uk.gov.hmcts.juror.api.bureau.service.ResponseExcusalService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.ContactLogRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EditJurorRecordRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.FilterableJurorDetailsRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAddressDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNameDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorOpticRefRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessNameChangeRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessPendingJurorRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.UpdateAttendanceRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactEnquiryTypeListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactLogListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.FilterableJurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAttendanceDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsCommonResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorNotesDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorOverviewResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorRecordSearchDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorSummonsReplyResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PendingJurorsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.ContactCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryType;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.IContactCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.PendingJuror;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.ApprovalDecision;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PendingJurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactEnquiryTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactLogRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorDetailRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.ReasonableAdjustmentsRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAuditChangeService;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.FAILED_TO_ATTEND_HAS_ATTENDANCE_RECORD;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.FAILED_TO_ATTEND_HAS_COMPLETION_DATE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_STATUS_MUST_BE_FAILED_TO_ATTEND;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_STATUS_MUST_BE_RESPONDED;
import static uk.gov.hmcts.juror.api.moj.service.PoolCreateService.DISQUALIFIED_ON_SELECTION;
import static uk.gov.hmcts.juror.api.moj.utils.JurorUtils.checkReadAccessForCurrentUser;

/**
 * Juror Record service.
 */
@Slf4j
@Service
@SuppressWarnings({"PMD.TooManyMethods", "PMD.LawOfDemeter", "PMD.ExcessiveImports"})
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurorRecordServiceImpl implements JurorRecordService {
    private final ContactCodeRepository contactCodeRepository;
    private static final Character NEW_REQUEST_STATE = 'N';

    private static final String REPLY_METHOD_ONLINE = "DIGITAL";
    private static final String REPLY_METHOD_PAPER = "PAPER";
    private static final String REPLY_METHOD_NOT_AVAILABLE = "RESPONSE N/A";
    private static final String PART_HIST_LIST_TO_MATCH = "AWFI,PDEF,PDET,PDIS,PEXC,POST,RESP";
    private static final String SJO_COMMENTS = "SJO Comments";
    public static final String PENDING_JUROR_STATUS_NOT_FOUND = "Pending Juror Status not found";

    private final JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;
    private final PoolRequestRepository poolRequestRepository;
    private final PendingJurorStatusRepository pendingJurorStatusRepository;
    private final JurorRepository jurorRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final JurorHistoryRepository jurorHistoryRepository;
    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    private final CourtLocationService courtLocationService;
    private final ContactLogRepository contactLogRepository;
    private final ContactEnquiryTypeRepository contactEnquiryTypeRepository;
    private final JurorDetailRepositoryMod jurorDetailRepositoryMod;
    private final BureauService bureauService;
    private final ResponseExcusalService responseExcusalService;
    private final JurorAuditChangeService jurorAuditChangeService;
    private final LetterServiceMod letterServiceMod;
    private final GeneratePoolNumberService generatePoolNumberService;
    private final CourtLocationRepository courtLocationRepository;
    private final PoolHistoryRepository poolHistoryRepository;
    private final PoolTypeRepository poolTypeRepository;
    private final PendingJurorRepository pendingJurorRepository;
    private final JurorHistoryService jurorHistoryService;
    private final JurorAppearanceService jurorAppearanceService;
    private final AppearanceRepository appearanceRepository;
    private final ReasonableAdjustmentsRepository reasonableAdjustmentsRepository;
    private final Clock clock;

    @Override
    @Transactional
    public void editJurorDetails(BureauJWTPayload payload, EditJurorRecordRequestDto requestDto, String jurorNumber) {
        log.info(String.format("Juror: %s. Start updating details by user %s", jurorNumber, payload.getLogin()));

        String owner = payload.getOwner();

        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, jurorNumber);
        JurorUtils.checkOwnershipForCurrentUser(juror, owner);

        juror.setTitle(requestDto.getTitle());
        juror.setFirstName(requestDto.getFirstName());
        juror.setLastName(requestDto.getLastName());
        juror.setAddressLine1(requestDto.getAddressLineOne());
        juror.setAddressLine2(requestDto.getAddressLineTwo());
        juror.setAddressLine3(requestDto.getAddressLineThree());
        juror.setAddressLine4(requestDto.getAddressTown());
        juror.setAddressLine5(requestDto.getAddressCounty());
        juror.setPostcode(requestDto.getAddressPostcode());
        juror.setDateOfBirth(requestDto.getDateOfBirth());
        juror.setPhoneNumber(requestDto.getPrimaryPhone());
        juror.setAltPhoneNumber(requestDto.getSecondaryPhone());
        juror.setEmail(requestDto.getEmailAddress());

        //save reasonable adjustments to reasonable adjustment repository

        if (requestDto.getSpecialNeed() != null) {
            juror.setReasonableAdjustmentCode(requestDto.getSpecialNeed());
            juror.setReasonableAdjustmentMessage(requestDto.getSpecialNeedMessage());
            updateJurorReasonableAdjustments(requestDto, jurorNumber);
        } else {
            juror.setReasonableAdjustmentCode(null);
            juror.setReasonableAdjustmentMessage(null);
        }

        juror.setPendingTitle(requestDto.getPendingTitle());
        juror.setPendingFirstName(requestDto.getPendingFirstName());
        juror.setPendingLastName(requestDto.getPendingLastName());
        juror.setOpticRef(requestDto.getOpticReference());
        juror.setWelsh(requestDto.getWelshLanguageRequired());

        jurorRepository.save(juror);
    }

    private void updateJurorReasonableAdjustments(EditJurorRecordRequestDto requestDto, String jurorNumber) {

        ReasonableAdjustments reasonableAdjustments =
            RepositoryUtils.retrieveFromDatabase(requestDto.getSpecialNeed(), reasonableAdjustmentsRepository);

        if (jurorReasonableAdjustmentRepository.findByJurorNumber(jurorNumber).isEmpty()) {
            JurorReasonableAdjustment jurorReasonableAdjustment = new JurorReasonableAdjustment();
            jurorReasonableAdjustment.setJurorNumber(jurorNumber);
            jurorReasonableAdjustment.setReasonableAdjustment(reasonableAdjustments);
            jurorReasonableAdjustment.setReasonableAdjustmentDetail(requestDto.getSpecialNeedMessage());
            jurorReasonableAdjustmentRepository.save(jurorReasonableAdjustment);
        } else {
            List<JurorReasonableAdjustment> jurorReasonableAdjustment =
                jurorReasonableAdjustmentRepository.findByJurorNumber(jurorNumber);
            jurorReasonableAdjustment.get(0).setReasonableAdjustmentDetail(requestDto.getSpecialNeedMessage());
            jurorReasonableAdjustment.get(0).setReasonableAdjustment(reasonableAdjustments);
            jurorReasonableAdjustmentRepository.saveAll(jurorReasonableAdjustment);
        }
    }


    Juror getJuror(String jurorNumber, Long jurorVersion) {
        final Juror juror;
        if (jurorVersion == null) {
            juror = jurorRepository.findByJurorNumber(jurorNumber);
        } else {
            juror = jurorRepository.findRevision(jurorNumber, jurorVersion)
                .map(Revision::getEntity).orElse(null);
        }
        if (juror == null) {
            throw new MojException.NotFound(
                "Juror not found: JurorNumber: " + jurorNumber + " Revision: " + jurorVersion, null);
        }
        return juror;
    }

    @Override
    @Transactional(readOnly = true)
    public FilterableJurorDetailsResponseDto getJurorDetails(FilterableJurorDetailsRequestDto request) {
        FilterableJurorDetailsRequestDto.FilterContext context = new FilterableJurorDetailsRequestDto.FilterContext(
            getJuror(request.getJurorNumber(), request.getJurorVersion())
        );
        FilterableJurorDetailsResponseDto filterableJurorDetailsResponseDto = new FilterableJurorDetailsResponseDto();
        filterableJurorDetailsResponseDto.setJurorNumber(request.getJurorNumber());
        filterableJurorDetailsResponseDto.setJurorVersion(request.getJurorVersion());
        request.getInclude().forEach(includeType -> includeType.apply(filterableJurorDetailsResponseDto, context));
        return filterableJurorDetailsResponseDto;
    }

    /**
     * Return the Juror details information.
     *
     * @param jurorNumber The Juror record number
     */
    @Override
    @Transactional(readOnly = true)
    public JurorDetailsResponseDto getJurorDetails(BureauJWTPayload payload, String jurorNumber, String locCode) {
        log.info("Retrieving juror record details for juror {} by user {}", jurorNumber, payload.getLogin());
        CourtLocation courtLocation = courtLocationService.getCourtLocation(locCode);
        // should be only one active and editable record
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPool(jurorPoolRepository, jurorNumber, courtLocation);

        // do a check to see if a court user should be able to view this record
        JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, payload.getOwner());

        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
        JurorDetailsResponseDto jurorDetailsResponseDto = new JurorDetailsResponseDto(jurorPool,
            jurorStatusRepository, responseExcusalService, pendingJurorRepository);

        // need to send reply method and status so front end can determine if edit should be from response or juror
        // record
        if (jurorResponse != null) {
            jurorDetailsResponseDto.setReplyMethod(REPLY_METHOD_ONLINE);
            jurorDetailsResponseDto.setReplyProcessingStatus(jurorResponse.getProcessingStatus().getDescription());
            // Todo keep this in for now but will need to revisit post UAT
            if (jurorResponse.getThirdPartyFName() != null) {
                jurorDetailsResponseDto.setThirdParty(getThirdPartyDetails(jurorResponse));
            }
            return jurorDetailsResponseDto;
        }

        PaperResponse response = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        if (response != null) {
            jurorDetailsResponseDto.setReplyMethod(REPLY_METHOD_PAPER);
            jurorDetailsResponseDto.setReplyProcessingStatus(response.getProcessingStatus().getDescription());
        }

        return jurorDetailsResponseDto;
    }


    @Override
    @Transactional(readOnly = true)
    public JurorOverviewResponseDto getJurorOverview(BureauJWTPayload payload, String jurorNumber, String locCode) {
        log.info("Retrieving juror record overview for juror {} by user {}", jurorNumber, payload.getLogin());
        CourtLocation courtLocation = courtLocationService.getCourtLocation(locCode);

        // should be only one active and editable record
        JurorPool jurorPool =
            jurorPoolRepository.findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);

        //check if juror pool exists
        if (jurorPool == null) {
            log.debug("No active juror record found for juror {}", jurorNumber);
            return null;
        }

        // do a check to see if a court user should be able to view this record
        JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, payload.getOwner());

        Juror juror = jurorPool.getJuror();

        //check if juror was summoned or disqualified
        if (Objects.equals(jurorPool.getStatus().getStatus(), IJurorStatus.SUMMONED)
            || (Objects.equals(jurorPool.getStatus().getStatus(), IJurorStatus.DISQUALIFIED)
            && juror.getSummonsFile() != null
            && juror.getSummonsFile().equals(DISQUALIFIED_ON_SELECTION))) {
            //return just the common details
            return new JurorOverviewResponseDto(jurorPool, jurorStatusRepository, responseExcusalService,
                pendingJurorRepository);
        }

        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);

        if (jurorResponse != null) {
            JurorOverviewResponseDto jurorOverviewResponseDto = new JurorOverviewResponseDto(jurorPool,
                jurorStatusRepository, responseExcusalService, pendingJurorRepository);
            jurorOverviewResponseDto.setOpticReference(jurorPool.getJuror().getOpticRef());
            jurorOverviewResponseDto.setWelshLanguageRequired(jurorPool.getJuror().getWelsh());
            jurorOverviewResponseDto.setReplyMethod(REPLY_METHOD_ONLINE);
            jurorOverviewResponseDto.setReplyDate(jurorResponse.getDateReceived().toLocalDate());
            jurorOverviewResponseDto.setReplyStatus(jurorResponse.getProcessingStatus().getDescription());
            return jurorOverviewResponseDto;
        }

        LocalDate twelveMonthsAgo = LocalDate.now().minusMonths(12);
        //look for juror history records for juror within the last 12 months
        List<JurorHistory> jurorHistoryList = jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(
            jurorPool.getJurorNumber(), twelveMonthsAgo);

        if (!jurorHistoryList.isEmpty()) {

            //check if any of the history entries match the paper response processing entries
            List<JurorHistory> jurorHistFiltered =
                jurorHistoryList.stream().filter(p -> PART_HIST_LIST_TO_MATCH.contains(p.getHistoryCode().getCode()))
                    .toList();
            if (!jurorHistFiltered.isEmpty()) {
                JurorOverviewResponseDto jurorOverviewResponseDto = new JurorOverviewResponseDto(jurorPool,
                    jurorStatusRepository, responseExcusalService, pendingJurorRepository);

                jurorOverviewResponseDto.setReplyMethod(REPLY_METHOD_PAPER);
                return jurorOverviewResponseDto;
            }
        }

        //send the default response.
        JurorOverviewResponseDto jurorOverviewResponseDto = new JurorOverviewResponseDto(jurorPool,
            jurorStatusRepository, responseExcusalService, pendingJurorRepository);

        jurorOverviewResponseDto.setReplyMethod(REPLY_METHOD_NOT_AVAILABLE);
        return jurorOverviewResponseDto;
    }

    @Override
    public JurorRecordSearchDto searchJurorRecord(BureauJWTPayload payload, String jurorNumber) {
        log.info("Searching for juror number {} by user {}", jurorNumber, payload.getLogin());

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> data = new ArrayList<>();

        // can be multiple active records for a juror in different locations
        List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);

        //check if any juror pool found
        if (jurorPools.isEmpty()) {
            log.debug("No active juror record found for juror {}", jurorNumber);
            return new JurorRecordSearchDto(data);
        }

        String owner = payload.getOwner();

        if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
            //return all records for bureau user
            for (JurorPool jurorPool : jurorPools) {
                data.add(new JurorRecordSearchDto.JurorRecordSearchDataDto(jurorPool));
            }
        } else {
            //return court owned records for court user
            for (JurorPool jurorPool : jurorPools) {
                if (jurorPool.getOwner().equals(owner)) {
                    data.add(new JurorRecordSearchDto.JurorRecordSearchDataDto(jurorPool));
                }
            }
        }

        return new JurorRecordSearchDto(data);
    }

    @Override
    public PendingJurorsResponseDto getPendingJurors(String locCode, PendingJurorStatus status) {

        List<PendingJurorsResponseDto.PendingJurorsResponseData> pendingJurorsResponseData =
            pendingJurorRepository.findPendingJurorsForCourt(locCode, status);

        return PendingJurorsResponseDto.builder()
            .data(pendingJurorsResponseData)
            .build();
    }

    /**
     * Court users can create new Juror record for a given court location under their control, the Juror can be in an
     * existing pool or a new pool can be created for the new juror. Bureau users cannot create a new Juror record in
     * this way. The juror record created will be stored in a pending juror table until approved by a senior jury
     * officer.
     *
     * @param payload               web token payload containing the current user's basic authorisation information
     * @param jurorCreateRequestDto request payload containing the necessary data items to create a new Juror record
     */
    @Transactional
    @Override
    @IsCourtUser
    public void createJurorRecord(BureauJWTPayload payload, JurorCreateRequestDto jurorCreateRequestDto) {
        log.info("User {} creating a pending Juror record in court location", payload.getLogin(),
            jurorCreateRequestDto.getLocationCode());

        String poolNumber = jurorCreateRequestDto.getPoolNumber();
        PoolRequest poolRequest;

        if (poolNumber != null) {
            poolRequest = RepositoryUtils.retrieveFromDatabase(poolNumber,
                poolRequestRepository);
            // check if the court user owns the pool
            if (!poolRequest.getOwner().equals(payload.getOwner())) {
                throw new MojException.Forbidden(
                    "Court user cannot create a juror record in pool " + poolNumber,
                    null);
            }
        } else {
            poolRequest = createNewPoolFromDto(jurorCreateRequestDto, payload);
        }

        // assign the poolNumber to the actual pool request number
        poolNumber = poolRequest.getPoolNumber();

        log.info("Creating a pending Juror record in pool {}", poolNumber);

        PendingJurorStatus pendingJurorStatus =
            pendingJurorStatusRepository.findById(PendingJurorStatusEnum.QUEUED.getCode())
                .orElseThrow(() -> new MojException.NotFound(PENDING_JUROR_STATUS_NOT_FOUND, null));

        String pendingJurorNumber = pendingJurorRepository
            .generatePendingJurorNumber(jurorCreateRequestDto.getLocationCode());

        JurorAddressDto jurorAddress = jurorCreateRequestDto.getAddress();
        PendingJuror pendingJuror = PendingJuror.builder()
            .jurorNumber(pendingJurorNumber)
            .poolNumber(poolNumber)
            .title(jurorCreateRequestDto.getTitle())
            .firstName(jurorCreateRequestDto.getFirstName())
            .lastName(jurorCreateRequestDto.getLastName())
            .dateOfBirth(jurorCreateRequestDto.getDateOfBirth())
            .addressLine1(jurorAddress.getLineOne())
            .addressLine2(jurorAddress.getLineTwo())
            .addressLine3(jurorAddress.getLineThree())
            .addressLine4(jurorAddress.getTown())
            .addressLine5(jurorAddress.getCounty())
            .postcode(jurorAddress.getPostcode())
            .phoneNumber(jurorCreateRequestDto.getPrimaryPhone())
            .altPhoneNumber(jurorCreateRequestDto.getAlternativePhone())
            .email(jurorCreateRequestDto.getEmailAddress())
            .notes(jurorCreateRequestDto.getNotes())
            .status(pendingJurorStatus)
            .nextDate(poolRequest.getReturnDate())
            .responded(true)
            .build();

        pendingJurorRepository.save(pendingJuror);

        log.info("Pending Juror record created for juror {} in pool {}", pendingJuror.getJurorNumber(),
            pendingJuror.getPoolNumber());

    }

    @Override
    @Transactional
    public void processPendingJuror(ProcessPendingJurorRequestDto processPendingJurorRequestDto) {

        final String jurorNumber = processPendingJurorRequestDto.getJurorNumber();
        log.info("Processing pending juror {} with decision {}", processPendingJurorRequestDto.getJurorNumber(),
            processPendingJurorRequestDto.getDecision());

        PendingJuror pendingJuror = RepositoryUtils.retrieveFromDatabase(jurorNumber, pendingJurorRepository);

        validateStatusOfPendingJuror(pendingJuror);

        // check if we need to add any notes from SJO user
        if (processPendingJurorRequestDto.getComments() != null && !processPendingJurorRequestDto.getComments()
            .isEmpty()) {
            String notes = pendingJuror.getNotes() == null
                ? ""
                : pendingJuror.getNotes() + "\n\n";
            pendingJuror.setNotes(notes + SJO_COMMENTS + "\n" + processPendingJurorRequestDto.getComments());
        }

        PendingJurorStatus pendingJurorStatus = null;
        if (processPendingJurorRequestDto.getDecision().equals(ApprovalDecision.APPROVE)) {
            pendingJurorStatus = pendingJurorStatusRepository.findById(PendingJurorStatusEnum.AUTHORISED.getCode())
                .orElseThrow(() -> new MojException.NotFound(PENDING_JUROR_STATUS_NOT_FOUND, null));
            updatePendingJuror(pendingJuror, pendingJurorStatus);
            //create juror record from pending juror record and associated pool
            createApprovedJurorRecord(pendingJuror);

        } else {
            pendingJurorStatus = pendingJurorStatusRepository.findById(PendingJurorStatusEnum.REJECTED.getCode())
                .orElseThrow(() -> new MojException.NotFound(PENDING_JUROR_STATUS_NOT_FOUND, null));
            updatePendingJuror(pendingJuror, pendingJurorStatus);
        }

        log.info("Pending juror {} processed with decision {}", pendingJuror.getJurorNumber(),
            pendingJuror.getStatus().getDescription());
    }

    @Override
    public void updateAttendance(UpdateAttendanceRequestDto dto) {
        log.info("Placing juror on call for juror {} ", dto.getJurorNumber());

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, dto.getJurorNumber());

        validateUpdateAttendance(dto);

        if (dto.isOnCall()) {
            validateOnCall(jurorPool);
            jurorPool.setOnCall(true);
            jurorPool.setNextDate(null);
        } else if (dto.getNextDate() != null) {
            jurorPool.setOnCall(false);
            jurorPool.setNextDate(dto.getNextDate());
        } else {
            throw new MojException.BadRequest("Must select either on call or enter new date",
                null);
        }

        jurorPoolRepository.save(jurorPool);

    }


    private void validateOnCall(JurorPool jurorPool) {
        if (jurorPool.getOnCall() != null && jurorPool.getOnCall().equals(true)) {
            throw new MojException.BadRequest("Juror status is already on call", null);
        }
    }

    private void validateUpdateAttendance(UpdateAttendanceRequestDto dto) {
        if (dto.isOnCall() && dto.getNextDate() != null) {
            throw new MojException.BadRequest("Cannot place juror on call and have a next date",
                null);
        }
    }

    private void validateStatusOfPendingJuror(PendingJuror pendingJuror) {
        if (pendingJuror.getStatus().getCode().equals(PendingJurorStatusEnum.AUTHORISED.getCode())
            || pendingJuror.getStatus().getCode().equals(PendingJurorStatusEnum.REJECTED.getCode())) {
            throw new MojException.BadRequest("Pending Juror has already been processed", null);
        }
    }

    private void createApprovedJurorRecord(PendingJuror pendingJuror) {
        //create juror record
        Juror juror = Juror.builder()
            .jurorNumber(pendingJuror.getJurorNumber())
            .title(pendingJuror.getTitle())
            .firstName(pendingJuror.getFirstName())
            .lastName(pendingJuror.getLastName())
            .dateOfBirth(pendingJuror.getDateOfBirth())
            .addressLine1(pendingJuror.getAddressLine1())
            .addressLine2(pendingJuror.getAddressLine2())
            .addressLine3(pendingJuror.getAddressLine3())
            .addressLine4(pendingJuror.getAddressLine4())
            .addressLine5(pendingJuror.getAddressLine5())
            .postcode(pendingJuror.getPostcode())
            .phoneNumber(pendingJuror.getPhoneNumber())
            .altPhoneNumber(pendingJuror.getAltPhoneNumber())
            .email(pendingJuror.getEmail())
            .notes(pendingJuror.getNotes())
            .responded(true)
            .build();

        jurorRepository.save(juror);
        log.info("Juror record created for juror {}", pendingJuror.getJurorNumber());

        PoolRequest poolRequest =
            RepositoryUtils.retrieveFromDatabase(pendingJuror.getPoolNumber(), poolRequestRepository);
        JurorPool jurorPool = JurorPool.builder()
            .pool(poolRequest)
            .juror(juror)
            .status(RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository))
            .isActive(true)
            .owner(poolRequest.getOwner())
            .nextDate(pendingJuror.getNextDate())
            .userEdtq(SecurityUtil.getActiveLogin())
            .build();

        jurorPoolRepository.save(jurorPool);
        log.info("Juror Pool record created for juror {}", pendingJuror.getJurorNumber());

        //create juror history record
        jurorHistoryService.createPendingJurorAuthorisedHistory(jurorPool);
    }


    private void updatePendingJuror(PendingJuror pendingJuror, PendingJurorStatus pendingJurorStatus) {
        pendingJuror.setStatus(pendingJurorStatus);
        pendingJurorRepository.save(pendingJuror);
    }


    private PoolRequest createNewPoolFromDto(JurorCreateRequestDto jurorCreateRequestDto, BureauJWTPayload payload) {
        String courtLocationCode = jurorCreateRequestDto.getLocationCode();

        log.debug("Retrieve the Court Location object from the database for: " + courtLocationCode);
        CourtLocation courtLocation = RepositoryUtils.retrieveFromDatabase(jurorCreateRequestDto.getLocationCode(),
            courtLocationRepository);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(generatePoolNumberService.generatePoolNumber(courtLocationCode,
            jurorCreateRequestDto.getStartDate()));
        poolRequest.setOwner(payload.getOwner());
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setNewRequest(NEW_REQUEST_STATE);
        poolRequest.setReturnDate(jurorCreateRequestDto.getStartDate());
        // explicitly set number requested to null (not 0) so we can differentiate between these pools and nil pools
        poolRequest.setNumberRequested(null);

        poolRequest.setAttendTime(LocalDateTime.of(jurorCreateRequestDto.getStartDate(),
            courtLocation.getCourtAttendTime()));

        poolRequest.setPoolType(
            RepositoryUtils.retrieveFromDatabase(jurorCreateRequestDto.getPoolType(), poolTypeRepository));

        poolRequestRepository.saveAndFlush(poolRequest);

        poolHistoryRepository.save(
            new PoolHistory(poolRequest.getPoolNumber(), LocalDateTime.now(), HistoryCode.PREQ,
                payload.getLogin(), String.format("Pool Request %s created for pending Juror",
                poolRequest.getPoolNumber()
            )));

        return poolRequest;
    }

    /**
     * Query the database to retrieve the currently active juror pool record. Bureau users can view both Bureau owned
     * and court owned records, whereas court users can only view juror records owned by a court the current user has
     * access to
     *
     * @param payload     web token payload containing the current user's basic authorisation information
     * @param jurorNumber 9 digit (numeric string) to uniquely identify a given juror record
     * @return A JSON response containing a list of Contact Log records
     */
    @Override
    public ContactLogListDto getJurorContactLogs(BureauJWTPayload payload, String jurorNumber) {
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber,
            payload.getOwner());
        // do a check to see if a court user should be able to view this record
        checkReadAccessForCurrentUser(jurorPoolRepository, jurorPool.getJurorNumber(), payload.getOwner());
        List<ContactLog> contactLogs = contactLogRepository.findByJurorNumber(jurorNumber);
        List<ContactLogListDto.ContactLogDataDto> contactLogDataList = new ArrayList<>();

        for (ContactLog contactLog : contactLogs) {
            contactLogDataList.add(new ContactLogListDto.ContactLogDataDto(contactLog));
        }

        return new ContactLogListDto(contactLogDataList, new JurorDetailsCommonResponseDto(jurorPool,
            jurorStatusRepository, responseExcusalService, pendingJurorRepository));
    }

    /**
     * Bureau users can only create new contact logs against Bureau owned juror records. Once a record has been
     * transferred from the Bureau to a court, the bureau officers have read-only access to the contact logs.
     * Court users can only create new contact logs against juror records owned by a court the current user has access
     * to (for example juror records which have been transferred from the Bureau to the court, or juror records added to
     * the pool as court owned deferrals).
     *
     * @param payload              web token payload containing the current user's basic authorisation information
     * @param contactLogRequestDto request payload containing the necessary data items to create a new contact log
     *                             record in the database
     */
    @Override
    @Transactional(propagation = REQUIRED)
    public void createJurorContactLog(BureauJWTPayload payload, ContactLogRequestDto contactLogRequestDto) {
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository,
            contactLogRequestDto.getJurorNumber(), payload.getOwner());
        // check whether the current user has permissions to create new contact logs against the currently active
        // juror record
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());


        ContactCode enquiryType = RepositoryUtils.retrieveFromDatabase(
            IContactCode.fromCode(contactLogRequestDto.getEnquiryType()).getCode(), contactCodeRepository);

        ContactLog contactLog = ContactLog.builder()
            .username(payload.getLogin())
            .jurorNumber(contactLogRequestDto.getJurorNumber())
            .startCall(contactLogRequestDto.getStartCall())
            .enquiryType(enquiryType)
            .notes(contactLogRequestDto.getNotes())
            .repeatEnquiry(false)
            .build();

        contactLogRepository.saveAndFlush(contactLog);
    }

    /**
     * Query the database to extract a list of all available Contact Enquiry Type values.
     *
     * @return a DTO containing a list of all available Contact Enquiry Types (serialised)
     */
    @Override
    public ContactEnquiryTypeListDto getContactEnquiryTypes() {
        List<ContactEnquiryType> enquiryTypes = new ArrayList<>();
        contactEnquiryTypeRepository.findAll().forEach(enquiryTypes::add);
        return new ContactEnquiryTypeListDto(enquiryTypes);
    }

    @Override
    public JurorNotesDto getJurorNotes(String jurorNumber, String owner) {
        checkReadAccessForCurrentUser(jurorPoolRepository, jurorNumber, owner);

        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber, owner);
        Juror juror = jurorPool.getJuror();

        return new JurorNotesDto(juror.getNotes(), new JurorDetailsCommonResponseDto(jurorPool, jurorStatusRepository,
            responseExcusalService, pendingJurorRepository));
    }

    @Override
    public void setJurorNotes(String jurorNumber, String notes, String owner) {
        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, jurorNumber);
        JurorUtils.checkOwnershipForCurrentUser(juror, owner, true);
        juror.setNotes(notes);

        jurorRepository.saveAndFlush(juror);
    }

    @Override
    @Transactional
    public void createJurorOpticReference(BureauJWTPayload payload, JurorOpticRefRequestDto opticsRefRequestDto) {

        final String jurorNumber = opticsRefRequestDto.getJurorNumber();
        final String poolNumber = opticsRefRequestDto.getPoolNumber();
        final String opticsRef = opticsRefRequestDto.getOpticReference();
        final String owner = payload.getOwner();
        log.info("Creating an Optics reference for Juror {} in pool {} by user {}", jurorNumber, poolNumber,
            payload.getLogin());

        Juror juror = jurorRepository.findById(jurorNumber).orElseThrow(() ->
            new MojException.NotFound(String.format("Unable to find valid juror record for Juror Number: %s",
                jurorNumber), null));

        // only allow access if the owner of record is same as users owner
        JurorUtils.checkOwnershipForCurrentUser(juror, owner);

        juror.setOpticRef(opticsRef);
        jurorRepository.save(juror);

        Optional<DigitalResponse> digitalResponse = jurorResponseRepository.findById(jurorNumber);
        if (digitalResponse.isPresent()) {
            DigitalResponse response = digitalResponse.get();
            response.setProcessingStatus(ProcessingStatus.AWAITING_COURT_REPLY);
            jurorResponseRepository.save(response);
            log.debug("Finished adding optics reference for juror {}", jurorNumber);
            return;
        }

        PaperResponse paperResponse = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        if (paperResponse != null) {
            paperResponse.setProcessingStatus(ProcessingStatus.AWAITING_COURT_REPLY);
            jurorPaperResponseRepository.save(paperResponse);
            log.debug("Finished adding optics reference for juror {}", jurorNumber);
        }
    }

    @Override
    public String getJurorOpticReference(String jurorNumber, String poolNumber, BureauJWTPayload payload) {

        log.info("Retrieving an Optics reference for Juror {} in pool {} by user {}", jurorNumber, poolNumber,
            payload.getLogin());

        final String owner = payload.getOwner();

        List<JurorPool> jurorPools = JurorPoolUtils.getActiveJurorPoolRecords(jurorPoolRepository, jurorNumber);
        JurorPool jurorPool = jurorPools.stream().filter(p -> {
            if (JurorDigitalApplication.JUROR_OWNER.equals(owner)) {
                return true;
            } else {
                return p.getOwner().equals(owner);
            }
        }).findFirst().orElseThrow(() ->
            new MojException.Forbidden("Current user does not have ownership of any "
                + "associated pools for juror", null));

        // Bureau should always be able to read record
        JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, owner);

        log.debug("Finished retrieving optics reference for juror {}", jurorNumber);
        return jurorPool.getJuror().getOpticRef();
    }

    @Override
    public JurorBankDetailsDto getJurorBankDetails(String jurorNumber) {

        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, jurorNumber);
        String owner = SecurityUtil.getActiveOwner();

        JurorUtils.checkOwnershipForCurrentUser(juror, owner);

        return new JurorBankDetailsDto(juror);
    }

    /**
     * Uses the MOJ Bureau Details view, which is filtered to view juror records when they were owned by the Bureau.
     *
     * @param jurorNumber 9-digit numeric string to identify a juror record
     * @param owner       3-digit numeric string to uniquely identify the court location the current user belongs to
     * @return Fully populated DTO for a Bureau owned juror record with a digital summons reply
     */
    @Override
    @Transactional(readOnly = true)
    public BureauJurorDetailDto getBureauDetailsByJurorNumber(String jurorNumber, String owner) {

        JurorPoolUtils.checkMultipleRecordReadAccess(jurorPoolRepository, jurorNumber, owner);

        ModJurorDetail jurorDetails = jurorDetailRepositoryMod.findById(jurorNumber)
            .orElseThrow( () -> new MojException.NotFound(String.format("Could not find juror details for %s",
                jurorNumber), null));

        BureauJurorDetailDto responseDto = bureauService.mapJurorDetailsToDto(jurorDetails);
        responseDto.setWelshCourt(jurorDetails.isWelshCourt());
        return responseDto;
    }

    @Override
    @Transactional
    public JurorSummonsReplyResponseDto getJurorSummonsReply(BureauJWTPayload payload, String jurorNumber,
                                                             String locCode) {
        log.info("Retrieving juror summons reply info for juror {} by user {}", jurorNumber, payload.getLogin());
        CourtLocation courtLocation = courtLocationService.getCourtLocation(locCode);

        // Can be multiple active court records if juror was transferred, filter by location
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPool(jurorPoolRepository, jurorNumber, courtLocation);

        JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, payload.getOwner());

        Juror juror = jurorPool.getJuror();

        //check if juror was disqualified exit quick
        if (Objects.equals(jurorPool.getStatus().getStatus(), IJurorStatus.DISQUALIFIED)
            && juror.getSummonsFile() != null
            && juror.getSummonsFile().equals("Disq. on selection")) {
            //return just the common details
            return new JurorSummonsReplyResponseDto(jurorPool, jurorStatusRepository, responseExcusalService,
                pendingJurorRepository);
        }


        // check if there is a digital reply from user
        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
        // check if there is a paper reply from user
        PaperResponse jurorPaperResponse = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);

        if (Objects.equals(jurorPool.getStatus().getStatus(), IJurorStatus.SUMMONED)
            && jurorResponse == null && jurorPaperResponse == null) {
            return new JurorSummonsReplyResponseDto(jurorPool, jurorStatusRepository, responseExcusalService,
                pendingJurorRepository);
        }

        if (jurorResponse != null) {
            JurorSummonsReplyResponseDto jurorSummonsReplyResponseDto = new JurorSummonsReplyResponseDto(jurorPool,
                jurorStatusRepository, responseExcusalService, pendingJurorRepository);
            jurorSummonsReplyResponseDto.setReplyMethod(REPLY_METHOD_ONLINE);
            jurorSummonsReplyResponseDto.setReplyDate(jurorResponse.getDateReceived().toLocalDate());
            jurorSummonsReplyResponseDto.setReplyStatus(jurorResponse.getProcessingStatus().getDescription());
            return jurorSummonsReplyResponseDto;
        }

        if (jurorPaperResponse != null) {
            JurorSummonsReplyResponseDto jurorSummonsReplyResponseDto = new JurorSummonsReplyResponseDto(jurorPool,
                jurorStatusRepository, responseExcusalService, pendingJurorRepository);
            jurorSummonsReplyResponseDto.setReplyMethod(REPLY_METHOD_PAPER);
            jurorSummonsReplyResponseDto.setReplyDate(jurorPaperResponse.getDateReceived().toLocalDate());
            jurorSummonsReplyResponseDto.setReplyStatus(jurorPaperResponse.getProcessingStatus().getDescription());
            return jurorSummonsReplyResponseDto;
        }

        LocalDate twelveMonthsAgo = LocalDate.now().minusMonths(12);
        //look for history records for juror within the last 12 months
        List<JurorHistory> jurorHistList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorPool.getJurorNumber(),
                twelveMonthsAgo);

        if (!jurorHistList.isEmpty()) {
            //check if any of the history entries match the paper response processing entries
            List<JurorHistory> jurorHistFiltered = jurorHistList.stream().filter(p ->
                PART_HIST_LIST_TO_MATCH.contains(p.getHistoryCode().getCode())).toList();

            if (!jurorHistFiltered.isEmpty()) {
                JurorSummonsReplyResponseDto jurorSummonsReplyResponseDto =
                    new JurorSummonsReplyResponseDto(jurorPool, jurorStatusRepository, responseExcusalService,
                        pendingJurorRepository);
                jurorSummonsReplyResponseDto.setReplyMethod(REPLY_METHOD_PAPER);
                return jurorSummonsReplyResponseDto;
            }
        }

        //send the default response
        JurorSummonsReplyResponseDto jurorSummonsReplyResponseDto = new JurorSummonsReplyResponseDto(jurorPool,
            jurorStatusRepository, responseExcusalService, pendingJurorRepository);

        jurorSummonsReplyResponseDto.setReplyMethod(REPLY_METHOD_NOT_AVAILABLE);
        return jurorSummonsReplyResponseDto;
    }

    @Override
    @Transactional
    public void setPendingNameChange(Juror juror, String pendingTitle,
                                     String pendingFirstName, String pendingLastName) {
        log.trace("Enter setPendingNameChange");
        juror.setPendingTitle(pendingTitle);
        juror.setPendingFirstName(pendingFirstName);
        juror.setPendingLastName(pendingLastName);

        log.debug("Juror {} has provided an updated name for approval - original name: {}, new pending name {}",
            juror.getJurorNumber(), juror.getFirstName() + " " + juror.getLastName(),
            pendingFirstName + " " + pendingLastName);

        jurorRepository.save(juror);
        log.trace("Exit setPendingNameChange");
    }

    /**
     * Update a Juror's name, bypassing the approval process. This is intended to only be used for small "fixes" to
     * a juror's name to help them pass a police check, for example, removing special characters.
     *
     * @param payload             JSON Web Token containing user authentication context
     * @param jurorNameDetailsDto Update juror name details to persist on the juror record
     * @param jurorNumber         The juror number to update the name details for
     */
    @Override
    @Transactional
    public void fixErrorInJurorName(BureauJWTPayload payload, String jurorNumber,
                                    JurorNameDetailsDto jurorNameDetailsDto) {
        log.trace("Enter fixErrorInJurorName");

        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber,
            payload.getOwner());

        updateJurorNameDetails(payload.getLogin(), jurorPool, jurorNameDetailsDto);
        jurorRepository.save(jurorPool.getJuror());

        log.trace("Exit fixErrorInJurorName");
    }

    @Override
    @Transactional
    public void processPendingNameChange(BureauJWTPayload payload, String jurorNumber,
                                         ProcessNameChangeRequestDto requestDto) {
        log.trace("Enter processPendingNameChange");

        final String username = payload.getLogin();

        final String changeOfNameCode = "CN";
        final String contactLogNotes = WordUtils.capitalize(requestDto.getDecision().getDescription())
            + " the juror's name change. " + requestDto.getNotes();

        JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
        Juror juror = jurorPool.getJuror();
        JurorUtils.checkOwnershipForCurrentUser(juror, payload.getOwner());

        jurorAuditChangeService.recordContactLog(juror, username, changeOfNameCode, contactLogNotes);
        jurorAuditChangeService.recordApprovalHistoryEvent(jurorNumber, requestDto.getDecision(), username,
            jurorPool.getPoolNumber());

        if (requestDto.getDecision().equals(ApprovalDecision.APPROVE)) {
            JurorNameDetailsDto dto = new JurorNameDetailsDto(juror.getPendingTitle(),
                juror.getPendingFirstName(), juror.getPendingLastName());
            updateJurorNameDetails(username, jurorPool, dto);
        }

        // flush out the pending name change values
        juror.setPendingTitle(null);
        juror.setPendingFirstName(null);
        juror.setPendingLastName(null);
        juror.setUserEdtq(username);
        jurorRepository.saveAndFlush(juror);

        log.trace("Exit processPendingNameChange");
    }

    private void updateJurorNameDetails(String auditorUsername, JurorPool jurorPool,
                                        JurorNameDetailsDto jurorNameDetailsDto) {
        log.trace("Enter updateJurorNameDetails");

        Juror juror = jurorPool.getJuror();
        Map<String, Boolean> changedPropertiesMap =
            jurorAuditChangeService.initChangedPropertyMap(juror, jurorNameDetailsDto);

        changedPropertiesMap.keySet().forEach(propName -> {
            if (Boolean.TRUE.equals(changedPropertiesMap.get(propName))) {
                jurorAuditChangeService.recordPersonalDetailsHistory(propName, juror, jurorPool.getPoolNumber(),
                    auditorUsername);
            }
        });

        juror.setTitle(jurorNameDetailsDto.getTitle());
        juror.setFirstName(jurorNameDetailsDto.getFirstName());
        juror.setLastName(jurorNameDetailsDto.getLastName());
        juror.setUserEdtq(auditorUsername);

        log.trace("Exit updateJurorNameDetails");
    }

    private JurorResponseDto.ThirdParty getThirdPartyDetails(DigitalResponse jurorResponse) {

        JurorResponseDto.ThirdParty thirdParty = new JurorResponseDto.ThirdParty();

        thirdParty.setThirdPartyFName(jurorResponse.getThirdPartyFName());
        thirdParty.setThirdPartyLName(jurorResponse.getThirdPartyLName());
        thirdParty.setThirdPartyReason(jurorResponse.getThirdPartyReason());
        thirdParty.setThirdPartyOtherReason(jurorResponse.getThirdPartyOtherReason());
        thirdParty.setRelationship(jurorResponse.getRelationship());
        thirdParty.setMainPhone(jurorResponse.getMainPhone());
        thirdParty.setOtherPhone(jurorResponse.getOtherPhone());
        thirdParty.setEmailAddress(jurorResponse.getEmailAddress());
        thirdParty.setUseJurorEmailDetails(jurorResponse.getJurorEmailDetails());
        thirdParty.setUseJurorPhoneDetails(jurorResponse.getJurorPhoneDetails());

        return thirdParty;
    }

    @Override
    @Transactional
    public void updatePncStatus(final String jurorNumber, final PoliceCheck policeCheck) {
        log.info("Attempting to update PNC check status for juror {} to be {}", jurorNumber, policeCheck);
        final JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
        final Juror juror = jurorPool.getJuror();

        final PoliceCheck oldPoliceCheckValue = juror.getPoliceCheck();
        final PoliceCheck newPoliceCheckValue = PoliceCheck.getEffectiveValue(oldPoliceCheckValue, policeCheck);
        if (oldPoliceCheckValue == newPoliceCheckValue) {
            log.debug("Skipping PNC check update for juror {} as new value equals existing value", jurorNumber);
            return;//If both are the same no point continuing
        }
        juror.setPoliceCheck(newPoliceCheckValue);

        if (newPoliceCheckValue == PoliceCheck.ELIGIBLE
            || newPoliceCheckValue == PoliceCheck.UNCHECKED_MAX_RETRIES_EXCEEDED
        ) {
            log.debug("Juror {} has passed police check verification with status {}", jurorNumber, policeCheck);
            //These values are set to null to align to the oracle JUROR.phoenix_checking.finalise procedure
            juror.setDisqualifyCode(null);
            juror.setDisqualifyDate(null);
            jurorHistoryService.createPoliceCheckQualifyHistory(jurorPool, newPoliceCheckValue.isChecked());
        } else if (newPoliceCheckValue == PoliceCheck.INELIGIBLE) {
            log.debug("Juror {} is ineligible disqualifying for police check", jurorNumber);
            jurorPool.setStatus(
                RepositoryUtils.retrieveFromDatabase(IJurorStatus.DISQUALIFIED, jurorStatusRepository));
            juror.setDisqualifyCode(DisCode.ELECTRONIC_POLICE_CHECK_FAILURE);
            juror.setDisqualifyDate(LocalDate.now(clock));

            letterServiceMod.createDisqualificationLetter(juror);
            jurorHistoryService.createPoliceCheckDisqualifyHistory(jurorPool);
        } else if (newPoliceCheckValue == PoliceCheck.IN_PROGRESS) {
            log.debug("Juror {} police check is in progress adding part history", jurorNumber);
            jurorHistoryService.createPoliceCheckInProgressHistory(jurorPool);
        } else if (newPoliceCheckValue == PoliceCheck.INSUFFICIENT_INFORMATION) {
            log.debug("Juror {} police check has insufficient information adding part history", jurorNumber);
            jurorHistoryService.createPoliceCheckInsufficientInformationHistory(jurorPool);
        }
        jurorPoolRepository.save(jurorPool);
        jurorRepository.save(juror);
    }

    @Override
    public void updateJurorToFailedToAttend(final String jurorNumber, final String poolNumber) {
        JurorPool jurorPool = getJurorPool(jurorNumber, poolNumber);

        if (jurorPool.getStatus().getStatus() != IJurorStatus.RESPONDED) {
            throw new MojException.BusinessRuleViolation(
                "Juror status must be responded in order to undo the failed to attend status.",
                JUROR_STATUS_MUST_BE_RESPONDED);
        }

        if (jurorPool.getJuror().getCompletionDate() != null) {
            throw new MojException.BusinessRuleViolation(
                "This juror cannot be given a Failed To Attend status because they have been given a completion date. "
                    + "Only a Senior Jury Officer can be remove the completion date",
                FAILED_TO_ATTEND_HAS_COMPLETION_DATE);
        }

        if (jurorAppearanceService.hasAppearances(jurorNumber)) {
            throw new MojException.BusinessRuleViolation(
                "This juror cannot be given a Failed To Attend status because they have had attendances recorded."
                    + " The Failed To Attend status is only for jurors who have not attended at all",
                FAILED_TO_ATTEND_HAS_ATTENDANCE_RECORD);
        }

        jurorPool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.FAILED_TO_ATTEND, jurorStatusRepository));
        jurorHistoryService.createFailedToAttendHistory(jurorPool);
        jurorPoolRepository.save(jurorPool);
    }

    @Override
    public void undoUpdateJurorToFailedToAttend(String jurorNumber, String poolNumber) {
        JurorPool jurorPool = getJurorPool(jurorNumber, poolNumber);
        if (jurorPool.getStatus().getStatus() != IJurorStatus.FAILED_TO_ATTEND) {
            throw new MojException.BusinessRuleViolation(
                "Juror status must be failed to attend in order to undo the failed to attend status.",
                JUROR_STATUS_MUST_BE_FAILED_TO_ATTEND);
        }
        jurorPool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository));
        jurorHistoryService.createUndoFailedToAttendHistory(jurorPool);
        jurorPoolRepository.save(jurorPool);
    }

    @Override
    @Transactional(readOnly = true)
    public JurorAttendanceDetailsResponseDto getJurorAttendanceDetails(String jurorNumber, String poolNumber,
                                                                       BureauJWTPayload payload) {
        log.info("Juror {} attendance record requested by user {}", jurorNumber, payload.getLogin());

        JurorPool jurorPool = getJurorPool(jurorNumber, poolNumber);
        JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, payload.getOwner());

        JurorAttendanceDetailsResponseDto responseDto = new JurorAttendanceDetailsResponseDto();

        // run custom query to return the required data.
        List<JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData> jurorAttendanceDetails =
            getAttendanceData(jurorNumber, poolNumber);
        jurorAttendanceDetails.sort(Comparator
            .comparing(JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData::getAttendanceDate));
        responseDto.setData(jurorAttendanceDetails);

        responseDto.setAbsences((int) jurorAttendanceDetails.stream()
            .filter(p -> AttendanceType.ABSENT.equals(p.getAttendanceType())).count());

        responseDto.setAttendances((int) jurorAttendanceDetails.stream()
            .filter(p -> AttendanceType.FULL_DAY.equals(p.getAttendanceType())
                || AttendanceType.HALF_DAY.equals(p.getAttendanceType())
                || AttendanceType.FULL_DAY_LONG_TRIAL.equals(p.getAttendanceType())
                || AttendanceType.HALF_DAY_LONG_TRIAL.equals(p.getAttendanceType()))
            .count());

        responseDto.setNonAttendances((int) jurorAttendanceDetails.stream()
            .filter(p -> AttendanceType.NON_ATTENDANCE.equals(p.getAttendanceType())).count());

        responseDto.setOnCall(ObjectUtils.defaultIfNull(jurorPool.getOnCall(), false));
        responseDto.setNextDate(jurorPool.getNextDate());

        return responseDto;
    }

    private List<JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData> getAttendanceData(String jurorNumber,
                                                                                                  String poolNumber) {

        List<Appearance> appearances = appearanceRepository.findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber);

        List<JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData> collect = appearances.stream()
            .filter(appearance -> appearance.getAppearanceStage() != null)
            .filter(appearance -> !Set.of(AppearanceStage.CHECKED_IN, AppearanceStage.CHECKED_OUT)
                .contains(appearance.getAppearanceStage()))
            .map(JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData::new)
            .collect(Collectors.toList());

        return collect;
    }

    @Override
    public void editJurorsBankDetails(RequestBankDetailsDto dto) {
        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, dto.getJurorNumber());

        juror.setSortCode(dto.getSortCode());
        juror.setBankAccountNumber(dto.getAccountNumber());
        juror.setBankAccountName(dto.getAccountHolderName());

        jurorRepository.save(juror);

        jurorHistoryService.createEditBankAccountNameHistory(dto.getJurorNumber());
        jurorHistoryService.createEditBankAccountNumberHistory(dto.getJurorNumber());
        jurorHistoryService.createEditBankSortCodeHistory(dto.getJurorNumber());

    }

    private JurorPool getJurorPool(String jurorNumber, String poolNumber) {
        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);
        if (jurorPool == null) {
            throw new MojException.NotFound("Juror number " + jurorNumber + " not found in pool " + poolNumber, null);
        }
        return jurorPool;
    }
}

