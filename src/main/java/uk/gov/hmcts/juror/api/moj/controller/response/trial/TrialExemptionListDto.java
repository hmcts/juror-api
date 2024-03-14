package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "A List of trials for issuing exemptions")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TrialExemptionListDto  {
    @JsonProperty("case_number")
    @Schema(description = "A Unique number representing the trial at a given court")
    private String trialNumber;

    @JsonProperty("parties")
    @Schema(description = "A list of the accused member(s)")
    private String defendants;

    @Schema(description = "Type of trial e.g. Crown or Civil")
    private String trialType;

    private String judge;

    @Schema(description = "Date the trial service began")
    private LocalDate startDate;

    @Schema(description = "Date the trial ended")
    private LocalDate endDate;
}
