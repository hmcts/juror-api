package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStaff is a Querydsl query type for Staff
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStaff extends EntityPathBase<Staff> {

    private static final long serialVersionUID = -1461380771L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStaff staff = new QStaff("staff");

    public final NumberPath<Integer> active = createNumber("active", Integer.class);

    public final StringPath court1 = createString("court1");

    public final StringPath court10 = createString("court10");

    public final StringPath court2 = createString("court2");

    public final StringPath court3 = createString("court3");

    public final StringPath court4 = createString("court4");

    public final StringPath court5 = createString("court5");

    public final StringPath court6 = createString("court6");

    public final StringPath court7 = createString("court7");

    public final StringPath court8 = createString("court8");

    public final StringPath court9 = createString("court9");

    public final StringPath login = createString("login");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> rank = createNumber("rank", Integer.class);

    public final QTeam team;

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QStaff(String variable) {
        this(Staff.class, forVariable(variable), INITS);
    }

    public QStaff(Path<? extends Staff> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStaff(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStaff(PathMetadata metadata, PathInits inits) {
        this(Staff.class, metadata, inits);
    }

    public QStaff(Class<? extends Staff> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.team = inits.isInitialized("team") ? new QTeam(forProperty("team")) : null;
    }

}

