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
@Schema(description = "Response DTO containing eligible count and age-disqualified juror details "
    + "for a deferral operation")
public class DeferralAgeDisqualificationResponseDto {

    @Schema(description = "Count of jurors successfully processed")
    @Builder.Default
    private int eligible = 0;

    @Schema(description = "Jurors disqualified due to age (will be 76+ on service start date)")
    @Builder.Default
    private List<AgeDisqualifiedJurorDto> ageDisqualified = new ArrayList<>();

    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Getter
    @Setter
    @Builder
    @Schema(description = "Details of a juror disqualified due to age")
    public static class AgeDisqualifiedJurorDto {

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
