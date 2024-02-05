package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.validation.ValidateIf;
import uk.gov.hmcts.juror.api.validation.ValidateIfTrigger;

@ValidateIfTrigger(classToValidate = JurorPoolSearch.class)
@Data
@Builder
public class JurorPoolSearch {

    @ValidateIf(fields = {"jurorNumber", "postcode"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "postcode", "poolNumber"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String jurorName;

    @ValidateIf(fields = {"jurorName", "postcode"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorName", "postcode", "poolNumber"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String jurorNumber;

    @ValidateIf(fields = {"jurorNumber", "jurorName"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "jurorName", "poolNumber"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String postcode;

    @ValidateIf(fields = {"postcode", "jurorNumber", "jurorName"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"postcode", "jurorNumber", "jurorName"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String poolNumber;

    @Min(1)
    @JsonProperty("page_number")
    private int pageNumber;

    @Min(1)
    @JsonProperty("page_limit")
    private int pageLimit;
}
