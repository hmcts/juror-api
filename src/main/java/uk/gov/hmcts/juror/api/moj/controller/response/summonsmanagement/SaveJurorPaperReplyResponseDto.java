package uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Result of saving and validating a paper summons reply")
public class SaveJurorPaperReplyResponseDto {

    @Schema(description = "Is this summons reply eligible for straight through acceptance")
    private boolean straightThroughAcceptance;

}
