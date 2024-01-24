package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QSystemParameter is a Querydsl query type for SystemParameter
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QSystemParameter extends EntityPathBase<SystemParameter> {

    private static final long serialVersionUID = -272464777L;

    public static final QSystemParameter systemParameter = new QSystemParameter("systemParameter");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdDate = createDateTime("createdDate", java.util.Date.class);

    public final StringPath spDesc = createString("spDesc");

    public final NumberPath<Integer> spId = createNumber("spId", Integer.class);

    public final StringPath spValue = createString("spValue");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedDate = createDateTime("updatedDate", java.util.Date.class);

    public QSystemParameter(String variable) {
        super(SystemParameter.class, forVariable(variable));
    }

    public QSystemParameter(Path<? extends SystemParameter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSystemParameter(PathMetadata metadata) {
        super(SystemParameter.class, metadata);
    }

}

