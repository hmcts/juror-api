package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class PoolSummaryResponseDto {

    @JsonProperty("poolDetails")
    @Schema(name = "Pool Details", description = "Data related to the Pool Request")
    private PoolDetails poolDetails;

    @JsonProperty("bureauSummoning")
    @Schema(name = "Bureau Summoning Summary", description = "Statistics relevant to the Bureau to manage the Pool")
    private BureauSummoning bureauSummoning;

    @JsonProperty("poolSummary")
    @Schema(name = "Pool Summary", description = "Pool statistics relevant to both Court and Bureau users")
    private PoolSummary poolSummary;

    @JsonProperty("additionalStatistics")
    @Schema(name = "Additional Statistics", description = "Additional data related to the pool to support Pool "
        + "Management")
    private AdditionalStatistics additionalStatistics;

    public PoolSummaryResponseDto() {
        this.poolDetails = new PoolDetails();
        this.bureauSummoning = new BureauSummoning();
        this.poolSummary = new PoolSummary();
        this.additionalStatistics = new AdditionalStatistics();
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class PoolDetails {
        @JsonProperty("poolNumber")
        @Schema(name = "Pool Number", description = "9 digit numeric String to identify a Pool Request")
        private String poolNumber;

        @JsonProperty("courtName")
        @Schema(name = "Court Name", description = "English name for a given Court Location")
        private String courtName;

        @JsonProperty("locCode")
        @Schema(name = "Court Location Code", description = "3 digit numeric String to identify a Court Location")
        private String courtLocationCode;

        @JsonProperty("courtStartDate")
        @Schema(name = "Court Start Date", description = "The date this Pool has been requested for, when jurors will"
            + " first attend court")
        private String courtStartDate;

        @JsonProperty("poolType")
        @Schema(name = "Pool Type", description = "The type of pool")
        private String poolType;

        @JsonProperty("additionalRequirements")
        @Schema(name = "Additional Requirements", description = "A String representation of any special requirements,"
            + " formatted as a comma separated list")
        private String additionalRequirements;

        @JsonProperty("isActive")
        @Schema(name = "Is Active", description = "Indicates whether the status of the pool is Active (true) or "
            + "Requested (false))")
        private Boolean isActive;

        @JsonProperty("is_nil_pool")
        @Schema(name = "Is Nil Pool",
            description = "Indicates whether the the pool is a nil pool or not")
        private boolean isNilPool;

        @JsonProperty("current_owner")
        @Schema(name = "Current Owner", description = "Current owner (3 digit code) of the juror record")
        private String currentOwner;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class BureauSummoning {

        @JsonProperty("required")
        @Schema(name = "Requested From Bureau", description = "The number of jurors to be supplied by the Bureau")
        private Integer requestedFromBureau;

        @JsonProperty("confirmed")
        @Schema(name = "Confirmed From Bureau", description = "The number of pool members owned by the Bureau who "
            + "have a status of 'Responded'")
        private int confirmedFromBureau;

        @JsonProperty("unavailable")
        @Schema(name = "Unavailable", description = "The number of pool members owned by the Bureau who do not have a"
            + " status of 'Responded', 'Summoned' or 'Additional Info'")
        private int unavailable;

        @JsonProperty("notResponded")
        @Schema(name = "Unresolved", description = "The number of pool members who have a status of 'Summoned' or "
            + "'Additional Info'")
        private int unresolved;

        @JsonProperty("surplus")
        @Schema(name = "Surplus", description = "Either 0 or calculated as Requested From Bureau minus Confirmed, "
            + "whichever is greater. Cannot be below 0.")
        private int surplus;

        @JsonProperty("totalSummoned")
        @Schema(name = "Total Summoned", description = "The sum of all Bureau owned Pool Members, regardless of state.")
        private int totalSummoned;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class PoolSummary {

        @JsonProperty("currentPoolSize")
        @Schema(name = "Current Pool Size", description = "Calculated value of court supplied Pool Members (e.g. "
            + "court deferrals) plus Bureau owned Pool Members with a status of 'Responded'")
        private int currentPoolSize;

        @JsonProperty("requiredPoolSize")
        @Schema(name = "Required Pool Size", description = "The total number of jurors required for a given Pool")
        private int requiredPoolSize;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class AdditionalStatistics {

        @JsonProperty("courtSupply")
        @Schema(name = "Court Supply", description = "The number of Pool Members owned by the court (e.g. court "
            + "deferrals) with a status of 'Responded'")
        private int courtSupply;
    }

}
