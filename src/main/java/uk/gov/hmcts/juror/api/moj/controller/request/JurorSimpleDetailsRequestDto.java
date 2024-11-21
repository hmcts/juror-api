package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Simple information request for a number of jurors")
public class JurorSimpleDetailsRequestDto {

    @JsonProperty("juror_numbers")
    @Size(min = 1)
    @Schema(name = "Juror numbers", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "Array of Juror numbers to move to update")
    private List<String> jurorNumbers;

    @JsonProperty("court_code")
    @NotBlank
    @Length(min = 3, max = 3)
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locationCode;


}
