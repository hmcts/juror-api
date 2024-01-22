package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPoolStatus is a Querydsl query type for PoolStatus
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolStatus extends EntityPathBase<PoolStatus> {

    private static final long serialVersionUID = 1805880293L;

    public static final QPoolStatus poolStatus = new QPoolStatus("poolStatus");

    public final ComparablePath<Character> active = createComparable("active", Character.class);

    public final NumberPath<Long> status = createNumber("status", Long.class);

    public final StringPath statusDesc = createString("statusDesc");

    public QPoolStatus(String variable) {
        super(PoolStatus.class, forVariable(variable));
    }

    public QPoolStatus(Path<? extends PoolStatus> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoolStatus(PathMetadata metadata) {
        super(PoolStatus.class, metadata);
    }

}

