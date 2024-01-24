package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QCourtCatchmentEntity is a Querydsl query type for CourtCatchmentEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCourtCatchmentEntity extends EntityPathBase<CourtCatchmentEntity> {

    private static final long serialVersionUID = 953476788L;

    public static final QCourtCatchmentEntity courtCatchmentEntity = new QCourtCatchmentEntity("courtCatchmentEntity");

    public final StringPath courtCode = createString("courtCode");

    public final StringPath postCode = createString("postCode");

    public QCourtCatchmentEntity(String variable) {
        super(CourtCatchmentEntity.class, forVariable(variable));
    }

    public QCourtCatchmentEntity(Path<? extends CourtCatchmentEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCourtCatchmentEntity(PathMetadata metadata) {
        super(CourtCatchmentEntity.class, metadata);
    }

}

