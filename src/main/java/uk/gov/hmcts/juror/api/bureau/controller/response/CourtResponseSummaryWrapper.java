package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Wrapper for returning juror details transferred to court")
public class CourtResponseSummaryWrapper {
    @Schema(description = "Count of responses still not processed")
    private Long todoCourtCount;
    @Schema(description = "Count of responses still pending")
    private Long repliesPendingCourtCount;
    @Schema(description = "Count of responses closed")
    private Long completedCourtCount;

    @JsonProperty("data")
    @Schema(description = "List of juror response details transferred to court")
    private List<CourtResponseSummaryDto> responses;
}
