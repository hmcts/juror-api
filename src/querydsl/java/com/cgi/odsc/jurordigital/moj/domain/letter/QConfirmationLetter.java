package com.cgi.odsc.jurordigital.moj.domain.letter;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QConfirmationLetter is a Querydsl query type for ConfirmationLetter
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QConfirmationLetter extends EntityPathBase<ConfirmationLetter> {

    private static final long serialVersionUID = 260135468L;

    public static final QConfirmationLetter confirmationLetter = new QConfirmationLetter("confirmationLetter");

    public final QLetter _super = new QLetter(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> datePrinted = _super.datePrinted;

    //inherited
    public final StringPath jurorNumber = _super.jurorNumber;

    //inherited
    public final StringPath owner = _super.owner;

    //inherited
    public final BooleanPath printed = _super.printed;

    public QConfirmationLetter(String variable) {
        super(ConfirmationLetter.class, forVariable(variable));
    }

    public QConfirmationLetter(Path<? extends ConfirmationLetter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QConfirmationLetter(PathMetadata metadata) {
        super(ConfirmationLetter.class, metadata);
    }

}

