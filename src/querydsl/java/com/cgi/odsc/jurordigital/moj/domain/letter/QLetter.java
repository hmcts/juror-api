package com.cgi.odsc.jurordigital.moj.domain.letter;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QLetter is a Querydsl query type for Letter
 */
@Generated("com.querydsl.codegen.SupertypeSerializer")
public class QLetter extends EntityPathBase<Letter> {

    private static final long serialVersionUID = 793411351L;

    public static final QLetter letter = new QLetter("letter");

    public final DateTimePath<java.time.LocalDateTime> datePrinted = createDateTime("datePrinted", java.time.LocalDateTime.class);

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath owner = createString("owner");

    public final BooleanPath printed = createBoolean("printed");

    public QLetter(String variable) {
        super(Letter.class, forVariable(variable));
    }

    public QLetter(Path<? extends Letter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLetter(PathMetadata metadata) {
        super(Letter.class, metadata);
    }

}

