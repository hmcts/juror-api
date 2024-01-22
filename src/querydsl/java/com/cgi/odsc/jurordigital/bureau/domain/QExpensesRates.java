package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QExpensesRates is a Querydsl query type for ExpensesRates
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QExpensesRates extends EntityPathBase<ExpensesRates> {

    private static final long serialVersionUID = 1281384117L;

    public static final QExpensesRates expensesRates = new QExpensesRates("expensesRates");

    public final StringPath expenseType = createString("expenseType");

    public final NumberPath<Float> rate = createNumber("rate", Float.class);

    public QExpensesRates(String variable) {
        super(ExpensesRates.class, forVariable(variable));
    }

    public QExpensesRates(Path<? extends ExpensesRates> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExpensesRates(PathMetadata metadata) {
        super(ExpensesRates.class, metadata);
    }

}

