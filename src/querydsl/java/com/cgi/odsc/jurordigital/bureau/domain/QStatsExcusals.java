package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatsExcusals is a Querydsl query type for StatsExcusals
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatsExcusals extends EntityPathBase<StatsExcusals> {

    private static final long serialVersionUID = -565616266L;

    public static final QStatsExcusals statsExcusals = new QStatsExcusals("statsExcusals");

    public final StringPath bureauOrCourt = createString("bureauOrCourt");

    public final StringPath calendarYear = createString("calendarYear");

    public final NumberPath<Integer> excusalCount = createNumber("excusalCount", Integer.class);

    public final StringPath execCode = createString("execCode");

    public final StringPath financialYear = createString("financialYear");

    public final StringPath week = createString("week");

    public QStatsExcusals(String variable) {
        super(StatsExcusals.class, forVariable(variable));
    }

    public QStatsExcusals(Path<? extends StatsExcusals> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatsExcusals(PathMetadata metadata) {
        super(StatsExcusals.class, metadata);
    }

}

