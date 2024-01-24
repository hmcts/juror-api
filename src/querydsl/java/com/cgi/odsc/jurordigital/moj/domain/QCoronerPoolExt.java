package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QCoronerPoolExt is a Querydsl query type for CoronerPoolExt
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCoronerPoolExt extends EntityPathBase<CoronerPoolExt> {

    private static final long serialVersionUID = 1913497578L;

    public static final QCoronerPoolExt coronerPoolExt = new QCoronerPoolExt("coronerPoolExt");

    public final StringPath email = createString("email");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath poolNumber = createString("poolNumber");

    public QCoronerPoolExt(String variable) {
        super(CoronerPoolExt.class, forVariable(variable));
    }

    public QCoronerPoolExt(Path<? extends CoronerPoolExt> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoronerPoolExt(PathMetadata metadata) {
        super(CoronerPoolExt.class, metadata);
    }

}

