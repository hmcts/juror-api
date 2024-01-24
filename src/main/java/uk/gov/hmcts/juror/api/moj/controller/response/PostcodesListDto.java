package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.VotersLocPostcodeTotals;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostcodesListDto implements Serializable {

    @JsonProperty("CourtCatchmentItems")
    @Schema(name = "CourtCatchment", description = "Postcodes and Totals for catchment area")
    private List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> courtCatchmentSummaryItems;

}
