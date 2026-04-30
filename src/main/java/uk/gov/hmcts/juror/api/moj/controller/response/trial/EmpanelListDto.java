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
@Schema(description = "A List of jurors to be empanelled")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EmpanelListDto {
    @Schema(name = "Total jurors", description = "total jurors to be empanelled")
    private int totalJurorsForEmpanel;

    @Schema(name = "empanelled list", description = "list of jurors to be empanelled")
    private List<EmpanelDetailsDto> empanelList;
}
