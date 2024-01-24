package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPoolStatistics is a Querydsl query type for PoolStatistics
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolStatistics extends EntityPathBase<PoolStatistics> {

    private static final long serialVersionUID = -2126693482L;

    public static final QPoolStatistics poolStatistics = new QPoolStatistics("poolStatistics");

    public final NumberPath<Integer> available = createNumber("available", Integer.class);

    public final NumberPath<Integer> courtSupply = createNumber("courtSupply", Integer.class);

    public final StringPath poolNumber = createString("poolNumber");

    public final NumberPath<Integer> totalSummoned = createNumber("totalSummoned", Integer.class);

    public final NumberPath<Integer> unavailable = createNumber("unavailable", Integer.class);

    public final NumberPath<Integer> unresolved = createNumber("unresolved", Integer.class);

    public QPoolStatistics(String variable) {
        super(PoolStatistics.class, forVariable(variable));
    }

    public QPoolStatistics(Path<? extends PoolStatistics> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoolStatistics(PathMetadata metadata) {
        super(PoolStatistics.class, metadata);
    }

}

