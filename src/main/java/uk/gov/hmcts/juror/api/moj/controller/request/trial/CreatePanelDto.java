package uk.gov.hmcts.juror.api.moj.controller.request.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "create panel request information")
public class CreatePanelDto {
    @JsonProperty("trial_number")
    @NotBlank
    @Length(max = 16)
    private String trialNumber;

    @JsonProperty("number_requested")
    @NotNull
    private int numberRequested;

    @JsonProperty("pool_numbers")
    private Optional<List<String>> poolNumbers;

    @JsonProperty("court_location_code")
    @NotBlank
    @Length(min = 3, max = 3)
    private String courtLocationCode;

}
