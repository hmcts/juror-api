package uk.gov.hmcts.juror.api.moj.service.administration;

import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeCreateDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeUpdateDto;

import java.util.List;

public interface AdministrationJudgeService {
    JudgeDetailsDto viewJudge(long judgeId);

    void deleteJudge(Long judgeId);

    void updateJudge(Long judgeId, JudgeUpdateDto judgeUpdateDto);

    List<JudgeDetailsDto> viewAllJudges(Boolean isActive);

    void createJudge(JudgeCreateDto judgeCreateDto);
}
