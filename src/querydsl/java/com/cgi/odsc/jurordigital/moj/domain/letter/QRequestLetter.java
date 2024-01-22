package com.cgi.odsc.jurordigital.moj.domain.letter;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QRequestLetter is a Querydsl query type for RequestLetter
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QRequestLetter extends EntityPathBase<RequestLetter> {

    private static final long serialVersionUID = -1471463548L;

    public static final QRequestLetter requestLetter = new QRequestLetter("requestLetter");

    public final QLetter _super = new QLetter(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> datePrinted = _super.datePrinted;

    //inherited
    public final StringPath jurorNumber = _super.jurorNumber;

    //inherited
    public final StringPath owner = _super.owner;

    //inherited
    public final BooleanPath printed = _super.printed;

    public final StringPath requiredInformation = createString("requiredInformation");

    public QRequestLetter(String variable) {
        super(RequestLetter.class, forVariable(variable));
    }

    public QRequestLetter(Path<? extends RequestLetter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRequestLetter(PathMetadata metadata) {
        super(RequestLetter.class, metadata);
    }

}

