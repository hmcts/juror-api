package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStaffAudit is a Querydsl query type for StaffAudit
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStaffAudit extends EntityPathBase<StaffAudit> {

    private static final long serialVersionUID = 1776280702L;

    public static final QStaffAudit staffAudit = new QStaffAudit("staffAudit");

    public final EnumPath<StaffAmendmentAction> action = createEnum("action", StaffAmendmentAction.class);

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

    public final DateTimePath<java.util.Date> created = createDateTime("created", java.util.Date.class);

    public final StringPath editorLogin = createString("editorLogin");

    public final StringPath login = createString("login");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> rank = createNumber("rank", Integer.class);

    public final NumberPath<Long> team = createNumber("team", Long.class);

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QStaffAudit(String variable) {
        super(StaffAudit.class, forVariable(variable));
    }

    public QStaffAudit(Path<? extends StaffAudit> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStaffAudit(PathMetadata metadata) {
        super(StaffAudit.class, metadata);
    }

}

