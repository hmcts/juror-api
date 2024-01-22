package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QBureauAuthentication is a Querydsl query type for BureauAuthentication
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBureauAuthentication extends EntityPathBase<BureauAuthentication> {

    private static final long serialVersionUID = -1317956235L;

    public static final QBureauAuthentication bureauAuthentication = new QBureauAuthentication("bureauAuthentication");

    public final DateTimePath<java.util.Date> lastUsed = createDateTime("lastUsed", java.util.Date.class);

    public final StringPath login = createString("login");

    public final BooleanPath loginEnabledYn = createBoolean("loginEnabledYn");

    public final StringPath owner = createString("owner");

    public final StringPath password = createString("password");

    public final DateTimePath<java.util.Date> passwordChangedDate = createDateTime("passwordChangedDate", java.util.Date.class);

    public final StringPath userLevel = createString("userLevel");

    public QBureauAuthentication(String variable) {
        super(BureauAuthentication.class, forVariable(variable));
    }

    public QBureauAuthentication(Path<? extends BureauAuthentication> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBureauAuthentication(PathMetadata metadata) {
        super(BureauAuthentication.class, metadata);
    }

}

