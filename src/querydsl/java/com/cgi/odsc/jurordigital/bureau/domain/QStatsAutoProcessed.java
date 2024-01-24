package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatsAutoProcessed is a Querydsl query type for StatsAutoProcessed
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatsAutoProcessed extends EntityPathBase<StatsAutoProcessed> {

    private static final long serialVersionUID = 2027979939L;

    public static final QStatsAutoProcessed statsAutoProcessed = new QStatsAutoProcessed("statsAutoProcessed");

    public final NumberPath<Integer> processedCount = createNumber("processedCount", Integer.class);

    public final DateTimePath<java.util.Date> processedDate = createDateTime("processedDate", java.util.Date.class);

    public QStatsAutoProcessed(String variable) {
        super(StatsAutoProcessed.class, forVariable(variable));
    }

    public QStatsAutoProcessed(Path<? extends StatsAutoProcessed> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatsAutoProcessed(PathMetadata metadata) {
        super(StatsAutoProcessed.class, metadata);
    }

}

