package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPhoneLog is a Querydsl query type for PhoneLog
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPhoneLog extends EntityPathBase<PhoneLog> {

    private static final long serialVersionUID = -496399527L;

    public static final QPhoneLog phoneLog = new QPhoneLog("phoneLog");

    public final DateTimePath<java.util.Date> endCall = createDateTime("endCall", java.util.Date.class);

    public final StringPath jurorNumber = createString("jurorNumber");

    public final DateTimePath<java.util.Date> lastUpdate = createDateTime("lastUpdate", java.util.Date.class);

    public final StringPath notes = createString("notes");

    public final StringPath owner = createString("owner");

    public final StringPath phoneCode = createString("phoneCode");

    public final DateTimePath<java.util.Date> startCall = createDateTime("startCall", java.util.Date.class);

    public final StringPath username = createString("username");

    public QPhoneLog(String variable) {
        super(PhoneLog.class, forVariable(variable));
    }

    public QPhoneLog(Path<? extends PhoneLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPhoneLog(PathMetadata metadata) {
        super(PhoneLog.class, metadata);
    }

}

