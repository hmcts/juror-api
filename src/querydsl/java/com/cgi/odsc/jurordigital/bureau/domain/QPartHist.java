package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPartHist is a Querydsl query type for PartHist
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPartHist extends EntityPathBase<PartHist> {

    private static final long serialVersionUID = 1971601944L;

    public static final QPartHist partHist = new QPartHist("partHist");

    public final DateTimePath<java.util.Date> datePart = createDateTime("datePart", java.util.Date.class);

    public final StringPath historyCode = createString("historyCode");

    public final StringPath info = createString("info");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final DateTimePath<java.util.Date> lastUpdate = createDateTime("lastUpdate", java.util.Date.class);

    public final StringPath owner = createString("owner");

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath userId = createString("userId");

    public QPartHist(String variable) {
        super(PartHist.class, forVariable(variable));
    }

    public QPartHist(Path<? extends PartHist> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPartHist(PathMetadata metadata) {
        super(PartHist.class, metadata);
    }

}

