package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QJurorPaperResponseCJS is a Querydsl query type for JurorPaperResponseCJS
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QJurorPaperResponseCJS extends EntityPathBase<JurorPaperResponseCJS> {

    private static final long serialVersionUID = -1772571790L;

    public static final QJurorPaperResponseCJS jurorPaperResponseCJS = new QJurorPaperResponseCJS("jurorPaperResponseCJS");

    public final StringPath details = createString("details");

    public final StringPath employer = createString("employer");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jurorNumber = createString("jurorNumber");

    public QJurorPaperResponseCJS(String variable) {
        super(JurorPaperResponseCJS.class, forVariable(variable));
    }

    public QJurorPaperResponseCJS(Path<? extends JurorPaperResponseCJS> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJurorPaperResponseCJS(PathMetadata metadata) {
        super(JurorPaperResponseCJS.class, metadata);
    }

}

