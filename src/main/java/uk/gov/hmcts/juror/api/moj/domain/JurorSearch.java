package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.validation.ValidateIf;
import uk.gov.hmcts.juror.api.validation.ValidateIfTrigger;

@ValidateIfTrigger(classToValidate = JurorSearch.class)
@Data
@Builder
public class JurorSearch {

    @ValidateIf(fields = {"jurorNumber", "postcode"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "postcode"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String jurorName;

    @ValidateIf(fields = {"jurorName", "postcode"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorName", "postcode"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String jurorNumber;

    @ValidateIf(fields = {"jurorNumber", "jurorName"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "jurorName"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private String postcode;
}
