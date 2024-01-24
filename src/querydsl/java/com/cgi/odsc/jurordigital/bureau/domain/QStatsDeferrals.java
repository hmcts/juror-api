package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatsDeferrals is a Querydsl query type for StatsDeferrals
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatsDeferrals extends EntityPathBase<StatsDeferrals> {

    private static final long serialVersionUID = 608610988L;

    public static final QStatsDeferrals statsDeferrals = new QStatsDeferrals("statsDeferrals");

    public final StringPath bureauOrCourt = createString("bureauOrCourt");

    public final StringPath calendarYear = createString("calendarYear");

    public final NumberPath<Integer> excusalCount = createNumber("excusalCount", Integer.class);

    public final StringPath execCode = createString("execCode");

    public final StringPath financialYear = createString("financialYear");

    public final StringPath week = createString("week");

    public QStatsDeferrals(String variable) {
        super(StatsDeferrals.class, forVariable(variable));
    }

    public QStatsDeferrals(Path<? extends StatsDeferrals> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatsDeferrals(PathMetadata metadata) {
        super(StatsDeferrals.class, metadata);
    }

}

