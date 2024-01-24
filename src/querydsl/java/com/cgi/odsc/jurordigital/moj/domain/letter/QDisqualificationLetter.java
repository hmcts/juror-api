package com.cgi.odsc.jurordigital.moj.domain.letter;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QDisqualificationLetter is a Querydsl query type for DisqualificationLetter
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QDisqualificationLetter extends EntityPathBase<DisqualificationLetter> {

    private static final long serialVersionUID = -2072478568L;

    public static final QDisqualificationLetter disqualificationLetter = new QDisqualificationLetter("disqualificationLetter");

    public final QLetter _super = new QLetter(this);

    public final DatePath<java.time.LocalDate> dateDisq = createDate("dateDisq", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> datePrinted = _super.datePrinted;

    public final StringPath disqCode = createString("disqCode");

    //inherited
    public final StringPath jurorNumber = _super.jurorNumber;

    //inherited
    public final StringPath owner = _super.owner;

    //inherited
    public final BooleanPath printed = _super.printed;

    public QDisqualificationLetter(String variable) {
        super(DisqualificationLetter.class, forVariable(variable));
    }

    public QDisqualificationLetter(Path<? extends DisqualificationLetter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDisqualificationLetter(PathMetadata metadata) {
        super(DisqualificationLetter.class, metadata);
    }

}

