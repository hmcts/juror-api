package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJurorPaperResponseSpecialNeed is a Querydsl query type for JurorPaperResponseSpecialNeed
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QJurorPaperResponseSpecialNeed extends EntityPathBase<JurorPaperResponseSpecialNeed> {

    private static final long serialVersionUID = 647006581L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJurorPaperResponseSpecialNeed jurorPaperResponseSpecialNeed = new QJurorPaperResponseSpecialNeed("jurorPaperResponseSpecialNeed");

    public final StringPath detail = createString("detail");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jurorNumber = createString("jurorNumber");

    public final com.cgi.odsc.jurordigital.bureau.domain.QTSpecial specialNeed;

    public QJurorPaperResponseSpecialNeed(String variable) {
        this(JurorPaperResponseSpecialNeed.class, forVariable(variable), INITS);
    }

    public QJurorPaperResponseSpecialNeed(Path<? extends JurorPaperResponseSpecialNeed> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJurorPaperResponseSpecialNeed(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJurorPaperResponseSpecialNeed(PathMetadata metadata, PathInits inits) {
        this(JurorPaperResponseSpecialNeed.class, metadata, inits);
    }

    public QJurorPaperResponseSpecialNeed(Class<? extends JurorPaperResponseSpecialNeed> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.specialNeed = inits.isInitialized("specialNeed") ? new com.cgi.odsc.jurordigital.bureau.domain.QTSpecial(forProperty("specialNeed")) : null;
    }

}

