package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for listing pool requests.
 */
@AllArgsConstructor
@Getter
@Builder
@Schema(description = "Pool request list response")
public class PoolRequestListDto {

//    @JsonProperty("poolRequests")
//    @Schema(description = "List of pool requests")
//    private List<PoolRequestDataDto> data;
//
//    @AllArgsConstructor
//    @Getter
//    @Schema(description = "Pool Request data")
//    @ToString
//    public static class PoolRequestDataDto {
//
        @JsonProperty("courtName")
        @Schema(name = "Court name", description = "Name for a given court location")
        private String courtName;

        @JsonProperty("poolNumber")
        @Schema(name = "Pool number", description = "The unique number for a pool request")
        private String poolNumber;

        @JsonProperty("poolType")
        @Schema(name = "Pool type", description = "The type of court the pool is being requested for")
        private String poolType;

        @JsonProperty("numberRequested")
        @Schema(name = "Jurors requested", description = "The total number of jurors requested for a given pool")
        private int numberRequested;

        @JsonProperty("attendanceDate")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(name = "Attendance date",
            description = "The date the pool has been requested for and when the jurors are expected to first attend "
                + "the court")
        private LocalDate attendanceDate;
//
//        /**
//         * Initialise an instance of this DTO class using a PoolRequest object to populate its properties.
//         *
//         * @param poolRequest an object representation of a PoolRequest record from the database
//         */
//        public PoolRequestDataDto(PoolRequest poolRequest) {
//            this.courtName = poolRequest.getCourtLocation().getName();
//            this.poolNumber = poolRequest.getPoolNumber();
//            this.poolType = poolRequest.getPoolType().getPoolType();
//            this.numberRequested = poolRequest.getNumberRequested();
//            this.attendanceDate = poolRequest.getReturnDate();
//        }

//    }

}
