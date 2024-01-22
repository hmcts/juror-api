package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCoronerPool is a Querydsl query type for CoronerPool
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCoronerPool extends EntityPathBase<CoronerPool> {

    private static final long serialVersionUID = -382707017L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCoronerPool coronerPool = new QCoronerPool("coronerPool");

    public final com.cgi.odsc.jurordigital.juror.domain.QCourtLocation courtLocation;

    public final StringPath name = createString("name");

    public final NumberPath<Integer> numberRequested = createNumber("numberRequested", Integer.class);

    public final StringPath poolNumber = createString("poolNumber");

    public final DatePath<java.time.LocalDate> requestDate = createDate("requestDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> serviceDate = createDate("serviceDate", java.time.LocalDate.class);

    public QCoronerPool(String variable) {
        this(CoronerPool.class, forVariable(variable), INITS);
    }

    public QCoronerPool(Path<? extends CoronerPool> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCoronerPool(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCoronerPool(PathMetadata metadata, PathInits inits) {
        this(CoronerPool.class, metadata, inits);
    }

    public QCoronerPool(Class<? extends CoronerPool> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.courtLocation = inits.isInitialized("courtLocation") ? new com.cgi.odsc.jurordigital.juror.domain.QCourtLocation(forProperty("courtLocation"), inits.get("courtLocation")) : null;
    }

}

