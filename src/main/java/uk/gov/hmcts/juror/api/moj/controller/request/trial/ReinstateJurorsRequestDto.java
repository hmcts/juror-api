package uk.gov.hmcts.juror.api.moj.controller.request.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "The list containing the details of jurors to be reinstated to a trial")
public class ReinstateJurorsRequestDto {
    @JsonProperty("jurors")
    @NotEmpty
    private List<@JurorNumber String> jurors;

    @JsonProperty("trial_number")
    @NotBlank
    @Schema(name = "Trial number", description = "Identification for the running trial")
    @Length(max = 16)
    private String trialNumber;

    @JsonProperty("court_location_code")
    @NotBlank
    @CourtLocationCode
    @Schema(name = "Court location Code", description = "3-digit code representing the court location")
    private String courtLocationCode;

}
