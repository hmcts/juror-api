package uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@Schema(description = "Request DTO to bulk disqualify jurors due to age")
public class BulkDisqualifyRequestDto {

    @Schema(description = "List of age-disqualified jurors to disqualify")
    @NotEmpty
    @Valid
    private List<AgeDisqualifiedJurorDto> jurors;

    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Getter
    @Setter
    @Schema(description = "Details of a juror to be disqualified due to age")
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
