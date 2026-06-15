package uk.gov.hmcts.juror.api.moj.controller.request.letter.court;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;
import uk.gov.hmcts.juror.api.validation.ValidateIf;
import uk.gov.hmcts.juror.api.validation.ValidateIfTrigger;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Search criteria for finding jurors eligible for a given letter")
@ValidateIfTrigger(classToValidate = CourtLetterListRequestDto.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtLetterListRequestDto {

    @JurorNumber
    @Schema(description = "Unique juror number")
    @ValidateIf(fields = {"poolNumber"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"poolNumber", "jurorName", "jurorPostcode"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String jurorNumber;

    @Schema(description = "Juror's last name")
    @ValidateIf(fields = {"poolNumber"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"poolNumber", "jurorNumber", "jurorPostcode"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String jurorName;

    @Schema(description = "Juror's home postcode")
    @ValidateIf(fields = {"poolNumber"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"poolNumber", "jurorNumber", "jurorName"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String jurorPostcode;

    @PoolNumber
    @Schema(description = "Unique pool number")
    @ValidateIf(fields = {"jurorNumber", "jurorName", "jurorPostcode"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "jurorName", "jurorPostcode"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String poolNumber;

    @Schema(description = "Flag to indicate if previously printed letters should be included in the list")
    @ValidateIf(fields = {"jurorNumber", "jurorName", "jurorPostcode", "poolNumber"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    private Boolean includePrinted;

    @Schema(description = "Specifies the court letter type the request is for")
    @NotNull
    private CourtLetterType letterType;

    public boolean isIncludePrinted() {
        return Boolean.TRUE.equals(this.includePrinted);
    }
}
