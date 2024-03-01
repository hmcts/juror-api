package uk.gov.hmcts.juror.api.moj.domain.administration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JudgeDetailsDto {

    private long judgeId;
    private String judgeCode;
    private String judgeName;
    private boolean isActive;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime lastUsed;

    public JudgeDetailsDto(Judge judge) {
        this.judgeId = judge.getId();
        this.judgeCode = judge.getCode();
        this.judgeName = judge.getName();
        this.isActive = judge.isActive();
        this.lastUsed = judge.getLastUsed();
    }
}
