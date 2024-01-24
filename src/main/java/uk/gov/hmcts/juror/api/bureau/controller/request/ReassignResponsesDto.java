package uk.gov.hmcts.juror.api.bureau.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request DTO for reassigning a deactivated users assigned responses.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Dto for assigning officers responses to other staff depending on status and deactivating "
    + "officer")
public class ReassignResponsesDto implements Serializable {

    @Size(max = 20)
    @NotNull
    @Schema(description = "Login name of the staff member to be deactivated).")
    private String staffToDeactivate;

    @Size(max = 20)
    @Schema(description = "Login name of the staff member to assign any Urgent/Super-Urgent responses to.")
    private String urgentsLogin;

    @Size(max = 20)
    @Schema(description = "Login name of the staff member to assign any pending responses to.")
    private String pendingLogin;

    @Size(max = 20)
    @Schema(description = "Login name of the staff member to assign any to-do responses to (null assigns to backlog).")
    private String todoLogin;
}