package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "A List of jurors returned from a trial with a count of original empanelled jurors")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReturnedJurorsResponseDto {

    private List<PanelListDto> returnedJurors;

    private int originalJurorsCount;

}
