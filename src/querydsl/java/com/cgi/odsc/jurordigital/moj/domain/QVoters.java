package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QVoters is a Querydsl query type for Voters
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QVoters extends EntityPathBase<Voters> {

    private static final long serialVersionUID = 1180630754L;

    public static final QVoters voters = new QVoters("voters");

    public final StringPath address = createString("address");

    public final StringPath address2 = createString("address2");

    public final StringPath address3 = createString("address3");

    public final StringPath address4 = createString("address4");

    public final StringPath address5 = createString("address5");

    public final StringPath address6 = createString("address6");

    public final DateTimePath<java.util.Date> dateOfBirth = createDateTime("dateOfBirth", java.util.Date.class);

    public final DateTimePath<java.util.Date> dateSelected1 = createDateTime("dateSelected1", java.util.Date.class);

    public final StringPath firstName = createString("firstName");

    public final StringPath flags = createString("flags");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath lastName = createString("lastName");

    public final StringPath locCode = createString("locCode");

    public final StringPath newMarker = createString("newMarker");

    public final StringPath permDisqual = createString("permDisqual");

    public final StringPath pollNumber = createString("pollNumber");

    public final StringPath postcode = createString("postcode");

    public final NumberPath<java.math.BigDecimal> recNumber = createNumber("recNumber", java.math.BigDecimal.class);

    public final StringPath registerLett = createString("registerLett");

    public final StringPath rowId = createString("rowId");

    public final StringPath sourceId = createString("sourceId");

    public final StringPath title = createString("title");

    public QVoters(String variable) {
        super(Voters.class, forVariable(variable));
    }

    public QVoters(Path<? extends Voters> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVoters(PathMetadata metadata) {
        super(Voters.class, metadata);
    }

}

