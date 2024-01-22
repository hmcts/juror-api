package com.cgi.odsc.jurordigital.moj.domain.letter;

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

    private static final long serialVersionUID = 537426638L;

    public static final QExcusalLetter excusalLetter = new QExcusalLetter("excusalLetter");

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

