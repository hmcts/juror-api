package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

@Data
@Builder
@Schema(description = "Juror Record filter request")
public class JurorRecordFilterRequestQuery implements IsPageable {

    @JsonProperty("juror_number")
    @Schema(name = "Juror number", description = "Partial juror number to search for")
    @Length(max = 9)
    @Pattern(regexp = "[0-9]*")
    private String jurorNumber;

    @JsonProperty("juror_name")
    @Schema(name = "Juror name", description = "First name and or Last name to search for")
    @Length(max = 40)
    private String jurorName;

    @JsonProperty("postcode")
    @Schema(name = "Postcode", description = "Postcode to search for")
    private String postcode;

    @JsonProperty("pool_number")
    @Schema(name = "Pool number", description = "Pool Number to search for")
    @PoolNumber
    private String poolNumber;

    @JsonProperty("sort_method")
    private SortMethod sortMethod;

    @JsonProperty("sort_field")
    private JurorRecordFilterRequestQuery.SortField sortField;

    @Min(1)
    @JsonProperty("page_limit")
    @Schema(name = "Page limit", description = "Number of items per page")
    private long pageLimit;

    @Min(1)
    @JsonProperty("page_number")
    @Schema(name = "Page number", description = "Page number to fetch (1-indexed)")
    private long pageNumber;

    @Getter
    public enum SortField implements SortMethod.HasComparableExpression {
        JUROR_NUMBER(QJuror.juror.jurorNumber),
        JUROR_NAME(QJuror.juror.firstName.concat(" ").concat(QJuror.juror.lastName)),
        POSTCODE(QJuror.juror.postcode),
        POOL_NUMBER(QJurorPool.jurorPool.pool.poolNumber),
        COURT_NAME(QJurorPool.jurorPool.pool.poolRequest.courtLocation.name),
        STATUS(QJurorPool.jurorPool.status.status);

        private final Expression<? extends Comparable<?>> comparableExpression;

        SortField(Expression<? extends Comparable<?>> comparableExpression) {
            this.comparableExpression = comparableExpression;
        }
    }
}
