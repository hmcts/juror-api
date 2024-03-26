package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.bureau.domain.Team;

/**
 * DTO for {@link uk.gov.hmcts.juror.api.bureau.domain.Team}.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Team")
public class TeamDto {

    @JsonProperty("id")
    @Schema(description = "Team ID", example = "1")
    private Long id;

    @JsonProperty("name")
    @Schema(description = "Team name", example = "Midlands & South West")
    private String name;

    @JsonProperty("version")
    @Schema(description = "Optimistic locking version", example = "5")
    private Integer version;

    public TeamDto(final Team team) {
        this.id = team.getId();
        this.name = team.getTeamName();
        this.version = team.getVersion();
    }
}
