package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPartAmendment is a Querydsl query type for PartAmendment
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPartAmendment extends EntityPathBase<PartAmendment> {

    private static final long serialVersionUID = -833689129L;

    public static final QPartAmendment partAmendment = new QPartAmendment("partAmendment");

    public final StringPath address = createString("address");

    public final DateTimePath<java.util.Date> dateOfBirth = createDateTime("dateOfBirth", java.util.Date.class);

    public final DateTimePath<java.util.Date> editdate = createDateTime("editdate", java.util.Date.class);

    public final StringPath editUserId = createString("editUserId");

    public final StringPath firstName = createString("firstName");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath lastName = createString("lastName");

    public final StringPath owner = createString("owner");

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath postcode = createString("postcode");

    public final StringPath title = createString("title");

    public QPartAmendment(String variable) {
        super(PartAmendment.class, forVariable(variable));
    }

    public QPartAmendment(Path<? extends PartAmendment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPartAmendment(PathMetadata metadata) {
        super(PartAmendment.class, metadata);
    }

}

