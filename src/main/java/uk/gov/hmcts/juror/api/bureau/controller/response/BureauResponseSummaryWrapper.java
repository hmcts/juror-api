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
@Schema(description = "Wrapper for returning juror details")
public class BureauResponseSummaryWrapper {

    @Schema(description = "Count of responses still not processed")
    private Long todoCount;
    @Schema(description = "Count of responses still pending")
    private Long repliesPendingCount;
    @Schema(description = "Count of responses closed")
    private Long completedCount;

    @JsonProperty("data")
    @Schema(description = "List of juror response details")
    private List<BureauResponseSummaryDto> responses;
}
