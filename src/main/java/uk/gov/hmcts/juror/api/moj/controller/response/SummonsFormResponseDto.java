package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.VotersLocPostcodeTotals;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
@Schema(description = "Summons form request")
public class SummonsFormResponseDto implements Serializable {

    @JsonProperty("bureauDeferrals")
    @Schema(name = "Bureau Deferrals", description = "Bureau Deferrals")
    private Integer bureauDeferrals;

    @JsonProperty("numberRequired")
    @Schema(name = "Number of Jurors required", description = "Jurors required")
    private Integer numberRequired;

    @JsonProperty("courtCatchmentItems")
    @Schema(name = "CourtCatchment", description = "Postcodes and Totals for catchment area")
    private List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> courtCatchmentSummaryItems;


}