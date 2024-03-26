package uk.gov.hmcts.juror.api.moj.service.trial;

import org.springframework.data.domain.Page;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.EndTrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.ReturnJuryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialSummaryDto;

import java.util.List;

public interface TrialService {
    TrialSummaryDto createTrial(BureauJwtPayload payload, TrialDto trialDto);

    Page<TrialListDto> getTrials(BureauJwtPayload payload, int pageNumber, String sortBy, String sortOrder,
                                 boolean isActive, String trialNumber);

    TrialSummaryDto getTrialSummary(BureauJwtPayload payload, String trialNo, String locCode);

    void returnPanel(BureauJwtPayload payload, String trialNo, String locCode,
                     List<JurorDetailRequestDto> jurorDetailRequestDto);

    void returnJury(BureauJwtPayload payload, String trialNumber, String locationCode,
                    ReturnJuryDto jurorDetailRequestDto);

    void endTrial(EndTrialDto dto);
}
