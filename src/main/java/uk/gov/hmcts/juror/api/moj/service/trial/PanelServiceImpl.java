package uk.gov.hmcts.juror.api.moj.service.trial;

import com.querydsl.core.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.AvailableJurorsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.EmpanelDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.EmpanelListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.PanelListDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.JurorHistoryUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.NO_PANEL_EXIST;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.NUMBER_OF_JURORS_EXCEEDS_AVAILABLE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.NUMBER_OF_JURORS_EXCEEDS_LIMITS;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.TRIAL_HAS_ENDED;

@Slf4j
@Service
@SuppressWarnings("PMD.TooManyMethods")
public class PanelServiceImpl implements PanelService {

    @Autowired
    private AppearanceRepository appearanceRepository;

    @Autowired
    private PanelRepository panelRepository;

    @Autowired
    private TrialRepository trialRepository;

    @Autowired
    private JurorPoolRepository jurorPoolRepository;

    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;

    private static final int MAX_PANEL_MEMBERS = 1000;

    @Override
    public List<AvailableJurorsDto> getAvailableJurors(String courtLocation) {
        return processQueryResults(appearanceRepository.getAvailableJurors(courtLocation));
    }

    @Override
    public List<PanelListDto> createPanel(int numberRequested, String trialNumber,
                                          Optional<List<String>> poolNumbers, String courtLocationCode,
                                          BureauJwtPayload payload) {
        createPanelValidationChecks(numberRequested, trialNumber, courtLocationCode);
        List<JurorPool> appearanceList = buildRandomJurorPoolList(poolNumbers, trialNumber);
        return processPanelList(numberRequested, trialNumber, courtLocationCode, payload, appearanceList);
    }

    public List<PanelListDto> addPanelMembers(int numberRequested, String trialNumber,
                                              Optional<List<String>> poolNumbers, String courtLocationCode,
                                              BureauJwtPayload payload) {
        addPanelMembersValidationChecks(numberRequested, trialNumber, courtLocationCode);
        List<JurorPool> appearanceList = buildRandomJurorPoolList(poolNumbers, trialNumber);
        return processPanelList(numberRequested, trialNumber, courtLocationCode, payload, appearanceList);

    }

    private void createPanelValidationChecks(int numberRequested, String trialNumber, String courtLocationCode) {
        if (!trialRepository.existsByTrialNumberAndCourtLocationLocCode(trialNumber, courtLocationCode)) {
            throw new MojException.NotFound(String.format("Cannot find trial with number: %s for court location %s",
                trialNumber, courtLocationCode), null);
        }

        if (numberRequested <= 0 || numberRequested > MAX_PANEL_MEMBERS) {
            throw new MojException.BusinessRuleViolation(
                "Cannot create panel - Number requested must be between 1 and 1000",
                NUMBER_OF_JURORS_EXCEEDS_LIMITS);
        }

        if (panelRepository.existsByTrialTrialNumber(trialNumber)) {
            throw new MojException.BadRequest(
                "Cannot create panel - Trial already has a panel", null);
        }
    }

    private void addPanelMembersValidationChecks(int numberRequested, String trialNumber, String courtLocationCode) {

        Trial trial = trialRepository.findByTrialNumberAndCourtLocationLocCode(trialNumber, courtLocationCode);

        if (trial == null) {
            throw new MojException.NotFound(String.format("Cannot find trial with number: %s for court location %s",
                trialNumber, courtLocationCode), null);
        }

        if (trial.getTrialEndDate() != null) {
            throw new MojException.BusinessRuleViolation(
                "Cannot add panel members - Trial has ended", TRIAL_HAS_ENDED
            );
        }

        List<Panel> members = panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber,
            courtLocationCode);

        if (members.isEmpty()) {
            throw new MojException.BusinessRuleViolation(
                "Cannot add panel members - panel has not been created for trial",
                NO_PANEL_EXIST);
        }

        if (numberRequested <= 0 || numberRequested > MAX_PANEL_MEMBERS) {
            throw new MojException.BusinessRuleViolation(
                "Cannot add panel members - Number requested must be between 1 and 1000",
                NUMBER_OF_JURORS_EXCEEDS_LIMITS);
        }
    }

    private List<PanelListDto> processPanelList(int numberRequested, String trialNumber, String courtLocationCode,
                                                BureauJwtPayload payload,
                                                List<JurorPool> appearanceList) {

        if (numberRequested > appearanceList.size()) {
            throw new MojException.BusinessRuleViolation(
                "Cannot create panel - Not enough jurors available",
                NUMBER_OF_JURORS_EXCEEDS_AVAILABLE);
        }

        Trial trial = trialRepository.findByTrialNumberAndCourtLocationLocCode(trialNumber, courtLocationCode);
        List<PanelListDto> panelListDtosList = new ArrayList<>();
        for (int i = 0;
             i < numberRequested;
             i++) {
            Panel panel = createPanelEntity(appearanceList.get(i), trial);
            panel.getJurorPool().setLocation(trial.getCourtroom().getRoomNumber());
            if (panel.getJurorPool().getTimesSelected() == null) {
                panel.getJurorPool().setTimesSelected(1);
            } else {
                panel.getJurorPool().setTimesSelected(panel.getJurorPool().getTimesSelected() + 1);
            }

            panelRepository.saveAndFlush(panel);

            //update appearance record with trial number
            Appearance appearance = appearanceRepository.findByJurorNumber(panel.getJurorPool().getJurorNumber());
            appearance.setTrialNumber(trial.getTrialNumber());
            appearanceRepository.saveAndFlush(appearance);

            JurorHistoryUtils.saveJurorHistory(HistoryCodeMod.CREATE_NEW_PANEL,
                panel.getJurorPool().getJurorNumber(), panel.getJurorPool().getPoolNumber(), payload,
                jurorHistoryRepository);
            panelListDtosList.add(createPanelListDto(panel));
        }
        return panelListDtosList;
    }

    @Override
    public EmpanelListDto requestEmpanel(int numberRequested, String trialNumber, String locCode) {
        if (numberRequested <= 0 || numberRequested > 30) {
            throw new MojException.BadRequest(
                "Number requested must be between 1 and 30", null);
        }

        EmpanelListDto empanelListDto = new EmpanelListDto();
        List<EmpanelDetailsDto> detailsDtoList = new ArrayList<>();
        empanelListDto.setTotalJurorsForEmpanel(numberRequested);
        List<Panel> panelList =
            panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, locCode);
        for (Panel panel : panelList) {
            detailsDtoList.add(createEmpanelDetailsDto(panel));
        }
        empanelListDto.setEmpanelList(detailsDtoList);
        return empanelListDto;
    }

    @Override
    public List<PanelListDto> processEmpanelled(JurorListRequestDto dto, BureauJwtPayload payload) {
        if (dto.getNumberRequested() <= 0 || dto.getNumberRequested() > 30) {
            throw new MojException.BadRequest(
                "Number requested must be between 1 and 30", null);
        }

        for (JurorDetailRequestDto detail : dto.getJurors()) {
            Panel panelMember =
                panelRepository.findByTrialTrialNumberAndJurorPoolJurorJurorNumber(dto.getTrialNumber(),
                    detail.getJurorNumber());

            panelMember.setResult(detail.getResult());
            setJurorStatus(panelMember);

            panelRepository.saveAndFlush(panelMember);

            if (Objects.requireNonNull(panelMember.getResult()) == PanelResult.NOT_USED
                || panelMember.getResult() == PanelResult.CHALLENGED) {
                JurorHistoryUtils.saveJurorHistory(HistoryCodeMod.RETURN_PANEL,
                    panelMember.getJurorPool().getJurorNumber(), panelMember.getJurorPool().getPoolNumber(), payload,
                    jurorHistoryRepository);
                Appearance appearance =
                    appearanceRepository.findByJurorNumber(panelMember.getJurorPool().getJurorNumber());
                appearance.setPoolNumber(panelMember.getJurorPool().getPoolNumber());
                appearanceRepository.saveAndFlush(appearance);
            } else {
                JurorHistoryUtils.saveJurorHistory(HistoryCodeMod.JURY_EMPANELMENT,
                    panelMember.getJurorPool().getJurorNumber(), panelMember.getJurorPool().getPoolNumber(),
                    payload, jurorHistoryRepository);
            }
        }

        return getJurySummary(dto.getTrialNumber(), dto.getCourtLocationCode());
    }

    private static void setJurorStatus(Panel panelMember) {
        if (panelMember.getResult() == null) {
            throw new MojException.BadRequest(
                "Result has not been set, cannot process juror", null);
        }

        JurorStatus responded = new JurorStatus();
        switch (panelMember.getResult()) {
            case NOT_USED, CHALLENGED -> {
                responded.setStatus(IJurorStatus.RESPONDED);
                responded.setStatusDesc("Responded");
                panelMember.getJurorPool().setStatus(responded);
                panelMember.setCompleted(true);

            }
            case JUROR -> {
                responded.setStatus(IJurorStatus.JUROR);
                responded.setStatusDesc("Juror");
                panelMember.getJurorPool().setStatus(responded);
                panelMember.setCompleted(true);
            }
            default -> throw new MojException.BadRequest(
                "Invalid result - Wrong result set for juror",
                null);

        }
    }

    @Override
    public List<PanelListDto> getPanelSummary(String trialNumber, String locCode) {
        List<PanelListDto> dtoList = new ArrayList<>();
        List<Panel> panelList =
            panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, locCode);
        for (Panel panel : panelList) {
            if (panel.getResult() != PanelResult.RETURNED) {
                dtoList.add(createPanelListDto(panel));
            }
        }
        return dtoList;
    }

    @Override
    public List<PanelListDto> getJurySummary(String trialId, String locCode) {
        List<PanelListDto> dtoList = new ArrayList<>();
        List<Panel> panelList = panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialId, locCode);
        for (Panel panel : panelList) {
            if (panel.getJurorPool().getStatus().getStatus() == IJurorStatus.JUROR) {
                dtoList.add(createPanelListDto(panel));
            }
        }
        return dtoList;
    }


    private List<AvailableJurorsDto> processQueryResults(List<Tuple> results) {
        List<AvailableJurorsDto> dtoList = new ArrayList<>();
        for (Tuple tuple : results) {
            dtoList.add(createAvailableJurorDto(tuple));
        }
        return dtoList;
    }

    private AvailableJurorsDto createAvailableJurorDto(Tuple tuple) {
        AvailableJurorsDto dto = new AvailableJurorsDto();
        dto.setPoolNumber(tuple.get(0, String.class));
        dto.setAvailableJurors(tuple.get(1, Long.class));
        dto.setServiceStartDate(tuple.get(2, LocalDate.class));
        dto.setCourtLocation(tuple.get(3, String.class));
        dto.setCourtLocationCode(tuple.get(4, String.class));
        return dto;
    }

    private Panel createPanelEntity(JurorPool jurorPool, Trial trial) {
        //updating juror status
        JurorStatus status = new JurorStatus();
        status.setStatus(IJurorStatus.PANEL);
        status.setStatusDesc("Panelled");
        jurorPool.setStatus(status);
        jurorPoolRepository.saveAndFlush(jurorPool);

        Panel panel = new Panel();
        panel.setJurorPool(jurorPool);
        panel.setDateSelected(LocalDateTime.now());
        panel.setCompleted(false);
        panel.setTrial(trial);
        return panel;
    }

    private PanelListDto createPanelListDto(Panel panel) {
        PanelListDto panelListDto = new PanelListDto();
        panelListDto.setFirstName(panel.getJurorPool().getJuror().getFirstName());
        panelListDto.setLastName(panel.getJurorPool().getJuror().getLastName());
        panelListDto.setJurorNumber(panel.getJurorPool().getJuror().getJurorNumber());
        panelListDto.setJurorStatus(panel.getJurorPool().getStatus().getStatusDesc());
        return panelListDto;
    }

    private EmpanelDetailsDto createEmpanelDetailsDto(Panel panel) {
        EmpanelDetailsDto dto = new EmpanelDetailsDto();
        dto.setFirstName(panel.getJurorPool().getJuror().getFirstName());
        dto.setLastName(panel.getJurorPool().getJuror().getLastName());
        dto.setJurorNumber(panel.getJurorPool().getJurorNumber());
        dto.setStatus(panel.getJurorPool().getStatus().getStatusDesc());
        return dto;
    }

    private List<JurorPool> buildRandomJurorPoolList(Optional<List<String>> poolNumbers, String trialNumber) {
        List<JurorPool> appearanceList;
        if (poolNumbers.isPresent() && !poolNumbers.get().isEmpty()) {
            appearanceList = appearanceRepository.getJurorsInPools(poolNumbers.get());
        } else {
            appearanceList = appearanceRepository.retrieveAllJurors();
        }

        appearanceList = appearanceList.stream().filter(jurorPool ->
            panelRepository.findByTrialTrialNumberAndJurorPoolJurorJurorNumber(trialNumber,
                jurorPool.getJurorNumber()) == null).collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(appearanceList);

        return appearanceList;
    }
}
