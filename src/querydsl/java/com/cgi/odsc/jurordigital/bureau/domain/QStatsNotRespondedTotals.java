package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatsNotRespondedTotals is a Querydsl query type for StatsNotRespondedTotals
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatsNotRespondedTotals extends EntityPathBase<StatsNotRespondedTotals> {

    private static final long serialVersionUID = -1190566996L;

    public static final QStatsNotRespondedTotals statsNotRespondedTotals = new QStatsNotRespondedTotals("statsNotRespondedTotals");

    public final NumberPath<Integer> notRespondedTotals = createNumber("notRespondedTotals", Integer.class);

    public QStatsNotRespondedTotals(String variable) {
        super(StatsNotRespondedTotals.class, forVariable(variable));
    }

    public QStatsNotRespondedTotals(Path<? extends StatsNotRespondedTotals> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatsNotRespondedTotals(PathMetadata metadata) {
        super(StatsNotRespondedTotals.class, metadata);
    }

}

