package uk.gov.hmcts.juror.api.moj.controller.response.trial;


import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CourtroomsListDto {
    @JsonProperty("court_location")
    private String courtLocation;

    @JsonProperty("court_rooms")
    private List<CourtroomsDto> courtRooms;
}
