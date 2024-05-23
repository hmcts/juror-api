package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class UnpaidExpenseSummaryRequestDto implements IsPageable {

    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate from;
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate to;

    @Min(1)
    @JsonProperty("page_number")
    private long pageNumber;

    @Min(1)
    @JsonProperty("page_limit")
    private long pageLimit;

    @JsonProperty("sort_method")
    private SortMethod sortMethod;

    @JsonProperty("sort_field")
    private UnpaidExpenseSummaryRequestDto.SortField sortField;

    public static final NumberPath<BigDecimal> TOTAL_OUTSTANDING_EXPRESSION =
        Expressions.numberPath(BigDecimal.class, "total_outstanding");

    @Getter
    public enum SortField implements SortMethod.HasComparableExpression {

        JUROR_NUMBER(QAppearance.appearance.jurorNumber),
        POOL_NUMBER(QAppearance.appearance.poolNumber),
        FIRST_NAME(QJuror.juror.firstName),
        LAST_NAME(QJuror.juror.lastName),
        TOTAL_IN_DRAFT(TOTAL_OUTSTANDING_EXPRESSION);

        private final Expression<? extends Comparable<?>> comparableExpression;


        SortField(Expression<? extends Comparable<?>> comparableExpression) {
            this.comparableExpression = comparableExpression;
        }
    }
}
