package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.enumeration.ApprovalDecision;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProcessNameChangeRequestDto {

    @JsonProperty("decision")
    @NotNull
    @Schema(description = "Decision to either approve or reject a pending name change")
    private ApprovalDecision decision;

    @JsonProperty("notes")
    @Size(max = 2000)
    @NotBlank
    @Schema(description = "Reason for approval/rejection")
    private String notes;

}
