package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPoolMemberExt is a Querydsl query type for PoolMemberExt
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolMemberExt extends EntityPathBase<PoolMemberExt> {

    private static final long serialVersionUID = -663018412L;

    public static final QPoolMemberExt poolMemberExt = new QPoolMemberExt("poolMemberExt");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath opticRef = createString("opticRef");

    public final StringPath owner = createString("owner");

    public final StringPath pendingFirstName = createString("pendingFirstName");

    public final StringPath pendingLastName = createString("pendingLastName");

    public final StringPath pendingTitle = createString("pendingTitle");

    public final StringPath poolNumber = createString("poolNumber");

    public QPoolMemberExt(String variable) {
        super(PoolMemberExt.class, forVariable(variable));
    }

    public QPoolMemberExt(Path<? extends PoolMemberExt> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoolMemberExt(PathMetadata metadata) {
        super(PoolMemberExt.class, metadata);
    }

}

