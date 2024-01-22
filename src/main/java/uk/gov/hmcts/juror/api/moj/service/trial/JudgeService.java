package uk.gov.hmcts.juror.api.moj.service.trial;

import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeListDto;

public interface JudgeService {
    JudgeListDto getJudgeForCourtLocation(String courtLocation);
}
