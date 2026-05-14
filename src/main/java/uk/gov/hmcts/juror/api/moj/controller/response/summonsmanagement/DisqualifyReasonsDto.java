package uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Juror disqualification reasons")
@Getter
@Builder
public class DisqualifyReasonsDto {
    @Schema(name = "Disqualify reasons", description = "List of disqualify reasons")
    @NotBlank
    @Singular("disqualifyReason")
    private List<DisqualifyReasons> disqualifyReasons;

    @AllArgsConstructor
    @Schema(description = "Disqualification details")
    @Builder
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DisqualifyReasons {
        @Schema(description = "Disqualification code")
        @NotBlank
        private String code;

        @Schema(description = "Disqualification description")
        @NotBlank
        private String description;

        @Schema(description = "Heritage disqualification code")
        @NotBlank
        private String heritageCode;

        @Schema(description = "Heritage disqualification description")
        @NotBlank
        private String heritageDescription;
    }
}
