package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatsResponseTimesTotals is a Querydsl query type for StatsResponseTimesTotals
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatsResponseTimesTotals extends EntityPathBase<StatsResponseTimesTotals> {

    private static final long serialVersionUID = -1859664680L;

    public static final QStatsResponseTimesTotals statsResponseTimesTotals = new QStatsResponseTimesTotals("statsResponseTimesTotals");

    public final NumberPath<Integer> allResponsesTotal = createNumber("allResponsesTotal", Integer.class);

    public final NumberPath<Integer> onlineResponsesTotal = createNumber("onlineResponsesTotal", Integer.class);

    public QStatsResponseTimesTotals(String variable) {
        super(StatsResponseTimesTotals.class, forVariable(variable));
    }

    public QStatsResponseTimesTotals(Path<? extends StatsResponseTimesTotals> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatsResponseTimesTotals(PathMetadata metadata) {
        super(StatsResponseTimesTotals.class, metadata);
    }

}

