package uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;

/**
 * Payload (response message) containing a list of disqualification reasons (codes).
 */
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Juror disqualification reasons")
@Getter
@Builder
public class DisqualifyReasonsDto {
    @JsonProperty("disqualifyReasons")
    @Schema(name = "Disqualify reasons", description = "List of disqualify reasons")
    @NotBlank
    @Singular("disqualifyReason")
    private List<DisqualifyReasons> disqualifyReasons;

    @AllArgsConstructor
    @Schema(description = "Disqualification details")
    @Builder
    @Getter
    public static class DisqualifyReasons {
        @JsonProperty("code")
        @Schema(description = "Disqualification code")
        @NotBlank
        private String code;

        @JsonProperty("description")
        @Schema(description = "Disqualification description")
        @NotBlank
        private String description;

        @JsonProperty("heritageCode")
        @Schema(description = "Heritage disqualification code")
        @NotBlank
        private String heritageCode;

        @JsonProperty("heritageDescription")
        @Schema(description = "Heritage disqualification description")
        @NotBlank
        private String heritageDescription;
    }
}
