package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QContactLogExt is a Querydsl query type for ContactLogExt
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QContactLogExt extends EntityPathBase<ContactLogExt> {

    private static final long serialVersionUID = -602282906L;

    public static final QContactLogExt contactLogExt = new QContactLogExt("contactLogExt");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final BooleanPath repeatEnquiry = createBoolean("repeatEnquiry");

    public final DateTimePath<java.time.LocalDateTime> startCall = createDateTime("startCall", java.time.LocalDateTime.class);

    public QContactLogExt(String variable) {
        super(ContactLogExt.class, forVariable(variable));
    }

    public QContactLogExt(Path<? extends ContactLogExt> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContactLogExt(PathMetadata metadata) {
        super(ContactLogExt.class, metadata);
    }

}

