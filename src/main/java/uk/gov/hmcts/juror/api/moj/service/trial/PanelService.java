package uk.gov.hmcts.juror.api.moj.service.trial;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.AvailableJurorsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.EmpanelListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.PanelListDto;

import java.time.LocalDate;
import java.util.List;

public interface PanelService {
    List<AvailableJurorsDto> getAvailableJurors(String courtLocation);

    List<PanelListDto> createPanel(int numberRequested, String trialNumber,
                                   List<String> poolNumbers, String courtLocationCodes,
                                   LocalDate attendanceDate,
                                   BureauJwtPayload payload);

    EmpanelListDto requestEmpanel(int numberRequested, String trialNumber, String locCode);

    List<PanelListDto> processEmpanelled(JurorListRequestDto dto, BureauJwtPayload payload);

    List<PanelListDto> getPanelSummary(String trialId, String locCode, LocalDate date);

    List<PanelListDto> getJurySummary(String trialId, String locCode);

    List<PanelListDto> addPanelMembers(int numberRequested, String trialNumber,
                                       List<String> poolNumbers, String courtLocationCode,
                                       LocalDate attendanceDate);

    Boolean getPanelStatus(String trialNumber, String courtLocationCode);

    boolean isEmpanelledJuror(String jurorNumber, String locationCode, LocalDate date);

    int getCountPanelledJurors(String locCode);

}
