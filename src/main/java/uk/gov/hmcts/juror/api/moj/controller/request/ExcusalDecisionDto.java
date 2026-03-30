package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalDecision;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;

/**
 * Request DTO for Juror Response Excusal decision.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Excusal decision")
public class ExcusalDecisionDto {

    @JsonProperty("excusalReasonCode")
    @Schema(description = "Excusal Reason Code for Juror's excusal reason", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(max = 2)
    @NotEmpty
    @NotNull
    private String excusalReasonCode;

    @JsonProperty("excusalDecision")
    @Schema(description = "Excusal Decision, either GRANT or REFUSE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private ExcusalDecision excusalDecision;

    @JsonProperty("replyMethod")
    @Schema(description = "Reply method of juror response, either PAPER or DIGITAL", requiredMode =
        Schema.RequiredMode.REQUIRED)
    private ReplyMethod replyMethod;

}
