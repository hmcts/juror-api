package uk.gov.hmcts.juror.api.bureau.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.io.Serializable;

/**
 * Dto for crud operations on a staff member.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuppressWarnings("PMD.TooManyFields")
@Schema(description = "Staff member profile dto")
public class StaffMemberCrudResponseDto implements Serializable {
    @NotEmpty
    @Size(min = 1, max = 20)
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

    @NumericString
    @Size(max = 3)
    @Schema(description = "Court code staff member works", example = "123")
    private String court1;

    @NumericString
    @Size(max = 3)
    @Schema(description = "Court code staff member works", example = "123")
    private String court2;

    @NumericString
    @Size(max = 3)
    @Schema(description = "Court code staff member works", example = "123")
    private String court3;

    @NumericString
    @Size(max = 3)
    @Schema(description = "Court code staff member works", example = "123")
    private String court4;

    @NumericString
    @Size(max = 3)
    @Schema(description = "Court code staff member works", example = "123")
    private String court5;

    @NumericString
    @Size(max = 3)
    @Schema(description = "Court code staff member works", example = "123")
    private String court6;

    @NumericString
    @Size(max = 3)
    @Schema(description = "Court code staff member works", example = "123")
    private String court7;

    @NumericString
    @Size(max = 3)
    @Schema(description = "Court code staff member works", example = "123")
    private String court8;

    @NumericString
    @Size(max = 3)
    @Schema(description = "Court code staff member works", example = "123")
    private String court9;

    @NumericString
    @Size(max = 3)
    @Schema(description = "Court code staff member works", example = "123")
    private String court10;

    @Schema(description = "Optimistic locking version")
    private Integer version;

    public Integer getLevel() {
        return isTeamLeader() ? 1 : 0;
    }
}


