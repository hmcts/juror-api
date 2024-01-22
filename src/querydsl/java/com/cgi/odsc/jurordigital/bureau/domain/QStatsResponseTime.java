package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatsResponseTime is a Querydsl query type for StatsResponseTime
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatsResponseTime extends EntityPathBase<StatsResponseTime> {

    private static final long serialVersionUID = -1057130518L;

    public static final QStatsResponseTime statsResponseTime = new QStatsResponseTime("statsResponseTime");

    public final StringPath locCode = createString("locCode");

    public final NumberPath<Integer> responseCount = createNumber("responseCount", Integer.class);

    public final StringPath responseMethod = createString("responseMethod");

    public final DatePath<java.util.Date> responseMonth = createDate("responseMonth", java.util.Date.class);

    public final StringPath responsePeriod = createString("responsePeriod");

    public final DatePath<java.util.Date> summonsMonth = createDate("summonsMonth", java.util.Date.class);

    public QStatsResponseTime(String variable) {
        super(StatsResponseTime.class, forVariable(variable));
    }

    public QStatsResponseTime(Path<? extends StatsResponseTime> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatsResponseTime(PathMetadata metadata) {
        super(StatsResponseTime.class, metadata);
    }

}

