package uk.gov.hmcts.juror.api.moj.service.administration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeCreateDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeUpdateDto;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.trial.JudgeRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class AdministrationJudgeServiceImpl implements AdministrationJudgeService {
    private final JudgeRepository judgeRepository;

    @Override
    @Transactional(readOnly = true)
    public JudgeDetailsDto viewJudge(long judgeId) {
        return new JudgeDetailsDto(getJudge(judgeId));
    }

    @Override
    @Transactional
    public void updateJudge(Long judgeId, JudgeUpdateDto judgeUpdateDto) {
        Judge judge = getJudge(judgeId);
        if (!judge.getCode().equals(judgeUpdateDto.getJudgeCode())) {
            verifyCodeDoesNotExist(judge.getOwner(), judgeUpdateDto.getJudgeCode());
        }
        judge.setCode(judgeUpdateDto.getJudgeCode());
        judge.setName(judgeUpdateDto.getJudgeName());
        judge.setActive(judgeUpdateDto.getIsActive());
        judgeRepository.save(judge);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeDetailsDto> viewAllJudges(Boolean isActive) {
        return getJudges(isActive).stream().map(JudgeDetailsDto::new).toList();
    }

    @Override
    public void createJudge(JudgeCreateDto judgeCreateDto) {
        verifyCodeDoesNotExist(SecurityUtil.getActiveOwner(), judgeCreateDto.getJudgeCode());

        Judge judge = Judge.builder()
            .owner(SecurityUtil.getActiveOwner())
            .code(judgeCreateDto.getJudgeCode())
            .name(judgeCreateDto.getJudgeName())
            .isActive(true)
            .build();

        judgeRepository.save(judge);
    }

    @Override
    @Transactional
    public void deleteJudge(Long judgeId) {
        Judge judge = getJudge(judgeId);
        if (judge.getLastUsed() != null) {
            throw new MojException.BusinessRuleViolation(
                "Judge has been used and cannot be deleted",
                MojException.BusinessRuleViolation.ErrorCode.CANNOT_DELETE_USED_JUDGE);
        }
        judgeRepository.delete(judge);
    }


    void verifyCodeDoesNotExist(String owner, String judgeCode) {
        judgeRepository.findByOwnerAndCode(owner, judgeCode)
            .ifPresent(j -> {
                throw new MojException.BusinessRuleViolation(
                    "Judge with this code already exists",
                    MojException.BusinessRuleViolation.ErrorCode.CODE_ALREADY_IN_USE);
            });
    }

    Judge getJudge(long judgeId) {
        Judge judge = judgeRepository.findById(judgeId)
            .orElseThrow(() -> new MojException.NotFound("Judge not found", null));
        SecurityUtil.validateCanAccessOwner(judge.getOwner());
        return judge;
    }

    List<Judge> getJudges(Boolean isActive) {
        if (isActive == null) {
            return judgeRepository.findByOwner(SecurityUtil.getActiveOwner());
        } else {
            return judgeRepository.findByOwnerAndIsActive(SecurityUtil.getActiveOwner(), isActive);
        }
    }
}
