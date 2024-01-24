package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QDefDenied is a Querydsl query type for DefDenied
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QDefDenied extends EntityPathBase<DefDenied> {

    private static final long serialVersionUID = 473621501L;

    public static final QDefDenied defDenied = new QDefDenied("defDenied");

    public final DateTimePath<java.util.Date> dateDef = createDateTime("dateDef", java.util.Date.class);

    public final DateTimePath<java.util.Date> datePrinted = createDateTime("datePrinted", java.util.Date.class);

    public final StringPath excusalCode = createString("excusalCode");

    public final StringPath owner = createString("owner");

    public final StringPath partNo = createString("partNo");

    public final StringPath printed = createString("printed");

    public QDefDenied(String variable) {
        super(DefDenied.class, forVariable(variable));
    }

    public QDefDenied(Path<? extends DefDenied> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDefDenied(PathMetadata metadata) {
        super(DefDenied.class, metadata);
    }

}

