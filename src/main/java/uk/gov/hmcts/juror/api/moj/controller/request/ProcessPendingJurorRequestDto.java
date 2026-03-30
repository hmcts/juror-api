package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.enumeration.ApprovalDecision;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ProcessPendingJurorRequestDto {

    @JsonProperty("jurorNumber")
    @Pattern(regexp = JUROR_NUMBER)
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String jurorNumber;

    @JsonProperty("decision")
    @NotNull
    @Schema(description = "Decision to either approve or reject a pending name change")
    private ApprovalDecision decision;

    @JsonProperty("comments")
    @Schema(description = "Comments to be added to the pending juror record")
    private String comments;

}
