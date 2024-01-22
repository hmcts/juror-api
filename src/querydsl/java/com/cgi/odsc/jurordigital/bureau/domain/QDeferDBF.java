package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QDeferDBF is a Querydsl query type for DeferDBF
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QDeferDBF extends EntityPathBase<DeferDBF> {

    private static final long serialVersionUID = 1431573305L;

    public static final QDeferDBF deferDBF = new QDeferDBF("deferDBF");

    public final StringPath checked = createString("checked");

    public final DateTimePath<java.util.Date> deferTo = createDateTime("deferTo", java.util.Date.class);

    public final StringPath locCode = createString("locCode");

    public final StringPath owner = createString("owner");

    public final StringPath partNo = createString("partNo");

    public QDeferDBF(String variable) {
        super(DeferDBF.class, forVariable(variable));
    }

    public QDeferDBF(Path<? extends DeferDBF> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDeferDBF(PathMetadata metadata) {
        super(DeferDBF.class, metadata);
    }

}

