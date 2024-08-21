package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;

@Data
@Builder
@Schema(description = "Pools requested pagination filtering and sorting")
public class PoolRequestedFilterQuery implements IsPageable {

    @JsonProperty("sort_method")
    private SortMethod sortMethod;

    @JsonProperty("sort_field")
    private PoolRequestedFilterQuery.SortField sortField;

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
        POOL_NUMBER(QPoolRequest.poolRequest.poolNumber),
        COURT_NAME(QPoolRequest.poolRequest.courtLocation.name),
        JURORS_REQUESTED(QPoolRequest.poolRequest.numberRequested),
        POOL_TYPE(QPoolRequest.poolRequest.poolType.poolType),
        RETURN_DATE(QPoolRequest.poolRequest.returnDate);

        private final Expression<? extends Comparable<?>> comparableExpression;

        SortField(Expression<? extends Comparable<?>> comparableExpression) {
            this.comparableExpression = comparableExpression;
        }
    }
}
