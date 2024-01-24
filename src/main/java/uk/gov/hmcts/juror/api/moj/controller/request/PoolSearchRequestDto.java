package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO specifying the inbound search criteria for Pool Request Search.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "DTO specifying the search criteria for Pool Request Search")
public class PoolSearchRequestDto {

    @JsonProperty("poolNumber")
    @Size(min = 3, max = 9)
    @NumericString
    @Schema(name = "Unique pool number",
        description = "If populated then expect a minimum of 3 characters to improve performance of partial pool "
            + "number search")
    private String poolNumber;

    @JsonProperty("locCode")
    @Size(min = 3, max = 3)
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locCode;

    @JsonProperty("serviceStartDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The date this pool is being created for, when the pool members are first expected to "
        + "attend court. Expected format is yyyy-MM-dd",
        example = "2023-01-31")
    private LocalDate serviceStartDate;

    @JsonProperty("offset")
    @Min(0)
    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "Zero indexed numeric offset value to support "
        + "pagination",
        description = "For the first page of results (page 1) the offset value will be 0. "
            + "For the second page of results (page 2) the offset value will be 1.")
    private int offset;

    @JsonProperty("sortColumn")
    @Schema(description = "The name of the column to sort on")
    private SortColumn sortColumn;


    @JsonProperty("sortDirection")
    @Schema(description = "Whether the results should be in ascending (ASC) or descending (DESC) order")
    private SortDirection sortDirection;

    @JsonProperty("poolStatus")
    @Schema(description = "Whether the pool is currently Requested, Active or Completed")
    private List<PoolStatus> poolStatus;

    @JsonProperty("poolStage")
    @Schema(description = "Whether the pool is currently with the Bureau or the Court")
    private List<PoolStage> poolStage;

    @JsonProperty("poolType")
    @Schema(description = "Which type of court the pool was requested for")
    private List<String> poolType;


    public enum SortColumn {
        POOL_NO,
        COURT_NAME,
        POOL_STAGE,
        POOL_STATUS,
        POOL_TYPE,
        START_DATE
    }

    public enum PoolStatus {
        REQUESTED,
        ACTIVE,
        COMPLETED
    }

    public enum PoolStage {
        COURT,
        BUREAU
    }
}
