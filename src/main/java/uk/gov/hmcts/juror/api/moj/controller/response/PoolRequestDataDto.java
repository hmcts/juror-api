package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for listing pool requests.
 */
@AllArgsConstructor
@Getter
@Schema(description = "Pool Request data")
@ToString
@Builder
public class PoolRequestDataDto {

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
        @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
        @Schema(name = "Attendance date",
            description = "The date the pool has been requested for and when the jurors are expected to first attend "
                + "the court")
        private LocalDate attendanceDate;
}
