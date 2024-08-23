package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

@Data
@Builder
public class ActivePoolFilterQuery implements IsPageable {

    @JsonProperty("loc_code")
    @CourtLocationCode
    private String locCode;

    private String tab;

    @JsonProperty("sort_method")
    private SortMethod sortMethod;

    @JsonProperty("sort_field")
    private SortField sortField;

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
        TOTAL_NUMBER_REQUESTED(QPoolRequest.poolRequest.totalNoRequired),
        JURORS_IN_POOL(QJurorPool.jurorPool.isActive.count()),
        COURT_NAME(QPoolRequest.poolRequest.courtLocation.name),
        SERVICE_START_DATE(QPoolRequest.poolRequest.returnDate),
        POOL_TYPE(QPoolRequest.poolRequest.poolType.poolType),
        POOL_CAPACITY(QPoolRequest.poolRequest.totalNoRequired);

        private final Expression<? extends Comparable<?>> comparableExpression;

        SortField(Expression<? extends Comparable<?>> comparableExpression) {
            this.comparableExpression = comparableExpression;
        }
    }
}
