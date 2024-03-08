package uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;

/**
 * Payload (request message) containing details to disqualify a juror.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
@Getter
@Data
@Schema(description = "Disqualify juror payload")
public class DisqualifyJurorDto {
    @JsonProperty("replyMethod")
    @Schema(description = "Reply method type (PAPER or DIGITAL)")
    @NotNull(message = "Reply method is missing")
    public ReplyMethod replyMethod;

    @JsonProperty("code")
    @Schema(description = "Disqualification code")
    @NotNull(message = "Disqualify code is missing")
    private DisqualifyCodeEnum code;
}
