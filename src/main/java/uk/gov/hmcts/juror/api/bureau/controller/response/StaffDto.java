package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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

    /**
     * Court location staff member covers.
     */
    @Size(max = 3)
    private String court1;

    /**
     * Court location staff member covers.
     */
    @Size(max = 3)
    private String court2;

    /**
     * Court location staff member covers.
     */
    @Size(max = 3)
    private String court3;

    /**
     * Court location staff member covers.
     */
    @Size(max = 3)
    private String court4;

    /**
     * Court location staff member covers.
     */
    @Size(max = 3)
    private String court5;

    /**
     * Court location staff member covers.
     */
    @Size(max = 3)
    private String court6;

    /**
     * Court location staff member covers.
     */
    @Size(max = 3)
    private String court7;

    /**
     * Court location staff member covers.
     */
    @Size(max = 3)
    private String court8;

    /**
     * Court location staff member covers.
     */
    @Size(max = 3)
    private String court9;

    /**
     * Court location staff member covers.
     */
    @Size(max = 3)
    private String court10;

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

            this.court1 = user.getCourtAtIndex(0, null);
            this.court2 = user.getCourtAtIndex(1, null);
            this.court3 = user.getCourtAtIndex(2, null);
            this.court4 = user.getCourtAtIndex(3, null);
            this.court5 = user.getCourtAtIndex(4, null);
            this.court6 = user.getCourtAtIndex(5, null);
            this.court7 = user.getCourtAtIndex(6, null);
            this.court8 = user.getCourtAtIndex(7, null);
            this.court9 = user.getCourtAtIndex(8, null);
            this.court10 = user.getCourtAtIndex(9, null);
        }
    }
}
