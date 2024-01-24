package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QBureauJurorCJS is a Querydsl query type for BureauJurorCJS
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBureauJurorCJS extends EntityPathBase<BureauJurorCJS> {

    private static final long serialVersionUID = 1260788895L;

    public static final QBureauJurorCJS bureauJurorCJS = new QBureauJurorCJS("bureauJurorCJS");

    public final StringPath details = createString("details");

    public final StringPath employer = createString("employer");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jurorNumber = createString("jurorNumber");

    public QBureauJurorCJS(String variable) {
        super(BureauJurorCJS.class, forVariable(variable));
    }

    public QBureauJurorCJS(Path<? extends BureauJurorCJS> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBureauJurorCJS(PathMetadata metadata) {
        super(BureauJurorCJS.class, metadata);
    }

}

