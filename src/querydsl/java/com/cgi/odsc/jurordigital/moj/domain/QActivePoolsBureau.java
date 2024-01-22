package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QActivePoolsBureau is a Querydsl query type for ActivePoolsBureau
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QActivePoolsBureau extends EntityPathBase<ActivePoolsBureau> {

    private static final long serialVersionUID = 796811188L;

    public static final QActivePoolsBureau activePoolsBureau = new QActivePoolsBureau("activePoolsBureau");

    public final NumberPath<Integer> confirmedJurors = createNumber("confirmedJurors", Integer.class);

    public final StringPath courtName = createString("courtName");

    public final NumberPath<Integer> jurorsRequested = createNumber("jurorsRequested", Integer.class);

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath poolType = createString("poolType");

    public final DatePath<java.time.LocalDate> serviceStartDate = createDate("serviceStartDate", java.time.LocalDate.class);

    public QActivePoolsBureau(String variable) {
        super(ActivePoolsBureau.class, forVariable(variable));
    }

    public QActivePoolsBureau(Path<? extends ActivePoolsBureau> path) {
        super(path.getType(), path.getMetadata());
    }

    public QActivePoolsBureau(PathMetadata metadata) {
        super(ActivePoolsBureau.class, metadata);
    }

}

