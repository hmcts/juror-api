package uk.gov.hmcts.juror.api.moj.service.trial;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.EndTrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.ReturnJuryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.CourtroomsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialSummaryDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.JudgeRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.service.AppearanceCreationService;
import uk.gov.hmcts.juror.api.moj.service.CompleteServiceService;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.utils.PanelUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.CANNOT_EDIT_COMPLETED_TRIAL;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.CANNOT_EDIT_TRIAL_WITH_JURORS;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.TRIAL_HAS_MEMBERS;

@Slf4j
@Service
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TrialServiceImpl implements TrialService {

    private final TrialRepository trialRepository;
    private final JudgeRepository judgeRepository;
    private final CourtroomRepository courtroomRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final PanelRepository panelRepository;
    private final AppearanceRepository appearanceRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final CompleteServiceService completeService;
    private final JurorAppearanceService jurorAppearanceService;
    private final JurorHistoryService jurorHistoryService;
    private final AppearanceCreationService appearanceCreationService;

    private static final String CANNOT_FIND_TRIAL_ERROR_MESSAGE = "Cannot find trial with number: %s for "
        + "court location %s";

    @Override
    @Transactional
    public TrialSummaryDto createTrial(BureauJwtPayload payload, TrialDto trialDto) {

        if (trialRepository.existsByTrialNumberAndCourtLocationLocCode(
            trialDto.getCaseNumber(), trialDto.getCourtLocation())) {
            throw new MojException.BadRequest(String.format("Unable to create trial with case number: %s at "
                    + "location code %s (case number already in use at this "
                    + "location)",
                trialDto.getCaseNumber(),
                trialDto.getCourtLocation()), null);
        }

        Courtroom courtroom =
            RepositoryUtils.unboxOptionalRecord(courtroomRepository.findById(trialDto.getCourtroomId()),
                trialDto.getCourtroomId().toString(), "Courtroom");
        if (!courtroom.getCourtLocation().getLocCode().equals(trialDto.getCourtLocation())) {
            throw new MojException.BadRequest("Courtroom does not belong to the court location", null);
        }
        CourtLocation courtLocation =
            RepositoryUtils.unboxOptionalRecord(courtLocationRepository.findByLocCode(trialDto.getCourtLocation()),
                trialDto.getCourtLocation(), "Court Location");
        Judge judge =
            RepositoryUtils.unboxOptionalRecord(judgeRepository.findById(trialDto.getJudgeId()),
                trialDto.getJudgeId().toString(), "Judge");

        if (!judge.getOwner().equals(courtLocation.getOwner())) {
            throw new MojException.BadRequest("Judge does not belong to the court location", null);
        }

        Trial trial = convertDtoToTrial(trialDto, courtroom, judge, courtLocation);
        trialRepository.save(trial);

        judge.setLastUsed(LocalDateTime.now());
        judgeRepository.save(judge);
        return createTrialSummary(trial, courtroom, judge, false);
    }


    @Override
    @Transactional
    public TrialSummaryDto editTrial(TrialDto trialDto) {

        // check user has access to the court location
        BureauJwtPayload payload = SecurityUtil.getActiveUsersBureauPayload();
        if (!payload.getStaff().getCourts().contains(trialDto.getCourtLocation())) {
            throw new MojException.Forbidden("User does not have access to the court location", null);
        }

        // check the trial exists
        Trial trial = trialRepository.findByTrialNumberAndCourtLocationLocCode(trialDto.getCaseNumber(),
                trialDto.getCourtLocation())
            .orElseThrow(() -> new MojException.NotFound(String.format(CANNOT_FIND_TRIAL_ERROR_MESSAGE,
                trialDto.getCaseNumber(), trialDto.getCourtLocation()), null));

        // check the trial is not completed
        if (trial.getTrialEndDate() != null) {
            throw new MojException.BusinessRuleViolation("Cannot edit a completed trial",
                CANNOT_EDIT_COMPLETED_TRIAL);
        }

        // cannot edit a trial with jurors or panel members
        if (hasJurorsOnTrial(trial)) {
            throw new MojException.BusinessRuleViolation("Cannot edit a trial with jurors or panel members",
                CANNOT_EDIT_TRIAL_WITH_JURORS);
        }

        // update the trial
        trial.setJudge(judgeRepository.findById(trialDto.getJudgeId())
            .orElseThrow(() -> new MojException.NotFound("Cannot find judge with id: %s"
                .formatted(trialDto.getJudgeId()), null)));
        trial.setDescription(trialDto.getDefendant());
        trial.setAnonymous(trialDto.isProtectedTrial());
        trial.setTrialStartDate(trialDto.getStartDate());
        trial.setTrialType(trialDto.getTrialType());
        trial.setCourtroom(courtroomRepository.findById(trialDto.getCourtroomId())
            .orElseThrow(() -> new MojException.NotFound("Cannot find courtroom with id: %s"
                .formatted(trialDto.getCourtroomId()), null)));

        if (!trial.getCourtLocation().getLocCode().equals(trialDto.getCourtLocation())) {
            throw new MojException.BadRequest("Courtroom does not belong to the court location", null);
        }
        if (!trial.getJudge().getOwner().equals(trial.getCourtLocation().getOwner())) {
            throw new MojException.BadRequest("Judge does not belong to the court location", null);
        }

        trialRepository.save(trial);

        return createTrialSummary(trial, trial.getCourtroom(), trial.getJudge(), false);
    }

    private boolean hasJurorsOnTrial(Trial trial) {

        List<Panel> panelList = panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(
            trial.getTrialNumber(),
            trial.getCourtLocation().getLocCode());

        return !panelList.isEmpty();
    }

    @Override
    public PaginatedList<TrialListDto> getTrials(TrialSearch trialSearch) {
        return trialRepository.getListOfTrials(trialSearch, this::createTrailListDto);
    }

    @Override
    public TrialSummaryDto getTrialSummary(BureauJwtPayload payload, String trialNo, String locCode) {
        if (!payload.getStaff().getCourts().contains(locCode)) {
            throw new MojException.Forbidden("Current user has insufficient permission "
                + "to view the trial details for the court location", null);
        }

        Trial trial = trialRepository.findByTrialNumberAndCourtLocationLocCode(trialNo, locCode)
            .orElseThrow(() -> new MojException.NotFound(String.format(CANNOT_FIND_TRIAL_ERROR_MESSAGE,
                trialNo, locCode), null));

        return createTrialSummary(trial, trial.getCourtroom(), trial.getJudge(), true);
    }

    @Override
    @Transactional
    public void returnPanel(BureauJwtPayload payload, String trialNo, String locCode,
                            List<JurorDetailRequestDto> jurorDetailRequestDto) {

        // grabs the panel members from the DB and checks to make sure they match the requested members to be returned
        List<Panel> panelList =
            panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNo, locCode);
        List<Panel> panelMembersToReturn = getPanelMembersToReturn(jurorDetailRequestDto, panelList);

        log.debug(String.format("found %d panel members to be returned", panelMembersToReturn.size()));

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);
        for (Panel panel : panelMembersToReturn) {
            panel.setResult(PanelResult.RETURNED);
            panel.setCompleted(true);
            panelRepository.saveAndFlush(panel);

            JurorPool jurorPool = PanelUtils.getAssociatedJurorPool(jurorPoolRepository, panel);
            if (jurorPool.getStatus().getStatus() == IJurorStatus.PANEL) {
                jurorPool.setStatus(jurorStatus);
                jurorPoolRepository.saveAndFlush(jurorPool);
            }

            log.debug(String.format("updated juror trial record for juror %s", panel.getJurorNumber()));
            jurorHistoryService.createReturnFromPanelHistory(jurorPool, panel);
            log.debug(String.format("saved history item for juror %s", panel.getJurorNumber()));
        }
    }

    @Override
    @Transactional
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void returnJury(BureauJwtPayload payload, String trialNumber, String locationCode,
                           ReturnJuryDto returnJuryDto) {
        List<Panel> panelList =
            panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, locationCode);

        List<Panel> juryMembersToBeReturned = getPanelMembersToReturn(returnJuryDto.getJurors(), panelList);

        log.info(String.format("found %d jury members to be returned", juryMembersToBeReturned.size()));


        // get the next available attendance number from the database sequence
        final long attendanceAuditNumber = appearanceRepository.getNextAttendanceAuditNumber();

        for (Panel panel : juryMembersToBeReturned) {

            final String jurorNumber = panel.getJurorNumber();
            JurorPool jurorPool = PanelUtils.getAssociatedJurorPool(jurorPoolRepository, panel);

            if (jurorPool.getStatus().getStatus() == IJurorStatus.JUROR
                && StringUtils.isNotEmpty(returnJuryDto.getCheckIn())) {

                Appearance appearance =
                    getJurorAppearanceForDate(jurorPool, returnJuryDto.getAttendanceDate(), locationCode);

                // only apply check in time for those that have not been checked in yet
                if (appearance.getTimeIn() == null) {
                    appearance.setAppearanceStage(AppearanceStage.CHECKED_IN);
                    appearance.setTimeIn(LocalTime.parse(returnJuryDto.getCheckIn()));
                    log.debug("setting time in for juror %s".formatted(jurorNumber));
                }

                if (StringUtils.isNotEmpty(returnJuryDto.getCheckOut())) {
                    if (appearance.getTimeOut() == null) {
                        appearance.setAppearanceStage(AppearanceStage.EXPENSE_ENTERED);
                        appearance.setTimeOut(LocalTime.parse(returnJuryDto.getCheckOut()));
                        log.debug("setting time out for juror %s".formatted(jurorNumber));
                    }
                    if (appearance.getAttendanceAuditNumber() == null) {
                        //Only give them an attendance number if they were checked out via this process
                        appearance.setAttendanceAuditNumber("J" + attendanceAuditNumber);
                        jurorHistoryService.createJuryAttendanceHistory(jurorPool, appearance, panel);
                    }
                }
                appearance.setTrialNumber(trialNumber);
                appearance.setSatOnJury(true);
                jurorAppearanceService.realignAttendanceType(appearance);
            }

            panel.setResult(PanelResult.RETURNED);
            panel.setCompleted(true);
            panel.setReturnDate(LocalDate.now());
            panelRepository.saveAndFlush(panel);

            if (jurorPool.getStatus().getStatus() == IJurorStatus.JUROR
                || jurorPool.getStatus().getStatus() == IJurorStatus.PANEL) {
                JurorStatus jurorStatus = new JurorStatus();
                jurorStatus.setStatus(IJurorStatus.RESPONDED);
                jurorPool.setStatus(jurorStatus);
            }

            log.debug(String.format("updated juror trial record for juror %s", jurorNumber));
            jurorHistoryService.createReturnFromPanelHistory(jurorPool, panel);
            log.debug(String.format(String.format("saved history item for juror %s", jurorNumber)));

            if (Boolean.TRUE.equals(returnJuryDto.getCompleted())) {
                completeService.completeServiceSingle(jurorPool, LocalDate.now());
            }
        }
    }

    @Override
    @Transactional
    public void endTrial(EndTrialDto dto) {
        List<Panel> panelList =
            panelRepository.retrieveMembersOnTrial(dto.getTrialNumber(), dto.getLocationCode());

        if (!panelList.isEmpty()) {
            throw new MojException.BusinessRuleViolation(
                "Cannot end trial, trial still has members",
                TRIAL_HAS_MEMBERS);
        }

        Trial trial = trialRepository
            .findByTrialNumberAndCourtLocationLocCode(dto.getTrialNumber(), dto.getLocationCode())
            .orElseThrow(() -> new MojException.NotFound(String.format(CANNOT_FIND_TRIAL_ERROR_MESSAGE,
                dto.getTrialNumber(), dto.getLocationCode()), null));


        trial.setTrialEndDate(dto.getTrialEndDate());
        log.info("trial {} has been completed", trial.getTrialNumber());
        trialRepository.save(trial);
    }

    private List<Panel> getPanelMembersToReturn(List<JurorDetailRequestDto> jurorList, List<Panel> panelList) {
        List<Panel> panelMembersToReturn = new ArrayList<>();
        for (Panel panel : panelList) {
            for (JurorDetailRequestDto dto : jurorList) {
                if (dto.getJurorNumber().equals(panel.getJurorNumber())) {
                    panelMembersToReturn.add(panel);
                    break;
                }
            }
        }
        return panelMembersToReturn;
    }

    private TrialListDto createTrailListDto(Trial trial) {
        TrialListDto dto = new TrialListDto();
        dto.setCourtroom(trial.getCourtroom().getDescription());
        dto.setTrialNumber(trial.getTrialNumber());
        dto.setJudge(trial.getJudge().getName());
        dto.setDefendants(trial.getDescription());
        dto.setStartDate(trial.getTrialStartDate());
        dto.setIsActive(trial.getTrialEndDate() == null);
        dto.setTrialType(trial.getTrialType().getDescription());
        dto.setCourtLocationName(trial.getCourtLocation().getName());
        dto.setCourtLocationCode(trial.getCourtLocation().getLocCode());
        return dto;
    }

    private TrialSummaryDto createTrialSummary(Trial trial, Courtroom courtroom, Judge judge, boolean panelCheck) {
        TrialSummaryDto dto = new TrialSummaryDto();
        dto.setCourtroomsDto(convertCourtroomEntityToDto(courtroom));
        dto.setJudge(convertJudgeEntityToDto(judge));
        dto.setTrialType(trial.getTrialType().getDescription());
        dto.setTrialNumber(trial.getTrialNumber());
        dto.setDefendants(trial.getDescription());
        dto.setProtectedTrial(trial.getAnonymous());
        dto.setTrialStartDate(trial.getTrialStartDate());
        dto.setIsActive(trial.getTrialEndDate() == null);
        dto.setTrialEndDate(trial.getTrialEndDate());
        dto.setCourtRoomLocationName(trial.getCourtroom().getCourtLocation().getName());

        if (panelCheck) {
            boolean isJuryEmpanelled = isJuryEmpanelledOnTrial(trial);
            dto.setIsJuryEmpanelled(isJuryEmpanelled);
        }

        return dto;
    }

    private boolean isJuryEmpanelledOnTrial(Trial trial) {
        List<Panel> panelList = panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(
            trial.getTrialNumber(),
            trial.getCourtLocation().getLocCode());

        return panelList.stream().anyMatch(panel -> panel.getResult() == PanelResult.JUROR);
    }

    private JudgeDto convertJudgeEntityToDto(Judge judge) {
        JudgeDto dto = new JudgeDto();
        dto.setCode(judge.getCode());
        dto.setId(judge.getId());
        dto.setDescription(judge.getName());
        return dto;
    }

    private CourtroomsDto convertCourtroomEntityToDto(Courtroom courtroom) {
        CourtroomsDto dto = new CourtroomsDto();
        dto.setDescription(courtroom.getDescription());
        dto.setOwner(courtroom.getCourtLocation().getOwner());
        dto.setLocCode(courtroom.getCourtLocation().getLocCode());
        dto.setId(courtroom.getId());
        dto.setRoomNumber(courtroom.getRoomNumber());
        return dto;
    }

    private Trial convertDtoToTrial(TrialDto dto, Courtroom courtroom, Judge judge, CourtLocation courtLocation) {
        Trial trial = new Trial();
        trial.setTrialNumber(dto.getCaseNumber());
        trial.setTrialStartDate(dto.getStartDate());
        trial.setDescription(dto.getDefendant());
        trial.setAnonymous(dto.isProtectedTrial());
        trial.setCourtroom(courtroom);
        trial.setJudge(judge);
        trial.setCourtLocation(courtLocation);
        trial.setTrialType(dto.getTrialType());
        return trial;
    }

    private Appearance getJurorAppearanceForDate(JurorPool jurorPool, LocalDate attendanceDate, String locCode) {

        final String jurorNumber = jurorPool.getJurorNumber();
        log.debug(String.format("Check for an appearance record for Juror: %s on %s", jurorNumber, attendanceDate));
        Optional<Appearance> appearanceOpt = jurorAppearanceService
            .getAppearance(jurorNumber, attendanceDate, locCode);
        log.debug(String.format("Appearance record for Juror: %s on %s at %s %s",
            jurorNumber, attendanceDate, locCode,
            appearanceOpt.isPresent()
                ? "already exists"
                : "could not be found"));

        return appearanceOpt.orElse(
            appearanceCreationService.createAppearance(
                jurorNumber,
                attendanceDate,
                jurorPool.getPool().getCourtLocation(),
                jurorPool.getPool().getPoolNumber(),
                false));
    }
}
