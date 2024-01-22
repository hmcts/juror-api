package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMessages is a Querydsl query type for Messages
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QMessages extends EntityPathBase<Messages> {

    private static final long serialVersionUID = -117673819L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMessages messages = new QMessages("messages");

    public final StringPath email = createString("email");

    public final StringPath fileDatetime = createString("fileDatetime");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final QCourtLocation locationCode;

    public final StringPath locationName = createString("locationName");

    public final NumberPath<Integer> messageId = createNumber("messageId", Integer.class);

    public final StringPath messageRead = createString("messageRead");

    public final StringPath messageText = createString("messageText");

    public final StringPath phone = createString("phone");

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath subject = createString("subject");

    public final StringPath userName = createString("userName");

    public QMessages(String variable) {
        this(Messages.class, forVariable(variable), INITS);
    }

    public QMessages(Path<? extends Messages> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMessages(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMessages(PathMetadata metadata, PathInits inits) {
        this(Messages.class, metadata, inits);
    }

    public QMessages(Class<? extends Messages> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.locationCode = inits.isInitialized("locationCode") ? new QCourtLocation(forProperty("locationCode"), inits.get("locationCode")) : null;
    }

}

