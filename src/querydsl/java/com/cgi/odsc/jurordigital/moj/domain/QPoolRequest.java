package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPoolRequest is a Querydsl query type for PoolRequest
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolRequest extends EntityPathBase<PoolRequest> {

    private static final long serialVersionUID = -1154435844L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPoolRequest poolRequest = new QPoolRequest("poolRequest");

    public final NumberPath<Integer> additionalSummons = createNumber("additionalSummons", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> attendTime = createDateTime("attendTime", java.time.LocalDateTime.class);

    public final com.cgi.odsc.jurordigital.juror.domain.QCourtLocation courtLocation;

    public final NumberPath<Integer> deferralsUsed = createNumber("deferralsUsed", Integer.class);

    public final DateTimePath<java.sql.Timestamp> lastUpdate = createDateTime("lastUpdate", java.sql.Timestamp.class);

    public final ComparablePath<Character> newRequest = createComparable("newRequest", Character.class);

    public final DatePath<java.time.LocalDate> nextDate = createDate("nextDate", java.time.LocalDate.class);

    public final NumberPath<Integer> numberRequested = createNumber("numberRequested", Integer.class);

    public final StringPath owner = createString("owner");

    public final StringPath poolNumber = createString("poolNumber");

    public final NumberPath<Integer> poolTotal = createNumber("poolTotal", Integer.class);

    public final QPoolType poolType;

    public final BooleanPath readOnly = createBoolean("readOnly");

    public final ComparablePath<Character> regularOrSpecial = createComparable("regularOrSpecial", Character.class);

    public final DatePath<java.time.LocalDate> returnDate = createDate("returnDate", java.time.LocalDate.class);

    public QPoolRequest(String variable) {
        this(PoolRequest.class, forVariable(variable), INITS);
    }

    public QPoolRequest(Path<? extends PoolRequest> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPoolRequest(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPoolRequest(PathMetadata metadata, PathInits inits) {
        this(PoolRequest.class, metadata, inits);
    }

    public QPoolRequest(Class<? extends PoolRequest> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.courtLocation = inits.isInitialized("courtLocation") ? new com.cgi.odsc.jurordigital.juror.domain.QCourtLocation(forProperty("courtLocation"), inits.get("courtLocation")) : null;
        this.poolType = inits.isInitialized("poolType") ? new QPoolType(forProperty("poolType")) : null;
    }

}

