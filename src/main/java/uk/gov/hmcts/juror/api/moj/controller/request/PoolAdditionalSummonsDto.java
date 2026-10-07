package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Summon additional citizens request")
public class PoolAdditionalSummonsDto implements Serializable {

    @Schema(name = "Pool number", description = "Pool Request number")
    private String poolNumber;

    @Schema(name = "Jurors Requested", description = "Pool Create - No. of Jurors Requested")
    private Integer noRequested;

    @Schema(name = "Bureau Deferrals", description = "Pool Create - Bureau Deferrals")
    private Integer bureauDeferrals;

    @Schema(name = "Citizens already summoned", description = "Pool Create - Number of Citizens already summoned")
    private Integer citizensSummoned;

    @Schema(name = "Citizens required", description = "Pool Create - Number of additional citizens required")
    private Integer citizensToSummon;

    @Schema(name = "Catchment Area", description = "Pool Create - Summons Catchment Area (location code)")
    private String catchmentArea;

    @Schema(name = "Postcodes", description = "Pool create - Postcodes to include in catchment area")
    private List<String> postcodes;

    @JsonProperty("previous_juror_count")
    @Schema(name = "previous_juror_count",
        description = "The number of jurors associated to this pool when it was viewed")
    private int previousJurorCount;

}
