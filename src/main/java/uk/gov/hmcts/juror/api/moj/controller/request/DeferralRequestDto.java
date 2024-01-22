package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.domain.DeferralDecision;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

/**
 * Request DTO for Juror Response Deferral decision.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Deferral decision")
public class DeferralRequestDto {


    @JsonProperty("jurorNumber")
    @JurorNumber
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String jurorNumber;

    @NotNull
    @JsonProperty("deferralReason")
    @Size(min = 1, max = 1)
    @Schema(description = "Excusal code reason. Single character.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deferralReason;//is an EXC_CODE char

    @JsonProperty("deferralDecision")
    @Schema(description = "Deferral Decision, either GRANT or REFUSE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private DeferralDecision deferralDecision;

}
