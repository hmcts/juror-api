package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

/**
 * Request DTO for Juror Address details, line 1 and line 4 are mandatory.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Juror Address details")
@Accessors(chain = true)
public class JurorAddressDto {

    @JsonProperty("line_one")
    @NotBlank(message = "Address line 1 cannot be blank")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Address line 1")
    private String lineOne;

    @JsonProperty("line_two")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Address line 2")
    private String lineTwo;

    @JsonProperty("line_three")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "AddressLine 3")
    private String lineThree;

    @JsonProperty("town")
    @Length(max = 35)
    @NotBlank(message = "Address town/city cannot be blank")
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Address line 4 - Town")
    private String town;

    @JsonProperty("county")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Address line 5 - County")
    private String county;

    @JsonProperty("postcode")
    @NotBlank
    @Length(max = 8)
    @Pattern(regexp = POSTCODE_REGEX)
    @Schema(description = "Postcode")
    private String postcode;

    public static JurorAddressDto from(Juror juror) {
        return JurorAddressDto.builder()
            .lineOne(juror.getAddressLine1())
            .lineTwo(juror.getAddressLine2())
            .lineThree(juror.getAddressLine3())
            .town(juror.getAddressLine4())
            .county(juror.getAddressLine5())
            .postcode(juror.getPostcode())
            .build();
    }
}
