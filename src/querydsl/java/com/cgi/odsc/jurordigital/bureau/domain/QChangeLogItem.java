package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChangeLogItem is a Querydsl query type for ChangeLogItem
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QChangeLogItem extends EntityPathBase<ChangeLogItem> {

    private static final long serialVersionUID = -452143068L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChangeLogItem changeLogItem = new QChangeLogItem("changeLogItem");

    public final QChangeLog changeLog;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath newKeyName = createString("newKeyName");

    public final StringPath newValue = createString("newValue");

    public final StringPath oldKeyName = createString("oldKeyName");

    public final StringPath oldValue = createString("oldValue");

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QChangeLogItem(String variable) {
        this(ChangeLogItem.class, forVariable(variable), INITS);
    }

    public QChangeLogItem(Path<? extends ChangeLogItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChangeLogItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChangeLogItem(PathMetadata metadata, PathInits inits) {
        this(ChangeLogItem.class, metadata, inits);
    }

    public QChangeLogItem(Class<? extends ChangeLogItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.changeLog = inits.isInitialized("changeLog") ? new QChangeLog(forProperty("changeLog"), inits.get("changeLog")) : null;
    }

}

