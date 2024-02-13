package uk.gov.hmcts.juror.api.moj.domain;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;

import static org.assertj.core.api.Assertions.assertThat;

class SortMethodTest {


    private static final ComparableExpressionBase<?> COMPARABLE_EXPRESSION_BASE =
        QJuror.juror.jurorNumber.eq(TestConstants.VALID_JUROR_NUMBER);

    @Test
    void positiveHasComparableExpressionSortAsc() {
        assertThat(SortMethod.ASC.from(new SortMethodTestClass())).isEqualTo(
            COMPARABLE_EXPRESSION_BASE.asc()
        );
    }

    @Test
    void positiveHasComparableExpressionSortDesc() {
        assertThat(SortMethod.DESC.from(new SortMethodTestClass())).isEqualTo(
            COMPARABLE_EXPRESSION_BASE.desc()
        );
    }

    @Test
    void positiveComparableExpressionBaseSortAsc() {
        assertThat(SortMethod.ASC.from(COMPARABLE_EXPRESSION_BASE)).isEqualTo(
            COMPARABLE_EXPRESSION_BASE.asc()
        );
    }

    @Test
    void positiveComparableExpressionBaseSortDesc() {
        assertThat(SortMethod.DESC.from(COMPARABLE_EXPRESSION_BASE)).isEqualTo(
            COMPARABLE_EXPRESSION_BASE.desc()
        );
    }


    static class SortMethodTestClass implements SortMethod.HasComparableExpression {

        @Override
        public ComparableExpressionBase<?> getComparableExpression() {
            return COMPARABLE_EXPRESSION_BASE;
        }
    }
}
