package uk.gov.hmcts.juror.api.moj.controller.response;

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
@Schema(description = "Summary of Management of juror(s) results")
public class JurorManagementResponseDto implements Serializable {

    @JsonProperty("availableForMove")
    @Schema(name = "Available For Move(transfer/reassign)", description = "List of juror numbers who pass validation and can be "
        + "moved")
    private List<String> availableForMove;

    @JsonProperty("unavailableForMove")
    @Schema(name = "Unavailable For Move(transfer/reassign)", description = "List of juror numbers who failed validation and cannot "
        + "be moved")
    private List<ValidationFailure> unavailableForMove;

    @Setter
    @Getter
    @Schema(description = "Breakdown of pool members who failed initial validation and are not available for Move(transfer/reassign)")
    public static class ValidationFailure implements Serializable {

        @JsonProperty("jurorNumber")
        @Schema(name = "Juror Number", description = "9-digit numeric string to identify a juror")
        private String jurorNumber;

        @JsonProperty("failureReason")
        @Schema(name = "Failure Reason", description = "Description of why this juror failed validations Move(transfer/reassign)")
        private String failureReason;

    }

}
