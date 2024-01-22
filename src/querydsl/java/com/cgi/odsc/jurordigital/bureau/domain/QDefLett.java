package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QDefLett is a Querydsl query type for DefLett
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QDefLett extends EntityPathBase<DefLett> {

    private static final long serialVersionUID = -785859941L;

    public static final QDefLett defLett = new QDefLett("defLett");

    public final DateTimePath<java.util.Date> dateDef = createDateTime("dateDef", java.util.Date.class);

    public final DateTimePath<java.util.Date> datePrinted = createDateTime("datePrinted", java.util.Date.class);

    public final StringPath excusalCode = createString("excusalCode");

    public final StringPath owner = createString("owner");

    public final StringPath partNo = createString("partNo");

    public final StringPath printed = createString("printed");

    public QDefLett(String variable) {
        super(DefLett.class, forVariable(variable));
    }

    public QDefLett(Path<? extends DefLett> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDefLett(PathMetadata metadata) {
        super(DefLett.class, metadata);
    }

}

