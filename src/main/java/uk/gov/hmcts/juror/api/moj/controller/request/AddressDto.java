package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;


/**
 * Request DTO for base Address details. Juror address details extend this class.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Schema(description = "Juror address details")
public class AddressDto {

    @JsonProperty("address_line_two")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 2")
    private String addressLineTwo;

    @JsonProperty("address_line_three")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 3")
    private String addressLineThree;

    @JsonProperty("address_county")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 5")
    private String addressCounty;

}
