package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "A List of trials")
public class TrialListDto {
    @JsonProperty("trial_number")
    private String trialNumber;

    @JsonProperty("parties")
    private String defendants;

    @JsonProperty("trial_type")
    private String trialType;

    @JsonProperty("judge")
    private String judge;

    @JsonProperty("courtroom")
    private String courtroom;

    @JsonProperty("court")
    private String courtLocationName;

    @JsonProperty("court_location")
    private String courtLocationCode;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("is_active")
    private Boolean isActive;
}
