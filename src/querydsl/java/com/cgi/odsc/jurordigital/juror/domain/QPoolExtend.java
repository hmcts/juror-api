package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPoolExtend is a Querydsl query type for PoolExtend
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolExtend extends EntityPathBase<PoolExtend> {

    private static final long serialVersionUID = 311016303L;

    public static final QPoolExtend poolExtend = new QPoolExtend("poolExtend");

    public final BooleanPath isLocked = createBoolean("isLocked");

    public final StringPath jurorNumber = createString("jurorNumber");

    public QPoolExtend(String variable) {
        super(PoolExtend.class, forVariable(variable));
    }

    public QPoolExtend(Path<? extends PoolExtend> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoolExtend(PathMetadata metadata) {
        super(PoolExtend.class, metadata);
    }

}

