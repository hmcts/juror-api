package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PoolSummaryResponseDto {
    @Schema(name = "Pool Details", description = "Data related to the Pool Request")
    private PoolDetails poolDetails;

    @Schema(name = "Bureau Summoning Summary", description = "Statistics relevant to the Bureau to manage the Pool")
    private BureauSummoning bureauSummoning;

    @Schema(name = "Pool Summary", description = "Pool statistics relevant to both Court and Bureau users")
    private PoolSummary poolSummary;

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
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PoolDetails {
        @Schema(name = "Pool Number", description = "9 digit numeric String to identify a Pool Request")
        private String poolNumber;

        @Schema(name = "Court Name", description = "English name for a given Court Location")
        private String courtName;

        @Schema(name = "Court Location Code", description = "3 digit numeric String to identify a Court Location")
        private String courtLocationCode;

        @Schema(name = "Court Start Date", description = "The date this Pool has been requested for, when jurors will"
            + " first attend court")
        private String courtStartDate;

        @Schema(name = "Additional Requirements", description = "A String representation of any special requirements,"
            + " formatted as a comma separated list")
        private String additionalRequirements;

        @Schema(name = "Is Active", description = "Indicates whether the status of the pool is Active (true) or "
            + "Requested (false))")
        private Boolean isActive;

        @Schema(name = "Is Nil Pool",
            description = "Indicates whether the the pool is a nil pool or not")
        private boolean isNilPool;

        @Schema(name = "Current Owner", description = "Current owner (3 digit code) of the juror record")
        private String currentOwner;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BureauSummoning {

        @Schema(name = "Requested From Bureau", description = "The number of jurors to be supplied by the Bureau")
        private Integer requestedFromBureau;

        @Schema(name = "Confirmed From Bureau", description = "The number of pool members owned by the Bureau who "
            + "have a status of 'Responded'")
        private int confirmedFromBureau;

        @Schema(name = "Unavailable", description = "The number of pool members owned by the Bureau who do not have a"
            + " status of 'Responded', 'Summoned' or 'Additional Info'")
        private int unavailable;

        @Schema(name = "Unresolved", description = "The number of pool members who have a status of 'Summoned' or "
            + "'Additional Info'")
        private int unresolved;

        @Schema(name = "Surplus", description = "Either 0 or calculated as Requested From Bureau minus Confirmed, "
            + "whichever is greater. Cannot be below 0.")
        private int surplus;

        @Schema(name = "Total Summoned", description = "The sum of all Bureau owned Pool Members, regardless of state.")
        private int totalSummoned;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PoolSummary {
        @Schema(name = "Current Pool Size", description = "Calculated value of court supplied Pool Members (e.g. "
            + "court deferrals) plus Bureau owned Pool Members with a status of 'Responded'")
        private int currentPoolSize;

        @Schema(name = "Required Pool Size", description = "The total number of jurors required for a given Pool")
        private int requiredPoolSize;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AdditionalStatistics {
        @Schema(name = "Court Supply", description = "The number of Pool Members owned by the court (e.g. court "
            + "deferrals) with a status of 'Responded'")
        private int courtSupply;
    }
}
