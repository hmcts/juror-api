package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;
import uk.gov.hmcts.juror.api.validation.ValidateIf;
import uk.gov.hmcts.juror.api.validation.ValidateIfTrigger;

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
    @ValidateIf(fields = {"poolNumber", "showAllQueued"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"poolNumber", "showAllQueued"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String jurorNumber;

    @JsonProperty("pool_number")
    @PoolNumber
    @Schema(description = "Unique pool number")
    @ValidateIf(fields = {"jurorNumber", "showAllQueued"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "showAllQueued"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String poolNumber;

    @JsonProperty(value = "letter_type", required = true)
    @NotNull
    @Schema(name = "letter type", description = "Code indicating the type of letter to be sent")
    private LetterType letterType;

    @JsonProperty(value = "show_all_queued")
    @Schema(description = "Flag to indicate if only queued letters should be listed")
    @ValidateIf(fields = {"jurorNumber", "poolNumber"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "poolNumber"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private Boolean showAllQueued;

    public Boolean isShowAllQueued() {
        return showAllQueued;
    }
}