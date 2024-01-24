package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QMessageStaging is a Querydsl query type for MessageStaging
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QMessageStaging extends EntityPathBase<MessageStaging> {

    private static final long serialVersionUID = 1291760333L;

    public static final QMessageStaging messageStaging = new QMessageStaging("messageStaging");

    public final StringPath email = createString("email");

    public final StringPath fileDatetime = createString("fileDatetime");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath locationCode = createString("locationCode");

    public final StringPath locationName = createString("locationName");

    public final NumberPath<Integer> messageId = createNumber("messageId", Integer.class);

    public final StringPath messageText = createString("messageText");

    public final StringPath notifyAccountKey = createString("notifyAccountKey");

    public final StringPath notifyTemplateId = createString("notifyTemplateId");

    public final StringPath phone = createString("phone");

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath regionId = createString("regionId");

    public final StringPath subject = createString("subject");

    public final StringPath userName = createString("userName");

    public QMessageStaging(String variable) {
        super(MessageStaging.class, forVariable(variable));
    }

    public QMessageStaging(Path<? extends MessageStaging> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMessageStaging(PathMetadata metadata) {
        super(MessageStaging.class, metadata);
    }

}

