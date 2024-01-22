package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QSummonsSnapshot is a Querydsl query type for SummonsSnapshot
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QSummonsSnapshot extends EntityPathBase<SummonsSnapshot> {

    private static final long serialVersionUID = -1417486017L;

    public static final QSummonsSnapshot summonsSnapshot = new QSummonsSnapshot("summonsSnapshot");

    public final StringPath courtLocationCode = createString("courtLocationCode");

    public final StringPath courtLocationName = createString("courtLocationName");

    public final StringPath courtName = createString("courtName");

    public final DateTimePath<java.time.LocalDateTime> dateCreated = createDateTime("dateCreated", java.time.LocalDateTime.class);

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath poolNumber = createString("poolNumber");

    public final DatePath<java.time.LocalDate> serviceStartDate = createDate("serviceStartDate", java.time.LocalDate.class);

    public QSummonsSnapshot(String variable) {
        super(SummonsSnapshot.class, forVariable(variable));
    }

    public QSummonsSnapshot(Path<? extends SummonsSnapshot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSummonsSnapshot(PathMetadata metadata) {
        super(SummonsSnapshot.class, metadata);
    }

}

