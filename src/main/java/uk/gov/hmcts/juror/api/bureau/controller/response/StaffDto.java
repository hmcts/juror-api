package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.User;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO for {@link uk.gov.hmcts.juror.api.bureau.domain.Staff}.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Single staff member")
public class StaffDto implements Serializable {

    @JsonProperty("login")
    @Schema(description = "Staff member login", example = "jpowers")
    private String login;

    @JsonProperty("name")
    @Schema(description = "Staff member name", example = "Joanna Powers")
    private String name;

    @JsonProperty("team")
    @Schema(description = "Team the staff member is part of ")
    private TeamDto team;

    @JsonProperty("isTeamLeader")
    @Schema(description = "Is the staff member a team leader?")
    private boolean isTeamLeader;

    @JsonProperty("isActive")
    @Schema(description = "Is the staff member active?")
    private boolean isActive;

    @JsonProperty("version")
    @Schema(description = "Optimistic locking version", example = "5")
    private Integer version;

    public StaffDto(final User user) {
        if (!Objects.isNull(user)) {
            this.login = user.getUsername();
            this.name = user.getName();
            this.team = !Objects.isNull(user.getTeam())
                ?
                new TeamDto(user.getTeam())
                :
                null;
            this.isTeamLeader = user.isTeamLeader();
            this.isActive = user.isActive();
            this.version = user.getVersion();
        }
    }
}
