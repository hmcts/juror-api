package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QTSpecial is a Querydsl query type for TSpecial
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTSpecial extends EntityPathBase<TSpecial> {

    private static final long serialVersionUID = 742451720L;

    public static final QTSpecial tSpecial = new QTSpecial("tSpecial");

    public final StringPath code = createString("code");

    public final StringPath description = createString("description");

    public QTSpecial(String variable) {
        super(TSpecial.class, forVariable(variable));
    }

    public QTSpecial(Path<? extends TSpecial> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTSpecial(PathMetadata metadata) {
        super(TSpecial.class, metadata);
    }

}

