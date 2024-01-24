package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPoolType is a Querydsl query type for PoolType
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolType extends EntityPathBase<PoolType> {

    private static final long serialVersionUID = -1289704467L;

    public static final QPoolType poolType1 = new QPoolType("poolType1");

    public final StringPath description = createString("description");

    public final StringPath poolType = createString("poolType");

    public QPoolType(String variable) {
        super(PoolType.class, forVariable(variable));
    }

    public QPoolType(Path<? extends PoolType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoolType(PathMetadata metadata) {
        super(PoolType.class, metadata);
    }

}

