package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;
import uk.gov.hmcts.juror.api.validation.ValidateIf;
import uk.gov.hmcts.juror.api.validation.ValidateIfTrigger;

@ValidateIfTrigger(classToValidate = JurorPoolSearch.class)
@Data
@Builder
@Getter
public class JurorPoolSearch implements IsPageable {

    @ValidateIf(fields = {"jurorNumber", "postcode"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "postcode", "poolNumber"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    @JsonProperty("juror_name")
    private String jurorName;

    @ValidateIf(fields = {"jurorName", "postcode"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorName", "postcode", "poolNumber"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    @JsonProperty("juror_number")
    private String jurorNumber;

    @ValidateIf(fields = {"jurorNumber", "jurorName"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"jurorNumber", "jurorName", "poolNumber"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    @JsonProperty("postcode")
    private String postcode;

    @ValidateIf(fields = {"postcode", "jurorNumber", "jurorName"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"postcode", "jurorNumber", "jurorName"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    @JsonProperty("pool_number")
    private String poolNumber;

    @Min(1)
    @JsonProperty("page_number")
    private long pageNumber;

    @Min(1)
    @JsonProperty("page_limit")
    private long pageLimit;

    @JsonProperty("sort_method")
    private SortMethod sortMethod;

    @JsonProperty("sort_field")
    private SortField sortField;


    @Getter
    public enum SortField implements SortMethod.HasComparableExpression {
        JUROR_NUMBER(QJuror.juror.jurorNumber),
        FIRST_NAME(QJuror.juror.firstName),
        LAST_NAME(QJuror.juror.lastName),
        POSTCODE(QJuror.juror.postcode),
        COMPLETION_DATE(QJuror.juror.completionDate);

        private final ComparableExpressionBase<?> comparableExpression;

        SortField(ComparableExpressionBase<?> comparableExpression) {
            this.comparableExpression = comparableExpression;
        }
    }
}
