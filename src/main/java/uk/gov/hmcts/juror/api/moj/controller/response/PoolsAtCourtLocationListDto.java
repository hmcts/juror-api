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
import lombok.ToString;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for listing pools at a court location.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Pools at court location response")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PoolsAtCourtLocationListDto {

    @Schema(description = "List of pools at a court location")
    private List<PoolsAtCourtLocationDataDto> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    @Schema(description = "Pool at court location data")
    @ToString
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PoolsAtCourtLocationDataDto {

        @PoolNumber
        @Schema(name = "Pool number", description = "The unique number for a pool request")
        private String poolNumber;

        @Schema(name = "Jurors on trial from the Pool",
            description = "The number of jurors who are on a trial from the Pool")
        private int jurorsOnTrials;

        @Schema(name = "Jurors in attendance for the Pool not on trial",
            description = "The number of jurors who have attended the court for the Pool but are not on trial")
        private int jurorsInAttendance;

        @Schema(name = "Jurors on call at court", description = "The total number of jurors on call at the court")
        private int jurorsOnCall;

        @Schema(name = "other Jurors", description = "The number of jurors who are not in attendance and not on call")
        private int otherJurors;

        @Schema(name = "totalJurors", description = "The total number of Jurors in the Pool")
        private int totalJurors;

        @Schema(name = "Type of Pool Requested",
            description = "The Type of the pool being requested, e.g. Crown Court")
        private String poolType;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(name = "Service Start date",
            description = "The date the pool has been requested for and when the jurors are expected to first attend "
                + "the court")
        private LocalDate serviceStartDate;

    }

}
