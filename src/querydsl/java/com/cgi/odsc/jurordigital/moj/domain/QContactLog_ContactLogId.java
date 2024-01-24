package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QContactLog_ContactLogId is a Querydsl query type for ContactLogId
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QContactLog_ContactLogId extends BeanPath<ContactLog.ContactLogId> {

    private static final long serialVersionUID = 661177010L;

    public static final QContactLog_ContactLogId contactLogId = new QContactLog_ContactLogId("contactLogId");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath owner = createString("owner");

    public final DateTimePath<java.time.LocalDateTime> startCall = createDateTime("startCall", java.time.LocalDateTime.class);

    public QContactLog_ContactLogId(String variable) {
        super(ContactLog.ContactLogId.class, forVariable(variable));
    }

    public QContactLog_ContactLogId(Path<? extends ContactLog.ContactLogId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContactLog_ContactLogId(PathMetadata metadata) {
        super(ContactLog.ContactLogId.class, metadata);
    }

}

