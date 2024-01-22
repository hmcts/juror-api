package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for listing active pool requests.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Pool request list response")
public class PoolRequestActiveListDto {

    @JsonProperty("poolRequestsActive")
    @Schema(description = "List of active pool requests")
    private List<PoolRequestActiveDataDto> data;

    @JsonProperty("totalSize")
    @Schema(description = "The total number of active pool records found - used for pagination")
    private long totalSize;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    @Schema(description = "Active Pool Requests data")
    @ToString
    public static class PoolRequestActiveDataDto {

        @JsonProperty("courtName")
        @Schema(name = "Court name", description = "Name for a given court location")
        private String courtName;

        @JsonProperty("poolNumber")
        @Schema(name = "Pool number", description = "The unique number for a pool request")
        private String poolNumber;

        @JsonProperty("attendanceDate")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(name = "Attendance date",
            description = "The date the pool has been requested for and when the jurors are expected to first attend "
                + "the court")
        private LocalDate attendanceDate;

        @JsonProperty("confirmedJurors")
        @Schema(name = "Confirmed From Bureau",
            description = "The number of pool members owned by the Bureau who have a status of 'Responded'")
        private int confirmedFromBureau;

        @JsonProperty("jurorsRequested")
        @Schema(name = "Requested From Bureau", description = "The number of jurors to be supplied by the Bureau")
        private int requestedFromBureau;

        @JsonProperty("poolCapacity")
        @Schema(name = "Pool capacity required by Court", description = "The total number of jurors required for a "
            + "given Pool")
        private int poolCapacity;

        @JsonProperty("jurorsInPool")
        @Schema(name = "Active Jurors in the Pool",
            description = "The number of pool members owned by Court who have a status of Responded, Panel or Juror")
        private long jurorsInPool;

        @JsonProperty("poolType")
        @Schema(name = "Type of Pool Requested",
            description = "The Type of the pool being requested, e.g. Crown Court")
        private String poolType;

    }

}
