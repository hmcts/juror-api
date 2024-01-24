package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChangeLog is a Querydsl query type for ChangeLog
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QChangeLog extends EntityPathBase<ChangeLog> {

    private static final long serialVersionUID = -19003023L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChangeLog changeLog = new QChangeLog("changeLog");

    public final SetPath<ChangeLogItem, QChangeLogItem> changeLogItems = this.<ChangeLogItem, QChangeLogItem>createSet("changeLogItems", ChangeLogItem.class, QChangeLogItem.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath notes = createString("notes");

    public final QStaff staff;

    public final DateTimePath<java.util.Date> timestamp = createDateTime("timestamp", java.util.Date.class);

    public final EnumPath<ChangeLogType> type = createEnum("type", ChangeLogType.class);

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QChangeLog(String variable) {
        this(ChangeLog.class, forVariable(variable), INITS);
    }

    public QChangeLog(Path<? extends ChangeLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChangeLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChangeLog(PathMetadata metadata, PathInits inits) {
        this(ChangeLog.class, metadata, inits);
    }

    public QChangeLog(Class<? extends ChangeLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.staff = inits.isInitialized("staff") ? new QStaff(forProperty("staff"), inits.get("staff")) : null;
    }

}

