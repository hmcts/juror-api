package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Response DTO for listing active pool requests.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Pool request list response")
public class PoolRequestActiveDataDto {

    @Schema(name = "Court name", description = "Name for a given court location")
    private String courtName;

    @Schema(name = "Pool number", description = "The unique number for a pool request")
    private String poolNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(name = "Attendance date",
        description = "The date the pool has been requested for and when the jurors are expected to first attend "
            + "the court")
    private LocalDate attendanceDate;

    @Schema(name = "Confirmed From Bureau",
        description = "The number of pool members owned by the Bureau who have a status of 'Responded'")
    private int confirmedFromBureau;

    @Schema(name = "Requested From Bureau", description = "The number of jurors to be supplied by the Bureau")
    private int requestedFromBureau;

    @Schema(name = "Pool capacity required by Court", description = "The total number of jurors required for a "
        + "given Pool")
    private int poolCapacity;

    @Schema(name = "Active Jurors in the Pool",
        description = "The number of pool members owned by Court who have a status of Responded, Panel or Juror")
    private long jurorsInPool;

    @Schema(name = "Type of Pool Requested",
        description = "The Type of the pool being requested, e.g. Crown Court")
    private String poolType;

    @Schema(name = "Required",
        description = "Indicates total amount required for the court to operate")
    private int required;

}
