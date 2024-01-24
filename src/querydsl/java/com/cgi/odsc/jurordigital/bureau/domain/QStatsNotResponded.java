package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatsNotResponded is a Querydsl query type for StatsNotResponded
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatsNotResponded extends EntityPathBase<StatsNotResponded> {

    private static final long serialVersionUID = -57158755L;

    public static final QStatsNotResponded statsNotResponded = new QStatsNotResponded("statsNotResponded");

    public final StringPath locCode = createString("locCode");

    public final NumberPath<Integer> nonResponseCount = createNumber("nonResponseCount", Integer.class);

    public final DateTimePath<java.util.Date> summonsMonth = createDateTime("summonsMonth", java.util.Date.class);

    public QStatsNotResponded(String variable) {
        super(StatsNotResponded.class, forVariable(variable));
    }

    public QStatsNotResponded(Path<? extends StatsNotResponded> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatsNotResponded(PathMetadata metadata) {
        super(StatsNotResponded.class, metadata);
    }

}

