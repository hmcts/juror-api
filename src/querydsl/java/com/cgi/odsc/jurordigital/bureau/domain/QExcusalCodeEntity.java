package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QExcusalCodeEntity is a Querydsl query type for ExcusalCodeEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QExcusalCodeEntity extends EntityPathBase<ExcusalCodeEntity> {

    private static final long serialVersionUID = -159856282L;

    public static final QExcusalCodeEntity excusalCodeEntity = new QExcusalCodeEntity("excusalCodeEntity");

    public final StringPath description = createString("description");

    public final StringPath excusalCode = createString("excusalCode");

    public QExcusalCodeEntity(String variable) {
        super(ExcusalCodeEntity.class, forVariable(variable));
    }

    public QExcusalCodeEntity(Path<? extends ExcusalCodeEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExcusalCodeEntity(PathMetadata metadata) {
        super(ExcusalCodeEntity.class, metadata);
    }

}

