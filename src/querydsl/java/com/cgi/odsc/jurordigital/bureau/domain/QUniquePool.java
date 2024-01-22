package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QUniquePool is a Querydsl query type for UniquePool
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QUniquePool extends EntityPathBase<UniquePool> {

    private static final long serialVersionUID = 2034857776L;

    public static final QUniquePool uniquePool = new QUniquePool("uniquePool");

    public final NumberPath<java.math.BigDecimal> additionalSummons = createNumber("additionalSummons", java.math.BigDecimal.class);

    public final StringPath attendTime = createString("attendTime");

    public final DateTimePath<java.util.Date> lastUpdate = createDateTime("lastUpdate", java.util.Date.class);

    public final StringPath locCode = createString("locCode");

    public final BooleanPath newRequest = createBoolean("newRequest");

    public final DateTimePath<java.util.Date> nextDate = createDateTime("nextDate", java.util.Date.class);

    public final StringPath poolNumber = createString("poolNumber");

    public final DateTimePath<java.util.Date> returnDate = createDateTime("returnDate", java.util.Date.class);

    public QUniquePool(String variable) {
        super(UniquePool.class, forVariable(variable));
    }

    public QUniquePool(Path<? extends UniquePool> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUniquePool(PathMetadata metadata) {
        super(UniquePool.class, metadata);
    }

}

