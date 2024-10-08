package uk.gov.hmcts.juror.api.moj.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.bureau.service.BureauService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.ConfirmIdentityDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ContactLogRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EditJurorRecordRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.FilterableJurorDetailsRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAddressDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorManualCreationRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNameDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorOpticRefRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorRecordFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoliceCheckStatusDto;
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
import uk.gov.hmcts.juror.api.moj.controller.response.juror.JurorHistoryResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.juror.JurorPaymentsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.ContactCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.FilterJurorRecord;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.IContactCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PendingJuror;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.ApprovalDecision;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.PendingJurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.PoolCreateException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactCodeRepository;
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
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCommonRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.ReasonableAdjustmentsRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAuditChangeService;
import uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorResponseUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.FAILED_TO_ATTEND_HAS_ATTENDANCE_RECORD;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.FAILED_TO_ATTEND_HAS_COMPLETION_DATE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_DATE_OF_BIRTH_REQUIRED;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_STATUS_MUST_BE_RESPONDED;
import static uk.gov.hmcts.juror.api.moj.service.PoolCreateService.DISQUALIFIED_ON_SELECTION;
import static uk.gov.hmcts.juror.api.moj.utils.JurorUtils.checkReadAccessForCurrentUser;

/**
 * Juror Record service.
 */
@Slf4j
@Service
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports",
    "PMD.TooManyFields"})
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurorRecordServiceImpl implements JurorRecordService {
    private final ContactCodeRepository contactCodeRepository;
    private final PoolMemberSequenceService poolMemberSequenceService;
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
    private final FinancialAuditService financialAuditService;

    private final JurorResponseCommonRepositoryMod jurorResponseCommonRepositoryMod;

    private final CourtLocationService courtLocationService;
    private final ContactLogRepository contactLogRepository;
    private final JurorDetailRepositoryMod jurorDetailRepositoryMod;
    private final BureauService bureauService;
    private final JurorAuditChangeService jurorAuditChangeService;
    private final PrintDataService printDataService;
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
    private final UserServiceModImpl userServiceModImpl;
    private final PanelRepository panelRepository;
    private final HistoryTemplateService historyTemplateService;
    private final WelshCourtLocationRepository welshCourtLocationRepository;
    private final JurorResponseAuditRepositoryMod jurorResponseAuditRepository;
    private final JurorPoolService jurorPoolService;
    private final JurorThirdPartyService jurorThirdPartyService;

    @Override
    @Transactional
    public void editJurorDetails(BureauJwtPayload payload, EditJurorRecordRequestDto requestDto, String jurorNumber) {
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
        if (requestDto.getThirdParty() == null) {
            jurorThirdPartyService.deleteThirdParty(juror);
        } else {
            jurorThirdPartyService.createOrUpdateThirdParty(juror, requestDto.getThirdParty());
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
        boolean requiresJurorPool = request.getInclude()
            .contains(FilterableJurorDetailsRequestDto.IncludeType.ACTIVE_POOL);

        if (request.getJurorVersion() != null && requiresJurorPool) {
            throw new MojException.BadRequest("Juror version can not be used along side and Active Pool include filter",
                null);
        }

        Juror juror;
        JurorPool jurorPool;
        if (requiresJurorPool) {
            jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, request.getJurorNumber());
            juror = jurorPool.getJuror();
        } else {
            jurorPool = null;
            juror = getJuror(request.getJurorNumber(), request.getJurorVersion());
        }
        FilterableJurorDetailsRequestDto.FilterContext context = new FilterableJurorDetailsRequestDto.FilterContext(
            juror, jurorPool);

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
    public JurorDetailsResponseDto getJurorDetails(BureauJwtPayload payload, String jurorNumber, String locCode) {
        log.info("Retrieving juror record details for juror {} by user {}", jurorNumber, payload.getLogin());
        CourtLocation courtLocation = courtLocationService.getCourtLocation(locCode);
        // should be only one active and editable record
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPool(jurorPoolRepository, jurorNumber, courtLocation);

        // do a check to see if a court user should be able to view this record
        JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, payload.getOwner());

        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
        JurorDetailsResponseDto jurorDetailsResponseDto = new JurorDetailsResponseDto(jurorPool,
            jurorStatusRepository, welshCourtLocationRepository, pendingJurorRepository);

        // need to send reply method and status so front end can determine if edit should be from response or juror
        // record
        if (jurorResponse != null) {
            jurorDetailsResponseDto.setReplyMethod(REPLY_METHOD_ONLINE);
            jurorDetailsResponseDto.setReplyProcessingStatus(jurorResponse.getProcessingStatus().getDescription());
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
    public JurorOverviewResponseDto getJurorOverview(BureauJwtPayload payload, String jurorNumber, String locCode) {
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
            || Objects.equals(jurorPool.getStatus().getStatus(), IJurorStatus.DISQUALIFIED)
            && juror.getSummonsFile() != null
            && juror.getSummonsFile().equals(DISQUALIFIED_ON_SELECTION)) {
            //return just the common details
            return getJurorOverviewResponseDto(jurorPool);
        }

        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);

        if (jurorResponse != null) {
            JurorOverviewResponseDto jurorOverviewResponseDto = getJurorOverviewResponseDto(jurorPool);
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
                JurorOverviewResponseDto jurorOverviewResponseDto = getJurorOverviewResponseDto(jurorPool);

                jurorOverviewResponseDto.setReplyMethod(REPLY_METHOD_PAPER);
                return jurorOverviewResponseDto;
            }
        }

        //send the default response.
        JurorOverviewResponseDto jurorOverviewResponseDto = getJurorOverviewResponseDto(jurorPool);

        jurorOverviewResponseDto.setReplyMethod(REPLY_METHOD_NOT_AVAILABLE);
        return jurorOverviewResponseDto;
    }

    private JurorOverviewResponseDto getJurorOverviewResponseDto(JurorPool jurorPool) {
        return new JurorOverviewResponseDto(jurorPool,
            jurorStatusRepository, panelRepository, appearanceRepository,
            pendingJurorRepository, welshCourtLocationRepository);
    }

    @Override
    public JurorRecordSearchDto searchJurorRecord(BureauJwtPayload payload, String jurorNumber) {
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
    public void createJurorRecord(BureauJwtPayload payload, JurorCreateRequestDto jurorCreateRequestDto) {
        log.info("User {} creating a pending Juror record in court location {}", payload.getLogin(),
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
            .addedBy(userServiceModImpl.findUserByUsername(SecurityUtil.getUsername()))
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
            .postcode(DataUtils.toUppercase(jurorAddress.getPostcode()))
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

        PendingJurorStatus pendingJurorStatus;
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

        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(dto.getJurorNumber());
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

    @Override
    @Transactional
    public void createJurorManual(JurorManualCreationRequestDto jurorCreateRequestDto) {

        String locCode = jurorCreateRequestDto.getLocationCode();
        String poolNumber = jurorCreateRequestDto.getPoolNumber();
        log.info("Creating a manual Juror record in pool {}", jurorCreateRequestDto.getPoolNumber());

        PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase(poolNumber, poolRequestRepository);

        // generating juror number as it does not exist in voters table
        String jurorNumber = pendingJurorRepository.generatePendingJurorNumber(locCode);

        if (jurorNumber == null || "null".equals(jurorNumber)) {
            throw new MojException.InternalServerError("Error generating new Juror Number", null);
        }

        Juror juror = createManualJurorRecord(jurorCreateRequestDto, jurorNumber);

        JurorPool jurorPool = createManualJurorPool(poolNumber, poolRequest, juror);

        printSummonsLetter(juror, jurorPool);

        log.info("Created manual juror {} in pool {}", juror.getJurorNumber(), jurorPool.getPoolNumber());

    }

    private void printSummonsLetter(Juror juror, JurorPool jurorPool) {
        printDataService.printSummonsLetter(jurorPool);

        JurorHistory jurorSummonsHistory = JurorHistory.builder()
            .jurorNumber(juror.getJurorNumber())
            .poolNumber(jurorPool.getPoolNumber())
            .createdBy(SecurityUtil.getActiveLogin())
            .historyCode(HistoryCodeMod.PRINT_SUMMONS).build();
        jurorHistoryRepository.save(jurorSummonsHistory);
    }

    private JurorPool createManualJurorPool(String poolNumber, PoolRequest poolRequest, Juror juror) {
        JurorPool jurorPool = new JurorPool();
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);
        jurorPool.setIsActive(true);
        jurorPool.setNextDate(poolRequest.getReturnDate());

        Optional<JurorStatus> jurorStatusOpt = jurorStatusRepository.findById(IJurorStatus.SUMMONED);
        JurorStatus jurorStatus = jurorStatusOpt.orElseThrow(PoolCreateException.InvalidPoolStatus::new);
        jurorPool.setStatus(jurorStatus);

        jurorPool.setOwner(SecurityUtil.getActiveOwner());

        int sequenceNumber = poolMemberSequenceService.getPoolMemberSequenceNumber(poolNumber);
        jurorPool.setPoolSequence(poolMemberSequenceService.leftPadInteger(sequenceNumber));

        jurorPool.setUserEdtq(SecurityUtil.getActiveLogin());
        jurorPool.setLastUpdate(LocalDateTime.now());

        jurorPoolRepository.save(jurorPool);
        return jurorPool;
    }

    private Juror createManualJurorRecord(JurorManualCreationRequestDto jurorCreateRequestDto, String jurorNumber) {
        Juror juror = Juror.builder()
            .jurorNumber(jurorNumber)
            .title(jurorCreateRequestDto.getTitle())
            .firstName(jurorCreateRequestDto.getFirstName())
            .lastName(jurorCreateRequestDto.getLastName())
            .dateOfBirth(jurorCreateRequestDto.getDateOfBirth())
            .addressLine1(jurorCreateRequestDto.getAddress().getLineOne())
            .addressLine2(jurorCreateRequestDto.getAddress().getLineTwo())
            .addressLine3(jurorCreateRequestDto.getAddress().getLineThree())
            .addressLine4(jurorCreateRequestDto.getAddress().getTown())
            .addressLine5(jurorCreateRequestDto.getAddress().getCounty())
            .postcode(jurorCreateRequestDto.getAddress().getPostcode())
            .phoneNumber(jurorCreateRequestDto.getPrimaryPhone())
            .altPhoneNumber(jurorCreateRequestDto.getAlternativePhone())
            .email(jurorCreateRequestDto.getEmailAddress())
            .notes(jurorCreateRequestDto.getNotes())
            .responded(false)
            .contactPreference(null)
            .build();

        jurorRepository.save(juror);
        return juror;
    }


    private void validateOnCall(JurorPool jurorPool) {
        if (jurorPool.isOnCall()) {
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
            .postcode(DataUtils.toUppercase(pendingJuror.getPostcode()))
            .phoneNumber(pendingJuror.getPhoneNumber())
            .altPhoneNumber(pendingJuror.getAltPhoneNumber())
            .email(pendingJuror.getEmail())
            .notes(pendingJuror.getNotes())
            .responded(true)
            .responseEntered(true)
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


    private PoolRequest createNewPoolFromDto(JurorCreateRequestDto jurorCreateRequestDto, BureauJwtPayload payload) {
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
    public ContactLogListDto getJurorContactLogs(BureauJwtPayload payload, String jurorNumber) {
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
            jurorStatusRepository, pendingJurorRepository, welshCourtLocationRepository));
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
    public void createJurorContactLog(BureauJwtPayload payload, ContactLogRequestDto contactLogRequestDto) {
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository,
            contactLogRequestDto.getJurorNumber(), payload.getOwner());
        // check whether the current user has permissions to create new contact logs against the currently active
        // juror record
        if (!("400".equals(payload.getOwner()) || jurorPool.getOwner().equals(payload.getOwner()))) {
            throw new MojException.Forbidden("Current user does not have sufficient permission to "
                + "view the juror pool record(s)", null);
        }

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
        List<ContactEnquiryTypeListDto.ContactEnquiry> enquiryTypes = new ArrayList<>();
        contactCodeRepository.findAll()
            .forEach(code -> enquiryTypes.add(ContactEnquiryTypeListDto.ContactEnquiry.from(code)));
        return new ContactEnquiryTypeListDto(enquiryTypes);
    }

    @Override
    public JurorNotesDto getJurorNotes(String jurorNumber, String owner) {
        checkReadAccessForCurrentUser(jurorPoolRepository, jurorNumber, owner);

        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber, owner);
        Juror juror = jurorPool.getJuror();

        return new JurorNotesDto(juror.getNotes(), new JurorDetailsCommonResponseDto(jurorPool, jurorStatusRepository,
            pendingJurorRepository, welshCourtLocationRepository));
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
    public void createJurorOpticReference(BureauJwtPayload payload, JurorOpticRefRequestDto opticsRefRequestDto) {
        final String jurorNumber = opticsRefRequestDto.getJurorNumber();
        final String poolNumber = opticsRefRequestDto.getPoolNumber();

        log.info("Creating an Optics reference for Juror {} in pool {} by user {}", jurorNumber, poolNumber,
            payload.getLogin());

        AbstractJurorResponse response =
            jurorResponseCommonRepositoryMod.findByJurorNumber(opticsRefRequestDto.getJurorNumber());

        if (response == null) {
            throw new MojException.NotFound("Cannot find juror response record for juror "
                + opticsRefRequestDto.getJurorNumber(), null);
        }

        if (response.getProcessingComplete().equals(true) || response.getProcessingStatus()
            .equals(ProcessingStatus.CLOSED)) {
            throw new MojException.BusinessRuleViolation("Cannot check court accommodation - Response has been "
                + "completed/closed", null);
        }

        final String opticsRef = opticsRefRequestDto.getOpticReference();
        final String owner = payload.getOwner();

        Juror juror = jurorRepository.findById(jurorNumber).orElseThrow(() ->
            new MojException.NotFound(String.format("Unable to find valid juror record for Juror Number: %s",
                jurorNumber), null));

        // only allow access if the owner of record is same as users owner
        JurorUtils.checkOwnershipForCurrentUser(juror, owner);
        juror.setOpticRef(opticsRef);
        jurorRepository.save(juror);
        response.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.AWAITING_COURT_REPLY);

        if (ReplyMethod.DIGITAL.getDescription().equals(response.getReplyType().getType())) {
            jurorResponseRepository.save((DigitalResponse) response);
            log.debug("Finished adding optics reference for juror {}", jurorNumber);
        } else {
            jurorPaperResponseRepository.save((PaperResponse) response);
            log.debug("Finished adding optics reference for juror {}", jurorNumber);
        }
    }

    @Override
    public String getJurorOpticReference(String jurorNumber, String poolNumber, BureauJwtPayload payload) {

        log.info("Retrieving an Optics reference for Juror {} in pool {} by user {}", jurorNumber, poolNumber,
            payload.getLogin());

        final String owner = payload.getOwner();

        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);

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
            .orElseThrow(() -> new MojException.NotFound(String.format("Could not find juror details for %s",
                jurorNumber), null));

        BureauJurorDetailDto responseDto = bureauService.mapJurorDetailsToDto(jurorDetails);
        responseDto.setWelshCourt(jurorDetails.isWelshCourt());

        // set the current owner.  Need to ensure the current owner is returned as the owner can change if, for
        // example, the juror is transferred to a different pool
        JurorResponseUtils.updateCurrentOwnerInResponseDto(jurorPoolRepository, responseDto);

        return responseDto;
    }

    @Override
    @Transactional
    public JurorSummonsReplyResponseDto getJurorSummonsReply(BureauJwtPayload payload, String jurorNumber,
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
            return new JurorSummonsReplyResponseDto(jurorPool, jurorStatusRepository, welshCourtLocationRepository,
                pendingJurorRepository);
        }


        // check if there is a digital reply from user
        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
        // check if there is a paper reply from user
        PaperResponse jurorPaperResponse = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);

        if (Objects.equals(jurorPool.getStatus().getStatus(), IJurorStatus.SUMMONED)
            && jurorResponse == null && jurorPaperResponse == null) {
            return new JurorSummonsReplyResponseDto(jurorPool, jurorStatusRepository, welshCourtLocationRepository,
                pendingJurorRepository);
        }

        if (jurorResponse != null) {
            JurorSummonsReplyResponseDto jurorSummonsReplyResponseDto = new JurorSummonsReplyResponseDto(jurorPool,
                jurorStatusRepository, welshCourtLocationRepository, pendingJurorRepository);
            jurorSummonsReplyResponseDto.setReplyMethod(REPLY_METHOD_ONLINE);
            jurorSummonsReplyResponseDto.setReplyDate(jurorResponse.getDateReceived().toLocalDate());
            jurorSummonsReplyResponseDto.setReplyStatus(jurorResponse.getProcessingStatus().getDescription());
            return jurorSummonsReplyResponseDto;
        }

        if (jurorPaperResponse != null) {
            JurorSummonsReplyResponseDto jurorSummonsReplyResponseDto = new JurorSummonsReplyResponseDto(jurorPool,
                jurorStatusRepository, welshCourtLocationRepository, pendingJurorRepository);
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
                    new JurorSummonsReplyResponseDto(jurorPool, jurorStatusRepository, welshCourtLocationRepository,
                        pendingJurorRepository);
                jurorSummonsReplyResponseDto.setReplyMethod(REPLY_METHOD_PAPER);
                return jurorSummonsReplyResponseDto;
            }
        }

        //send the default response
        JurorSummonsReplyResponseDto jurorSummonsReplyResponseDto = new JurorSummonsReplyResponseDto(jurorPool,
            jurorStatusRepository, welshCourtLocationRepository, pendingJurorRepository);

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
    public void fixErrorInJurorName(BureauJwtPayload payload, String jurorNumber,
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
    public void processPendingNameChange(BureauJwtPayload payload, String jurorNumber,
                                         ProcessNameChangeRequestDto requestDto) {
        log.trace("Enter processPendingNameChange");

        final String username = payload.getLogin();

        final String changeOfNameCode = "CN";
        final String contactLogNotes = WordUtils.capitalize(requestDto.getDecision().getDescription())
            + " the juror's name change. " + requestDto.getNotes();

        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
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

    @Override
    @Transactional
    public PoliceCheckStatusDto updatePncStatus(final String jurorNumber, final PoliceCheck policeCheck) {
        log.info("Attempting to update PNC check status for juror {} to be {}", jurorNumber, policeCheck);
        final JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
        final Juror juror = jurorPool.getJuror();

        final PoliceCheck oldPoliceCheckValue = juror.getPoliceCheck();
        final PoliceCheck newPoliceCheckValue = PoliceCheck.getEffectiveValue(oldPoliceCheckValue, policeCheck);
        if (oldPoliceCheckValue == newPoliceCheckValue) {
            log.debug("Skipping PNC check update for juror {} as new value equals existing value", jurorNumber);
            return new PoliceCheckStatusDto(policeCheck);//If both are the same no point continuing
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
            if (SecurityUtil.BUREAU_OWNER.equals(jurorPool.getOwner())) {
                printDataService.printConfirmationLetter(jurorPool);
                jurorHistoryService.createConfirmationLetterHistory(jurorPool, "Confirmation Letter Auto");
            }
        } else if (newPoliceCheckValue == PoliceCheck.INELIGIBLE) {
            log.debug("Juror {} is ineligible disqualifying for police check", jurorNumber);
            jurorPool.setStatus(
                RepositoryUtils.retrieveFromDatabase(IJurorStatus.DISQUALIFIED, jurorStatusRepository));
            juror.setDisqualifyCode(DisCode.ELECTRONIC_POLICE_CHECK_FAILURE);
            juror.setDisqualifyDate(LocalDate.now(clock));

            jurorHistoryService.createPoliceCheckDisqualifyHistory(jurorPool);
            if (SecurityUtil.BUREAU_OWNER.equals(jurorPool.getOwner())) {
                printDataService.printWithdrawalLetter(jurorPool);
                jurorHistoryService.createWithdrawHistory(jurorPool, "Withdrawal Letter Auto", "E");
            }
        } else if (newPoliceCheckValue == PoliceCheck.IN_PROGRESS) {
            log.debug("Juror {} police check is in progress adding part history", jurorNumber);
            jurorHistoryService.createPoliceCheckInProgressHistory(jurorPool);
        } else if (newPoliceCheckValue == PoliceCheck.INSUFFICIENT_INFORMATION) {
            log.debug("Juror {} police check has insufficient information adding part history", jurorNumber);
            jurorHistoryService.createPoliceCheckInsufficientInformationHistory(jurorPool);
        }
        jurorPoolRepository.save(jurorPool);
        jurorRepository.save(juror);
        return new PoliceCheckStatusDto(newPoliceCheckValue);
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

        if (jurorAppearanceService.hasAttendances(jurorNumber)) {
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
    @Transactional(readOnly = true)
    public JurorAttendanceDetailsResponseDto getJurorAttendanceDetails(String locCode, String jurorNumber,
                                                                       BureauJwtPayload payload) {
        log.info("Juror {} attendance record requested by user {}", jurorNumber, payload.getLogin());

        SecurityUtil.validateCourtLocationPermitted(locCode);
        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
        JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, payload.getOwner());

        JurorAttendanceDetailsResponseDto responseDto = new JurorAttendanceDetailsResponseDto();

        // run custom query to return the required data.
        List<JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData> jurorAttendanceDetails =
            getAttendanceData(locCode, jurorNumber);
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

        responseDto.setAppearances((int) jurorAttendanceDetails.stream()
            .filter(p -> (p.getCheckInTime() != null) || (p.getCheckOutTime() != null)).count());

        responseDto.setOnCall(jurorPool.isOnCall());
        responseDto.setNextDate(jurorPool.getNextDate());

        return responseDto;
    }


    private List<JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData> getAttendanceData(String locCode,
                                                                                                  String jurorNumber) {
        List<Appearance> appearances = appearanceRepository
            .findAllByCourtLocationLocCodeAndJurorNumber(locCode, jurorNumber);

        return appearances.stream()
            .filter(appearance -> appearance.getAppearanceStage() == null || !Set.of(AppearanceStage.CHECKED_IN,
                AppearanceStage.CHECKED_OUT).contains(appearance.getAppearanceStage()))
            .map(JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData::new)
            .collect(Collectors.toList());
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    private static class PaymentSummaryData {
        BigDecimal financialLoss = BigDecimal.ZERO;
        BigDecimal travel = BigDecimal.ZERO;
        BigDecimal subsistence = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;

        PaymentSummaryData add(PaymentSummaryData target) {
            financialLoss = financialLoss.add(BigDecimalUtils.getOrZero(target.getFinancialLoss()));
            travel = travel.add(BigDecimalUtils.getOrZero(target.getTravel()));
            subsistence = subsistence.add(BigDecimalUtils.getOrZero(target.getSubsistence()));
            paid = paid.add(BigDecimalUtils.getOrZero(target.getPaid()));

            return this;
        }
    }

    @Override
    public JurorPaymentsResponseDto getJurorPayments(String jurorNumber) {

        List<Appearance> appearances =
            appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumberAndAppearanceStageIn(
                SecurityUtil.getLocCode(),
                jurorNumber,
                AppearanceStage.getConfirmedAppearanceStages()
            );


        Collection<AttendanceType> nonAttendanceTypes = AttendanceType.getNonAttendanceTypes();

        int nonAttendanceCount = (int) appearances.stream().filter(
            appearance -> nonAttendanceTypes.contains(appearance.getAttendanceType())
        ).count();

        PaymentSummaryData summaryData = appearances.stream()
            .filter(appearance -> !appearance.isDraftExpense())
            .reduce(
                new PaymentSummaryData(),
                (total, item) -> total.add(new PaymentSummaryData(
                    item.getTotalFinancialLossDue(),
                    item.getTotalTravelDue(),
                    item.getSubsistenceDue(),
                    item.getTotalPaid())),
                PaymentSummaryData::add
            );

        Map<Long, Optional<FinancialAuditDetails>> auditDetailsMap = new ConcurrentHashMap<>();
        return JurorPaymentsResponseDto.builder()
            .attendances(appearances.size() - nonAttendanceCount)
            .nonAttendances(nonAttendanceCount)
            .financialLoss(BigDecimalUtils.getOrZero(summaryData.getFinancialLoss()))
            .travel(BigDecimalUtils.getOrZero(summaryData.getTravel()))
            .subsistence(BigDecimalUtils.getOrZero(summaryData.getSubsistence()))
            .totalPaid(BigDecimalUtils.getOrZero(summaryData.getPaid()))
            .data(appearances.stream().map(appearance -> {
                JurorPaymentsResponseDto.PaymentDayDto.PaymentDayDtoBuilder day =
                    JurorPaymentsResponseDto.PaymentDayDto.builder()
                        .attendanceDate(appearance.getAttendanceDate())
                        .attendanceAudit(appearance.getAttendanceAuditNumber());

                if (!appearance.isDraftExpense()) {
                    day.travel(appearance.getTotalTravelDue())
                        .financialLoss(appearance.getTotalFinancialLossDue())
                        .subsistence(BigDecimalUtils.getOrZero(appearance.getSubsistenceDue()))
                        .smartcard(BigDecimalUtils.getOrZero(appearance.getSmartCardAmountDue()))
                        .totalDue(appearance.getTotalDue())
                        .totalPaid(appearance.getTotalPaid());

                    if (appearance.getFinancialAudit() != null) {
                        final Optional<FinancialAuditDetails> financialAuditDetailsOptional;
                        if (!auditDetailsMap.containsKey(appearance.getFinancialAudit())) {
                            financialAuditDetailsOptional =
                                financialAuditService.getLastFinancialAuditDetailsFromAppearanceAndGenericType(
                                    appearance, FinancialAuditDetails.Type.GenericType.APPROVED);
                            auditDetailsMap.put(appearance.getFinancialAudit(), financialAuditDetailsOptional);
                        } else {
                            financialAuditDetailsOptional = auditDetailsMap.get(appearance.getFinancialAudit());
                        }
                        if (financialAuditDetailsOptional.isPresent()) {
                            FinancialAuditDetails financialAuditDetails = financialAuditDetailsOptional.get();
                            day.paymentAudit(FinancialAuditDetails.F_AUDIT_PREFIX + financialAuditDetails.getId());
                            day.datePaid(financialAuditDetails.getCreatedOn());
                        }
                    }
                }
                return day.build();
            }).toList())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public JurorHistoryResponseDto getJurorHistory(String jurorNumber) {
        checkReadAccessForCurrentUser(jurorPoolRepository, jurorNumber, SecurityUtil.getActiveOwner());
        List<JurorHistory> data = jurorHistoryRepository.findByJurorNumberOrderById(jurorNumber);
        return JurorHistoryResponseDto.builder()
            .data(data.stream()
                .map(historyTemplateService::toJurorHistoryEntryDto)
                .toList())
            .build();
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

    @Override
    @Transactional
    public void confirmIdentity(ConfirmIdentityDto dto) {
        log.info("Confirming identity for juror {}", dto.getJurorNumber());

        // confirm user has access to the juror record and get jurorPool record
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, dto.getJurorNumber(),
            SecurityUtil.getActiveOwner());

        jurorPool.setIdChecked(dto.getIdCheckCode().getCode());
        jurorPoolRepository.save(jurorPool);

        jurorHistoryService.createIdentityConfirmedHistory(jurorPool);
    }

    @Override
    @Transactional
    public void markResponded(String jurorNumber) {
        log.info("Marking juror {} as responded", jurorNumber);

        final JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber,
            SecurityUtil.getActiveOwner());
        final Juror juror = jurorPool.getJuror();

        if (null == juror.getDateOfBirth()) {
            throw new MojException.BusinessRuleViolation("Juror date of birth is required to mark as responded",
                JUROR_DATE_OF_BIRTH_REQUIRED);
        }

        final String auditorUsername = SecurityUtil.getActiveLogin();
        juror.setResponded(true);
        jurorRepository.save(juror);
        jurorPool.setUserEdtq(auditorUsername);
        jurorPool.setNextDate(jurorPool.getPool().getReturnDate());
        jurorPool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository));
        jurorPoolRepository.save(jurorPool);

        final JurorHistory history = JurorHistory.builder()
            .jurorNumber(jurorNumber)
            .historyCode(HistoryCodeMod.RESPONDED_POSITIVELY)
            .createdBy(auditorUsername)
            .otherInformation(JurorHistory.RESPONDED)
            .poolNumber(jurorPool.getPoolNumber())
            .dateCreated(LocalDateTime.now())
            .build();
        jurorHistoryRepository.save(history);
    }

    @Override
    public PaginatedList<FilterJurorRecord> searchForJurorRecords(JurorRecordFilterRequestQuery query) {

        return PaginationUtil.toPaginatedList(
            jurorRepository.fetchFilteredJurorRecords(query),
            query,
            query.getSortField(),
            query.getSortMethod(),
            tuple -> {
                FilterJurorRecord.FilterJurorRecordBuilder builder = FilterJurorRecord.builder()
                    .jurorNumber(tuple.get(QJuror.juror.jurorNumber))
                    .jurorName(tuple.get(jurorRepository.JUROR_FULL_NAME))
                    .postcode(tuple.get(QJuror.juror.postcode))
                    .poolNumber(tuple.get(QJurorPool.jurorPool.pool.poolNumber))
                    .courtName(tuple.get(QJurorPool.jurorPool.pool.courtLocation.name))
                    .status(tuple.get(QJurorPool.jurorPool.status.statusDesc))
                    .locCode(tuple.get(QJurorPool.jurorPool.pool.courtLocation.locCode));
                return builder.build();
            },
            ValidationConstants.MAX_ITEMS
        );
    }

    private JurorPool getJurorPool(String jurorNumber, String poolNumber) {
        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);
        if (jurorPool == null) {
            throw new MojException.NotFound("Juror number " + jurorNumber + " not found in pool " + poolNumber, null);
        }
        return jurorPool;
    }
}
