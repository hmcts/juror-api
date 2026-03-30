package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.QCoronerPool;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Search criteria for Coroner Pool Request Filter")
public class CoronerPoolFilterRequestQuery implements IsPageable {

    @JsonProperty("pool_number")
    @Schema(name = "Pool number", description = "Number of pool to fetch members of")
    @PoolNumber
    private String poolNumber;

    @JsonProperty("location_code")
    @CourtLocationCode
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locationCode;

    @JsonProperty("requested_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The date this pool is being requested for, when the pool members are first expected to "
        + "attend court. Expected format is yyyy-MM-dd", example = "2023-01-31")
    private LocalDate requestedDate;

    @JsonProperty("requested_by")
    @Schema(description = "Name of the person who requested the pool")
    private String requestedBy;

    @JsonProperty("sort_method")
    private SortMethod sortMethod;

    @JsonProperty("sort_field")
    private CoronerPoolFilterRequestQuery.SortField sortField;

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
        POOL_NUMBER(QCoronerPool.coronerPool.poolNumber),
        COURT_NAME(QCoronerPool.coronerPool.courtLocation.locCourtName),
        REQUESTED_DATE(QCoronerPool.coronerPool.requestDate),
        REQUESTED_BY(QCoronerPool.coronerPool.name);

        private final Expression<? extends Comparable<?>> comparableExpression;

        SortField(Expression<? extends Comparable<?>> comparableExpression) {
            this.comparableExpression = comparableExpression;
        }
    }

}
