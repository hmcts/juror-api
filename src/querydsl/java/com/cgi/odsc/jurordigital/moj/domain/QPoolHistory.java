package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPoolHistory is a Querydsl query type for PoolHistory
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolHistory extends EntityPathBase<PoolHistory> {

    private static final long serialVersionUID = -1323194623L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPoolHistory poolHistory = new QPoolHistory("poolHistory");

    public final QPoolHistory_PoolHistoryId poolHistoryId;

    public QPoolHistory(String variable) {
        this(PoolHistory.class, forVariable(variable), INITS);
    }

    public QPoolHistory(Path<? extends PoolHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPoolHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPoolHistory(PathMetadata metadata, PathInits inits) {
        this(PoolHistory.class, metadata, inits);
    }

    public QPoolHistory(Class<? extends PoolHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.poolHistoryId = inits.isInitialized("poolHistoryId") ? new QPoolHistory_PoolHistoryId(forProperty("poolHistoryId")) : null;
    }

}

