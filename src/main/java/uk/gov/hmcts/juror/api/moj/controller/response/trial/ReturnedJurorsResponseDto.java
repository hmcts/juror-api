package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "A List of jurors returned from a trial with a count of original empanelled jurors")
public class ReturnedJurorsResponseDto {

    @JsonProperty("returned_jurors")
    private List<PanelListDto> returnedJurors;

    @JsonProperty("original_jurors_count")
    private int originalJurorsCount;

}
