package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContactLog is a Querydsl query type for ContactLog
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QContactLog extends EntityPathBase<ContactLog> {

    private static final long serialVersionUID = 1513476027L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContactLog contactLog = new QContactLog("contactLog");

    public final QContactLog_ContactLogId contactLogId;

    public final DateTimePath<java.time.LocalDateTime> endCall = createDateTime("endCall", java.time.LocalDateTime.class);

    public final QContactEnquiryType enquiryType;

    public final DateTimePath<java.time.LocalDateTime> lastUpdate = createDateTime("lastUpdate", java.time.LocalDateTime.class);

    public final StringPath notes = createString("notes");

    public final StringPath username = createString("username");

    public QContactLog(String variable) {
        this(ContactLog.class, forVariable(variable), INITS);
    }

    public QContactLog(Path<? extends ContactLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContactLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContactLog(PathMetadata metadata, PathInits inits) {
        this(ContactLog.class, metadata, inits);
    }

    public QContactLog(Class<? extends ContactLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.contactLogId = inits.isInitialized("contactLogId") ? new QContactLog_ContactLogId(forProperty("contactLogId")) : null;
        this.enquiryType = inits.isInitialized("enquiryType") ? new QContactEnquiryType(forProperty("enquiryType")) : null;
    }

}

