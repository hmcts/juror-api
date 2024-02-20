package uk.gov.hmcts.juror.api.moj.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;

@SuppressWarnings("PMD.LawOfDemeter")
public enum SortMethod {
    DESC, ASC;


    public OrderSpecifier<?> from(HasComparableExpression expressionObject) {
        return from(expressionObject.getComparableExpression());
    }

    public OrderSpecifier<?> from(ComparableExpressionBase<?> expressionObject) {
        if (SortMethod.ASC.equals(this)) {
            return expressionObject.asc();
        } else {
            return expressionObject.desc();
        }
    }

    public interface HasComparableExpression {
        ComparableExpressionBase<?> getComparableExpression();
    }
}
