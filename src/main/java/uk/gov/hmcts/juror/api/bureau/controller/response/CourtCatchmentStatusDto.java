package uk.gov.hmcts.juror.api.bureau.controller.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for {@link uk.gov.hmcts.juror.api.bureau.domain.Team}.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "CourtCatchmentStatus")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtCatchmentStatusDto {

    @Schema(description = "Catchment status")
    private String courtCatchmentStatus;
}
