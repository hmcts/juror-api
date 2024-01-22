package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QWelshCourtLocation is a Querydsl query type for WelshCourtLocation
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QWelshCourtLocation extends EntityPathBase<WelshCourtLocation> {

    private static final long serialVersionUID = -57962746L;

    public static final QWelshCourtLocation welshCourtLocation = new QWelshCourtLocation("welshCourtLocation");

    public final StringPath address1 = createString("address1");

    public final StringPath address2 = createString("address2");

    public final StringPath address3 = createString("address3");

    public final StringPath address4 = createString("address4");

    public final StringPath address5 = createString("address5");

    public final StringPath address6 = createString("address6");

    public final StringPath locationAddress = createString("locationAddress");

    public final StringPath locCode = createString("locCode");

    public final StringPath locCourtName = createString("locCourtName");

    public QWelshCourtLocation(String variable) {
        super(WelshCourtLocation.class, forVariable(variable));
    }

    public QWelshCourtLocation(Path<? extends WelshCourtLocation> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWelshCourtLocation(PathMetadata metadata) {
        super(WelshCourtLocation.class, metadata);
    }

}

