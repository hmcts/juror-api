package com.cgi.odsc.jurordigital.moj.domain.letter;

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

    private static final long serialVersionUID = -1875844023L;

    public static final QExcusalDeniedLetter excusalDeniedLetter = new QExcusalDeniedLetter("excusalDeniedLetter");

    public final QLetter _super = new QLetter(this);

    public final DatePath<java.time.LocalDate> dateExcused = createDate("dateExcused", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> datePrinted = _super.datePrinted;

    public final StringPath excCode = createString("excCode");

    //inherited
    public final StringPath jurorNumber = _super.jurorNumber;

    //inherited
    public final StringPath owner = _super.owner;

    //inherited
    public final BooleanPath printed = _super.printed;

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

