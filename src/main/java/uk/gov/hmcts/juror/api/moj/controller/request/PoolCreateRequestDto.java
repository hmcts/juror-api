package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Pool Create request")
public class PoolCreateRequestDto implements Serializable {

    @JsonProperty("poolNumber")
    @Schema(name = "Pool number", description = "Pool Request number")
    private String poolNumber;

    @JsonProperty("startDate")
    @Schema(name = "Start Date", description = "Pool Create - Start Date")
    private LocalDate startDate;

    @JsonProperty("attendTime")
    @Schema(name = "Attend Time", description = "Pool Create - Attend Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime attendTime;

    @JsonProperty("noRequested")
    @Schema(name = "Jurors Requested", description = "Pool Create - No. of Jurors Requested")
    private Integer noRequested;

    @JsonProperty("bureauDeferrals")
    @Schema(name = "Bureau Deferrals", description = "Pool Create - Bureau Deferrals")
    private Integer bureauDeferrals;

    @JsonProperty("numberRequired")
    @Schema(name = "Jurors required", description = "Pool Create - Number of Jurors required")
    private Integer numberRequired;

    @JsonProperty("citizensToSummon")
    @Schema(name = "Citizens to summon", description = "Pool Create - Number of Citizens to summon")
    private Integer citizensToSummon;

    @JsonProperty("catchmentArea")
    @Schema(name = "Catchment Area", description = "Pool Create - Summons Catchment Area (location code)")
    private String catchmentArea;

    @JsonProperty("postcodes")
    @Schema(name = "Postcodes", description = "Pool create - Postcodes to include in catchment area")
    private List<String> postcodes;

    @JsonProperty("previous_juror_count")
    @Schema(name = "previous_juror_count",
        description = "The number of jurors associated to this pool when it was viewed")
    private int previousJurorCount;

}