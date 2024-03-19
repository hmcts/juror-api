package uk.gov.hmcts.juror.api.moj.domain;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;

@SuppressWarnings("PMD.LawOfDemeter")
public enum SortMethod {
    DESC, ASC;


    public OrderSpecifier<?> from(HasComparableExpression expressionObject) {
        return from(expressionObject.getComparableExpression());
    }

    public OrderSpecifier<?> from(Expression<? extends Comparable<?>> expressionObject) {
        if (SortMethod.ASC.equals(this)) {
            return new OrderSpecifier<>(Order.ASC, expressionObject);
        } else {
            return new OrderSpecifier<>(Order.DESC, expressionObject);
        }
    }

    public interface HasComparableExpression {
        Expression<? extends Comparable<?>> getComparableExpression();
    }
}
