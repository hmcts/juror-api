package uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Response DTO when requesting available pools data for a court location")
public class AvailablePoolsInCourtLocationDto {

    @JsonProperty("availablePools")
    @Schema(name = "Available Pools", description = "List available active pools for the given court location, "
        + "including number of Jurors needed/surplus")
    private List<AvailablePoolsDto> availablePools = new ArrayList<>();

    @NoArgsConstructor
    @Setter
    @Getter
    public static class AvailablePoolsDto {

        @JsonProperty("poolNumber")
        @Schema(name = "Pool number", description = "The unique number for a pool request")
        private String poolNumber;

        @JsonProperty("serviceStartDate")
        @Schema(description = "The date the pool has been requested for. When the Jurors are first expected to attend"
            + " court")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate serviceStartDate;

        @JsonProperty("utilisation")
        @Schema(name = "utilisation", description = "The number relating to how many pool members are needed/surplus "
            + "to requirements for the given pool")
        private long utilisation;

        @JsonProperty("utilisationDescription")
        @Schema(name = "Utilisation Description", description = "Whether the utilisation number reflects jurors "
            + "needed or "
            + "the number of surplus jurors.")
        private PoolUtilisationDescription utilisationDescription;

    }

}
