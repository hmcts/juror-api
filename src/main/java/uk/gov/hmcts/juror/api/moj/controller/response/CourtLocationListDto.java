package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CourtLocationListDto {

    @JsonProperty("courts")
    @Schema(description = "List of court locations")
    private List<CourtLocationDataDto> data;

}
