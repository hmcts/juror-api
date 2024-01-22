package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPoolComments_PoolCommentsId is a Querydsl query type for PoolCommentsId
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QPoolComments_PoolCommentsId extends BeanPath<PoolComments.PoolCommentsId> {

    private static final long serialVersionUID = 562258738L;

    public static final QPoolComments_PoolCommentsId poolCommentsId = new QPoolComments_PoolCommentsId("poolCommentsId");

    public final DateTimePath<java.sql.Timestamp> lastUpdate = createDateTime("lastUpdate", java.sql.Timestamp.class);

    public final NumberPath<Integer> numberRequested = createNumber("numberRequested", Integer.class);

    public final StringPath owner = createString("owner");

    public final StringPath poolComment = createString("poolComment");

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath userId = createString("userId");

    public QPoolComments_PoolCommentsId(String variable) {
        super(PoolComments.PoolCommentsId.class, forVariable(variable));
    }

    public QPoolComments_PoolCommentsId(Path<? extends PoolComments.PoolCommentsId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoolComments_PoolCommentsId(PathMetadata metadata) {
        super(PoolComments.PoolCommentsId.class, metadata);
    }

}

