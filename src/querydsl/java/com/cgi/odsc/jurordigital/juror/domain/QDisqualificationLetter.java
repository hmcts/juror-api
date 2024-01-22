package com.cgi.odsc.jurordigital.juror.domain;

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

    private static final long serialVersionUID = 2045395776L;

    public static final QDisqualificationLetter disqualificationLetter = new QDisqualificationLetter("disqualificationLetter");

    public final DateTimePath<java.util.Date> dateDisq = createDateTime("dateDisq", java.util.Date.class);

    public final DateTimePath<java.util.Date> datePrinted = createDateTime("datePrinted", java.util.Date.class);

    public final StringPath disqCode = createString("disqCode");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath owner = createString("owner");

    public final BooleanPath printed = createBoolean("printed");

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

