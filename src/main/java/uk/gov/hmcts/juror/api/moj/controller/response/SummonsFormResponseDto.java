package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.VotersLocPostcodeTotals;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Summons form request")
public class SummonsFormResponseDto implements Serializable {

    @Schema(name = "Bureau Deferrals", description = "Bureau Deferrals")
    private Integer bureauDeferrals;

    @Schema(name = "Number of Jurors required", description = "Jurors required")
    private Integer numberRequired;

    @Schema(name = "CourtCatchment", description = "Postcodes and Totals for catchment area")
    private List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> courtCatchmentSummaryItems;


}
