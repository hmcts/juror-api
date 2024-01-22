package com.cgi.odsc.jurordigital.moj.domain.letter;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPostponementLetter is a Querydsl query type for PostponementLetter
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPostponementLetter extends EntityPathBase<PostponementLetter> {

    private static final long serialVersionUID = -268485205L;

    public static final QPostponementLetter postponementLetter = new QPostponementLetter("postponementLetter");

    public final QLetter _super = new QLetter(this);

    public final DatePath<java.time.LocalDate> datePostpone = createDate("datePostpone", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> datePrinted = _super.datePrinted;

    //inherited
    public final StringPath jurorNumber = _super.jurorNumber;

    //inherited
    public final StringPath owner = _super.owner;

    //inherited
    public final BooleanPath printed = _super.printed;

    public QPostponementLetter(String variable) {
        super(PostponementLetter.class, forVariable(variable));
    }

    public QPostponementLetter(Path<? extends PostponementLetter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPostponementLetter(PathMetadata metadata) {
        super(PostponementLetter.class, metadata);
    }

}

