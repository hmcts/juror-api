package uk.gov.hmcts.juror.api.moj.domain;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.LawOfDemeter")
class SortMethodTest {


    private static final Expression<? extends Comparable<?>> COMPARABLE_EXPRESSION_BASE =
        QJuror.juror.jurorNumber.eq(TestConstants.VALID_JUROR_NUMBER);

    @Test
    void positiveHasComparableExpressionSortAsc() {
        assertThat(SortMethod.ASC.from(new SortMethodTestClass())).isEqualTo(
            new OrderSpecifier<>(Order.ASC, COMPARABLE_EXPRESSION_BASE)
        );
    }

    @Test
    void positiveHasComparableExpressionSortDesc() {
        assertThat(SortMethod.DESC.from(new SortMethodTestClass())).isEqualTo(
            new OrderSpecifier<>(Order.DESC, COMPARABLE_EXPRESSION_BASE)
        );
    }

    @Test
    void positiveComparableExpressionBaseSortAsc() {
        assertThat(SortMethod.ASC.from(COMPARABLE_EXPRESSION_BASE)).isEqualTo(
            new OrderSpecifier<>(Order.ASC, COMPARABLE_EXPRESSION_BASE)
        );
    }

    @Test
    void positiveComparableExpressionBaseSortDesc() {
        assertThat(SortMethod.DESC.from(COMPARABLE_EXPRESSION_BASE)).isEqualTo(
            new OrderSpecifier<>(Order.DESC, COMPARABLE_EXPRESSION_BASE)
        );
    }


    static class SortMethodTestClass implements SortMethod.HasComparableExpression {

        @Override
        public Expression<? extends Comparable<?>> getComparableExpression() {
            return COMPARABLE_EXPRESSION_BASE;
        }
    }
}
