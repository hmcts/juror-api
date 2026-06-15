package uk.gov.hmcts.juror.api.moj.controller.response.trial;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "A List of courtrooms for court locations")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtroomsListDto {
    private String courtLocation;

    private List<CourtroomsDto> courtRooms;
}
