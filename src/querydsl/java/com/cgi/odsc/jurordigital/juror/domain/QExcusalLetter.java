package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QExcusalLetter is a Querydsl query type for ExcusalLetter
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QExcusalLetter extends EntityPathBase<ExcusalLetter> {

    private static final long serialVersionUID = 1749358374L;

    public static final QExcusalLetter excusalLetter = new QExcusalLetter("excusalLetter");

    public final DateTimePath<java.util.Date> dateExcused = createDateTime("dateExcused", java.util.Date.class);

    public final DateTimePath<java.util.Date> datePrinted = createDateTime("datePrinted", java.util.Date.class);

    public final StringPath excusalCode = createString("excusalCode");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath owner = createString("owner");

    public final BooleanPath printed = createBoolean("printed");

    public QExcusalLetter(String variable) {
        super(ExcusalLetter.class, forVariable(variable));
    }

    public QExcusalLetter(Path<? extends ExcusalLetter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExcusalLetter(PathMetadata metadata) {
        super(ExcusalLetter.class, metadata);
    }

}

