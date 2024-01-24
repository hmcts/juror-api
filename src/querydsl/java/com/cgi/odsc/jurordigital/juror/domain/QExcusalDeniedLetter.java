package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QExcusalDeniedLetter is a Querydsl query type for ExcusalDeniedLetter
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QExcusalDeniedLetter extends EntityPathBase<ExcusalDeniedLetter> {

    private static final long serialVersionUID = 917298849L;

    public static final QExcusalDeniedLetter excusalDeniedLetter = new QExcusalDeniedLetter("excusalDeniedLetter");

    public final DateTimePath<java.util.Date> dateExcused = createDateTime("dateExcused", java.util.Date.class);

    public final DateTimePath<java.util.Date> datePrinted = createDateTime("datePrinted", java.util.Date.class);

    public final StringPath excusalCode = createString("excusalCode");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath owner = createString("owner");

    public final BooleanPath printed = createBoolean("printed");

    public QExcusalDeniedLetter(String variable) {
        super(ExcusalDeniedLetter.class, forVariable(variable));
    }

    public QExcusalDeniedLetter(Path<? extends ExcusalDeniedLetter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExcusalDeniedLetter(PathMetadata metadata) {
        super(ExcusalDeniedLetter.class, metadata);
    }

}

