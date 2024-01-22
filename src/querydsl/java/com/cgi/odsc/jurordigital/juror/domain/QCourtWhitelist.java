package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QCourtWhitelist is a Querydsl query type for CourtWhitelist
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCourtWhitelist extends EntityPathBase<CourtWhitelist> {

    private static final long serialVersionUID = -423271531L;

    public static final QCourtWhitelist courtWhitelist = new QCourtWhitelist("courtWhitelist");

    public final StringPath locCode = createString("locCode");

    public QCourtWhitelist(String variable) {
        super(CourtWhitelist.class, forVariable(variable));
    }

    public QCourtWhitelist(Path<? extends CourtWhitelist> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCourtWhitelist(PathMetadata metadata) {
        super(CourtWhitelist.class, metadata);
    }

}

