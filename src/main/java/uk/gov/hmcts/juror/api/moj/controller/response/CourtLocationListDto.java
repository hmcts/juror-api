package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for court location list.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Court location list response")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtLocationListDto {

    @Schema(description = "List of court locations")
    private List<CourtLocationDataDto> data;

}
