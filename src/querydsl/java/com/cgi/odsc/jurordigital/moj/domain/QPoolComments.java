package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPoolComments is a Querydsl query type for PoolComments
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolComments extends EntityPathBase<PoolComments> {

    private static final long serialVersionUID = -1636931289L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPoolComments poolComments = new QPoolComments("poolComments");

    public final QPoolComments_PoolCommentsId poolCommentsId;

    public QPoolComments(String variable) {
        this(PoolComments.class, forVariable(variable), INITS);
    }

    public QPoolComments(Path<? extends PoolComments> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPoolComments(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPoolComments(PathMetadata metadata, PathInits inits) {
        this(PoolComments.class, metadata, inits);
    }

    public QPoolComments(Class<? extends PoolComments> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.poolCommentsId = inits.isInitialized("poolCommentsId") ? new QPoolComments_PoolCommentsId(forProperty("poolCommentsId")) : null;
    }

}

