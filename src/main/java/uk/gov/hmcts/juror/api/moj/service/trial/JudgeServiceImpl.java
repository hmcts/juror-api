package uk.gov.hmcts.juror.api.moj.service.trial;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeListDto;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.repository.trial.JudgeRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JudgeServiceImpl implements JudgeService {

    @Autowired
    private JudgeRepository judgeRepository;

    @Override
    public JudgeListDto getJudgeForCourtLocation(String owner) {
        List<Judge> judges = judgeRepository.findByOwner(owner);
        List<JudgeDto> judgeDtos = new ArrayList<>();
        for (Judge judge : judges) {
            judgeDtos.add(createJudgeDto(judge.getId(), judge.getCode(), judge.getName()));
        }
        JudgeListDto judgeListDto = new JudgeListDto();
        judgeListDto.setJudges(judgeDtos);
        return judgeListDto;
    }

    // needed to fix pmd issue
    private static JudgeDto createJudgeDto(Long id, String code, String description) {
        return new JudgeDto(id, code, description);
    }
}
