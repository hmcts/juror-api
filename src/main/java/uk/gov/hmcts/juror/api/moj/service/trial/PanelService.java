package uk.gov.hmcts.juror.api.moj.service.trial;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.AvailableJurorsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.EmpanelListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.PanelListDto;

import java.util.List;
import java.util.Optional;

public interface PanelService {
    List<AvailableJurorsDto> getAvailableJurors(String courtLocation);

    List<PanelListDto> createPanel(int numberRequested, String trialNumber,
                                   Optional<List<String>> poolNumbers, String courtLocationCodes,
                                   BureauJwtPayload payload);

    EmpanelListDto requestEmpanel(int numberRequested, String trialNumber, String locCode);

    List<PanelListDto> processEmpanelled(JurorListRequestDto dto, BureauJwtPayload payload);

    List<PanelListDto> getPanelSummary(String trialId, String locCode);

    List<PanelListDto> getJurySummary(String trialId, String locCode);
}
