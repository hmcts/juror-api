package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBureauJurorSpecialNeed is a Querydsl query type for BureauJurorSpecialNeed
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBureauJurorSpecialNeed extends EntityPathBase<BureauJurorSpecialNeed> {

    private static final long serialVersionUID = -538151518L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBureauJurorSpecialNeed bureauJurorSpecialNeed = new QBureauJurorSpecialNeed("bureauJurorSpecialNeed");

    public final StringPath detail = createString("detail");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jurorNumber = createString("jurorNumber");

    public final QTSpecial specialNeed;

    public QBureauJurorSpecialNeed(String variable) {
        this(BureauJurorSpecialNeed.class, forVariable(variable), INITS);
    }

    public QBureauJurorSpecialNeed(Path<? extends BureauJurorSpecialNeed> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBureauJurorSpecialNeed(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBureauJurorSpecialNeed(PathMetadata metadata, PathInits inits) {
        this(BureauJurorSpecialNeed.class, metadata, inits);
    }

    public QBureauJurorSpecialNeed(Class<? extends BureauJurorSpecialNeed> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.specialNeed = inits.isInitialized("specialNeed") ? new QTSpecial(forProperty("specialNeed")) : null;
    }

}

