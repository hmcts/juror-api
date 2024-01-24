package uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Result of saving and validating a paper summons reply")
public class SaveJurorPaperReplyResponseDto {

    @JsonProperty("straightThroughAcceptance")
    @Schema(description = "Is this summons reply eligible for straight through acceptance")
    private boolean straightThroughAcceptance;

}
