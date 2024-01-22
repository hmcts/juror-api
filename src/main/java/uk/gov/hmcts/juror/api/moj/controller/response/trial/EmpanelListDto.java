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
@Schema(description = "A List of jurors to be empanelled")
public class EmpanelListDto {
    @JsonProperty("total_jurors_for_empanel")
    @Schema(name = "Total jurors", description = "total jurors to be empanelled")
    private int totalJurorsForEmpanel;

    @JsonProperty("empanel_list")
    @Schema(name = "empanelled list", description = "list of jurors to be empanelled")
    private List<EmpanelDetailsDto> empanelList;
}
