package uk.gov.hmcts.juror.api.moj.controller.request.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Trial End Information")
public class EndTrialDto {
    @JsonProperty("trial_end_date")
    @NotNull
    LocalDate trialEndDate;

    @JsonProperty("trial_number")
    @NotBlank
    String trialNumber;

    @JsonProperty("location_code")
    @NotBlank
    String locationCode;
}
