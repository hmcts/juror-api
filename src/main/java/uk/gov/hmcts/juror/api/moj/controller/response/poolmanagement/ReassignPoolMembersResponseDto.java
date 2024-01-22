package uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Summary of Reassign pool member(s) results")
public class ReassignPoolMembersResponseDto implements Serializable {

    @JsonProperty("availableForReassignment")
    @Schema(name = "Available For Reassign", description = "List of juror numbers who pass validation and can be "
        + "Reassigned")
    private List<String> availableForReassign;

    @JsonProperty("unavailableForReassignment")
    @Schema(name = "Unavailable For Reassignment", description = "List of juror numbers who failed validation and "
        + "cannot be Reassigned")
    private List<ValidationFailures> unavailableForReassign;

    @Setter
    @Schema(description = "Breakdown of pool members who failed initial validation and are not available for "
        + "Reassignment")
    public static class ValidationFailures implements Serializable {

        @JsonProperty("jurorNumber")
        @Schema(name = "Juror Number", description = "9-digit numeric string to identify a pool member")
        private String jurorNumber;

        @JsonProperty("failureReason")
        @Schema(name = "Failure Reason", description = "Description of why this juror failed validations Reassignment")
        private String failureReason;

    }

}