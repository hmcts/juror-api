package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPoolRequestExt is a Querydsl query type for PoolRequestExt
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolRequestExt extends EntityPathBase<PoolRequestExt> {

    private static final long serialVersionUID = -1995019387L;

    public static final QPoolRequestExt poolRequestExt = new QPoolRequestExt("poolRequestExt");

    public final DateTimePath<java.time.LocalDateTime> lastUpdate = createDateTime("lastUpdate", java.time.LocalDateTime.class);

    public final StringPath poolNumber = createString("poolNumber");

    public final NumberPath<Integer> totalNoRequired = createNumber("totalNoRequired", Integer.class);

    public QPoolRequestExt(String variable) {
        super(PoolRequestExt.class, forVariable(variable));
    }

    public QPoolRequestExt(Path<? extends PoolRequestExt> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoolRequestExt(PathMetadata metadata) {
        super(PoolRequestExt.class, metadata);
    }

}

