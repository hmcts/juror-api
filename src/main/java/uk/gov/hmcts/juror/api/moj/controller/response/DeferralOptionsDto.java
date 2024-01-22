package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@ToString
@Setter
@Schema(description = "Response DTO when requesting pool summary data for proposed deferral dates")
public class DeferralOptionsDto {

    @JsonProperty("deferralPoolsSummary")
    @Schema(name = "Deferral Pools Summary", description = "A list of active pools available for the provided "
        + "deferral dates")
    private List<OptionSummaryDto> deferralPoolsSummary = new ArrayList<>();

    @NoArgsConstructor
    @Getter
    @ToString
    @Setter
    public static class OptionSummaryDto {

        @JsonProperty("weekCommencing")
        @Schema(name = "Week Commencing", description = "The first day of the week (Monday) for the requested "
            + "deferral date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate weekCommencing;

        @JsonProperty("deferralOptions")
        @Schema(name = "Deferral Options", description = "List active pools for the given deferral date (if any), "
            + "including utilisation statistics")
        private List<DeferralOptionDto> deferralOptions = new ArrayList<>();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class DeferralOptionDto {

        @JsonProperty("poolNumber")
        @Schema(name = "Pool number", description = "The unique number for a pool request")
        private String poolNumber;

        @JsonProperty("serviceStartDate")
        @Schema(description = "The date the pool has been requested for. When the Jurors are first expected to attend "
            + "court")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate serviceStartDate;

        @JsonProperty("utilisation")
        @Schema(name = "Utilisation", description = "The number relating to how many pool members are needed/surplus "
            + "to requirements for the given pool or are in deferral maintenance for the given date")
        private long utilisation;

        @JsonProperty("utilisationDescription")
        @Schema(name = "Utilisation Description", description = "Whether the utilisation number reflects jurors "
            + "needed, "
            + "the number of surplus jurors, or the number already in deferral maintenance for the given date")
        private PoolUtilisationDescription utilisationDescription;

    }


}
