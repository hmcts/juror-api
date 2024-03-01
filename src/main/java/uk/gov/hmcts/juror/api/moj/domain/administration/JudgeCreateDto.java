package uk.gov.hmcts.juror.api.moj.domain.administration;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JudgeCreateDto {
    @NotBlank
    @Length(min = 1, max = 4)
    private String judgeCode;
    @NotBlank
    @Length(min = 1, max = 30)
    private String judgeName;
}
