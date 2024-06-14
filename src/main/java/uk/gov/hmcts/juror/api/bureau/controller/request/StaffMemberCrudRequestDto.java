package uk.gov.hmcts.juror.api.bureau.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Dto for crud operations on a staff member.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Staff member profile dto")
public class StaffMemberCrudRequestDto implements Serializable {
    @NotEmpty(groups = CreationOnlyValidationGroup.class)
    @Size(min = 1, max = 30, groups = CreationOnlyValidationGroup.class)
    @Schema(description = "Login username of the staff member juror account", requiredMode =
        Schema.RequiredMode.REQUIRED)
    private String login;

    @NotEmpty
    @Size(min = 1, max = 30)
    @Schema(description = "Display name of the staff member in the UI", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Is the staff member a team leader?", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean teamLeader;

    @Schema(description = "Is the staff member active?", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean active;

    @NotNull
    @Min(1)
    @Schema(description = "Team ID of the staff member", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long team;

    @Schema(description = "Optimistic locking version")
    private Integer version;

    public Integer getLevel() {
        return isTeamLeader() ? 1 : 0;
    }

    /**
     * Validation group for creation of staff INCLUDING default validation.
     */
    public interface CreationOnlyValidationGroup extends Default {
        // no-op
    }
}


