package uk.gov.hmcts.juror.api.moj.domain.administration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JudgeDetailsDtoTest {

    @Test
    void positiveConstructor() {
        LocalDateTime lastUsed = LocalDateTime.now();
        Judge judge = Judge.builder()
            .id(123L)
            .code("judgeCode")
            .name("judgeName")
            .isActive(true)
            .lastUsed(lastUsed)
            .build();
        JudgeDetailsDto judgeDetailsDto = new JudgeDetailsDto(judge);
        assertThat(judgeDetailsDto.getJudgeId()).isEqualTo(123L);
        assertThat(judgeDetailsDto.getJudgeCode()).isEqualTo("judgeCode");
        assertThat(judgeDetailsDto.getJudgeName()).isEqualTo("judgeName");
        assertThat(judgeDetailsDto.isActive()).isTrue();
        assertThat(judgeDetailsDto.getLastUsed()).isEqualTo(lastUsed);

    }
}
