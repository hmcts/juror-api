package uk.gov.hmcts.juror.api.moj.service.trial;

import org.springframework.data.domain.Page;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.ReturnJuryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialSummaryDto;

import java.util.List;

public interface TrialService {
    TrialSummaryDto createTrial(BureauJWTPayload payload, TrialDto trialDto);

    Page<TrialListDto> getTrials(BureauJWTPayload payload, int pageNumber, String sortBy, String sortOrder,
                                 boolean isActive);

    TrialSummaryDto getTrialSummary(BureauJWTPayload payload, String trialNo, String locCode);

    void returnPanel(BureauJWTPayload payload, String trialNo, String locCode,
                     List<JurorDetailRequestDto> jurorDetailRequestDto);

    void returnJury(BureauJWTPayload payload, String trialNumber, String locationCode,
                    ReturnJuryDto jurorDetailRequestDto);
}
