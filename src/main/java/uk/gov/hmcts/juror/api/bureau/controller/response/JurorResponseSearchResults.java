package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Wrapper for returning juror details in response to a search")
public class JurorResponseSearchResults {
    @JsonProperty("data")
    @Schema(description = "List of juror response details")
    private List<BureauResponseSummaryDto> responses;

    @Schema(description = "Metadata")
    private BureauSearchMetadata meta;

    /**
     * Created by russellda on 04/07/17.
     */
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Metadata for bureau search")
    public static class BureauSearchMetadata {

        @Schema(description = "Total number of database rows matching query (NB: May be more than number returned if "
            + "total > max)", example = "178")
        private Long total;

        @Schema(description = "Maximum number of results returned by this endpoint", example = "100")
        private Integer max;
    }
}
