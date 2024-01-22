package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPoolHistory_PoolHistoryId is a Querydsl query type for PoolHistoryId
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QPoolHistory_PoolHistoryId extends BeanPath<PoolHistory.PoolHistoryId> {

    private static final long serialVersionUID = 282082246L;

    public static final QPoolHistory_PoolHistoryId poolHistoryId = new QPoolHistory_PoolHistoryId("poolHistoryId");

    public final EnumPath<HistoryCode> historyCode = createEnum("historyCode", HistoryCode.class);

    public final DateTimePath<java.time.LocalDateTime> historyDate = createDateTime("historyDate", java.time.LocalDateTime.class);

    public final StringPath otherInformation = createString("otherInformation");

    public final StringPath owner = createString("owner");

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath userId = createString("userId");

    public QPoolHistory_PoolHistoryId(String variable) {
        super(PoolHistory.PoolHistoryId.class, forVariable(variable));
    }

    public QPoolHistory_PoolHistoryId(Path<? extends PoolHistory.PoolHistoryId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoolHistory_PoolHistoryId(PathMetadata metadata) {
        super(PoolHistory.PoolHistoryId.class, metadata);
    }

}

