package uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@Builder
@Schema(description = "Response DTO for bulk age disqualification operation")
public class BulkDisqualifyResponseDto {

    @SuppressWarnings("PMD.RedundantFieldInitializer")
    @Schema(description = "Count of jurors successfully disqualified")
    @Builder.Default
    private int disqualifiedCount = 0;

    @Schema(description = "Jurors successfully disqualified")
    @Builder.Default
    private List<DisqualifiedJurorDto> disqualified = new ArrayList<>();

    @Schema(description = "Jurors that could not be disqualified")
    @Builder.Default
    private List<DisqualifiedJurorDto> failedToDisqualify = new ArrayList<>();

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Details of a juror processed in the bulk disqualification")
    public static class DisqualifiedJurorDto {

        @Schema(description = "9-digit juror number")
        private String jurorNumber;

        @Schema(description = "Juror date of birth")
        @JsonFormat(pattern = "dd/MM/yyyy")
        private LocalDate dob;

        @Schema(description = "The service start date of the pool the juror is currently assigned to")
        @JsonFormat(pattern = "dd/MM/yyyy")
        private LocalDate currentServiceStartDate;

        @Schema(description = "The service start date of the pool the juror was being deferred to")
        @JsonFormat(pattern = "dd/MM/yyyy")
        private LocalDate newDate;
    }
}
