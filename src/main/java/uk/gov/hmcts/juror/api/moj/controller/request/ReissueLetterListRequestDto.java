package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;
import uk.gov.hmcts.juror.api.validation.ValidateIf;
import uk.gov.hmcts.juror.api.validation.ValidateIfTrigger;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Reissue Juror letter list request by juror number or pool number (or only pending letters)")
@ValidateIfTrigger(classToValidate = ReissueLetterListRequestDto.class)
public class ReissueLetterListRequestDto implements Serializable {

    @JsonProperty("juror_number")
    @JurorNumber
    @Schema(description = "Unique juror number")
    @ValidateIf(fields = {"poolNumber", "jurorName", "jurorPostcode", "showAllQueued"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"poolNumber", "jurorName", "jurorPostcode", "showAllQueued"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String jurorNumber;

    @JsonProperty("pool_number")
    @PoolNumber
    @Schema(description = "Unique pool number")
    @ValidateIf(fields = {"jurorNumber", "jurorName", "jurorPostcode", "showAllQueued"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "jurorName", "jurorPostcode", "showAllQueued"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String poolNumber;

    @JsonProperty("juror_name")
    @ValidateIf(fields = {"jurorNumber", "poolNumber", "jurorPostcode", "showAllQueued"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "poolNumber", "jurorPostcode", "showAllQueued"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String jurorName;

    @JsonProperty("juror_postcode")
    @ValidateIf(fields = {"jurorNumber", "poolNumber", "jurorName", "showAllQueued"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "poolNumber", "jurorName", "showAllQueued"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    @Pattern(regexp = ValidationConstants.POSTCODE_REGEX)
    private String jurorPostcode;

    @JsonProperty(value = "letter_type", required = true)
    @NotNull
    @Schema(name = "letter type", description = "Code indicating the type of letter to be sent")
    private LetterType letterType;

    @JsonProperty("show_all_queued")
    @Schema(description = "Flag to indicate if only queued letters should be listed")
    @ValidateIf(fields = {"jurorNumber", "poolNumber", "jurorName", "jurorPostcode"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "poolNumber", "jurorName", "jurorPostcode"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private Boolean showAllQueued;

}