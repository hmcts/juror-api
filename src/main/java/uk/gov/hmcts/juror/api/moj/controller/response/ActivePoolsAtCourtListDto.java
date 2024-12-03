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
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for listing active pools at a court location.
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Active Pools at court location response")
public class ActivePoolsAtCourtListDto {

    @JsonProperty("active_pools_at_court_location")
    @Schema(description = "List of active pools at a court location")
        private List<ActivePoolsAtCourtLocationDataDto> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    @Schema(description = "Active pool at court location date")
    @ToString
    public static class ActivePoolsAtCourtLocationDataDto {

        @JsonProperty("pool_number")
        @PoolNumber
        @Schema(name = "Pool number", description = "The unique number for a pool request")
            private String poolNumber;

        @JsonProperty("jurors_on_trials")
        @Schema(name = "Jurors on trial from the Pool",
                description = "The number of jurors who are on a trial from the Pool")
            private int jurorsOnTrials;

        @JsonProperty("jurors_in_attendance")
        @Schema(name = "Jurors in attendance for the Pool not on trial",
                description = "The number of jurors who have attended the court for the Pool but are not on trial")
            private int jurorsInAttendance;

        @JsonProperty("jurors_on_call")
        @Schema(name = "Jurors on call at court", description = "The total number of jurors on call at the court")
            private int jurorsOnCall;

        @JsonProperty("other_jurors")
        @Schema(name = "other Jurors", description = "The number of jurors who are not in attendance and not on call")
            private int otherJurors;

        @JsonProperty("total_jurors")
        @Schema(name = "totalJurors", description = "The total number of Jurors in the Pool")
            private int totalJurors;

        @JsonProperty("pool_type")
        @Schema(name = "Type of Pool Requested",
                description = "The Type of the pool being requested, e.g. Crown Court")
            private String poolType;

        @JsonProperty("service_start_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(name = "Service Start date",
                description = "The date the pool has been requested for and when the jurors are expected to "
                    + " first attend the court")
            private LocalDate serviceStartDate;

    }
}
