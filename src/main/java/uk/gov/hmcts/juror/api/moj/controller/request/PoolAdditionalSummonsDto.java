package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Summon additional citizens request")
public class PoolAdditionalSummonsDto implements Serializable {

    @JsonProperty("poolNumber")
    @Schema(name = "Pool number", description = "Pool Request number")
    private String poolNumber;

    @JsonProperty("noRequested")
    @Schema(name = "Jurors Requested", description = "Pool Create - No. of Jurors Requested")
    private Integer noRequested;

    @JsonProperty("bureauDeferrals")
    @Schema(name = "Bureau Deferrals", description = "Pool Create - Bureau Deferrals")
    private Integer bureauDeferrals;

    @JsonProperty("citizensSummoned")
    @Schema(name = "Citizens already summoned", description = "Pool Create - Number of Citizens already summoned")
    private Integer citizensSummoned;

    @JsonProperty("citizensToSummon")
    @Schema(name = "Citizens required", description = "Pool Create - Number of additional citizens required")
    private Integer citizensToSummon;

    @Schema(name = "Catchment Area", description = "Pool Create - Summons Catchment Area (location code)")
    private String catchmentArea;

    @JsonProperty("postcodes")
    @Schema(name = "Postcodes", description = "Pool create - Postcodes to include in catchment area")
    private List<String> postcodes;

}