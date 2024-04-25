package uk.gov.hmcts.juror.api.moj.service.trial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JurorForExemptionListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialExemptionListDto;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ExemptionCertificateServiceImpl implements ExemptionCertificateService {

    @Autowired
    private PanelRepository panelRepository;

    @Autowired
    private TrialRepository trialRepository;

    @Override
    public List<TrialExemptionListDto> getTrialExemptionList(String courtLocation) {
        BureauJwtPayload payload = SecurityUtil.getActiveUsersBureauPayload();
        if (!payload.getStaff().getCourts().contains(courtLocation)) {
            throw new MojException.Forbidden
                .Forbidden("Current user has insufficient permission to view the trial details for the court location",
                null);
        }

        List<TrialExemptionListDto> dtoList = new ArrayList<>();
        List<Trial> trials = trialRepository.getListOfActiveTrials(courtLocation);

        for (Trial trial : trials) {
            TrialExemptionListDto.TrialExemptionListDtoBuilder builder = TrialExemptionListDto.builder();
            builder.judge(trial.getJudge().getName());
            builder.trialNumber(trial.getTrialNumber());
            builder.trialType(trial.getTrialType().getDescription());
            builder.defendants(trial.getDescription());
            builder.startDate(trial.getTrialStartDate());
            builder.endDate(trial.getTrialEndDate());
            dtoList.add(builder.build());
        }

        return dtoList;
    }

    @Override
    public List<JurorForExemptionListDto> getJurorsForExemptionList(String caseNumber, String courtLocation) {
        BureauJwtPayload payload = SecurityUtil.getActiveUsersBureauPayload();
        if (!payload.getStaff().getCourts().contains(courtLocation)) {
            throw new MojException.Forbidden
                .Forbidden("Current user has insufficient permission to view the trial details for the court location",
                null);
        }

        List<JurorForExemptionListDto> dtoList = new ArrayList<>();

        List<Panel> panelMembers = panelRepository
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(caseNumber, courtLocation);
        for (Panel member : panelMembers) {
            dtoList.add(JurorForExemptionListDto.builder()
                .dateEmpanelled(member.getDateSelected().toLocalDate())
                .firstName(member.getJuror().getFirstName())
                .lastName(member.getJuror().getLastName())
                .jurorNumber(member.getJurorNumber())
                .build());
        }
        return dtoList;
    }
}
