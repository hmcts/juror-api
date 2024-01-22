package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QDisqualifyCodeEntity is a Querydsl query type for DisqualifyCodeEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QDisqualifyCodeEntity extends EntityPathBase<DisqualifyCodeEntity> {

    private static final long serialVersionUID = -613097358L;

    public static final QDisqualifyCodeEntity disqualifyCodeEntity = new QDisqualifyCodeEntity("disqualifyCodeEntity");

    public final StringPath description = createString("description");

    public final StringPath disqualifyCode = createString("disqualifyCode");

    public QDisqualifyCodeEntity(String variable) {
        super(DisqualifyCodeEntity.class, forVariable(variable));
    }

    public QDisqualifyCodeEntity(Path<? extends DisqualifyCodeEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDisqualifyCodeEntity(PathMetadata metadata) {
        super(DisqualifyCodeEntity.class, metadata);
    }

}

