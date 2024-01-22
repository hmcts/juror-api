package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCourtLocation is a Querydsl query type for CourtLocation
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCourtLocation extends EntityPathBase<CourtLocation> {

    private static final long serialVersionUID = -1245848921L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCourtLocation courtLocation = new QCourtLocation("courtLocation");

    public final StringPath address1 = createString("address1");

    public final StringPath address2 = createString("address2");

    public final StringPath address3 = createString("address3");

    public final StringPath address4 = createString("address4");

    public final StringPath address5 = createString("address5");

    public final StringPath address6 = createString("address6");

    public final StringPath courtAttendTime = createString("courtAttendTime");

    public final StringPath courtFaxNo = createString("courtFaxNo");

    public final QCourtRegion courtRegion;

    public final StringPath insertIndicators = createString("insertIndicators");

    public final StringPath juryOfficerPhone = createString("juryOfficerPhone");

    public final StringPath locationAddress = createString("locationAddress");

    public final StringPath locCode = createString("locCode");

    public final StringPath locCourtName = createString("locCourtName");

    public final StringPath locPhone = createString("locPhone");

    public final StringPath name = createString("name");

    public final StringPath owner = createString("owner");

    public final ListPath<com.cgi.odsc.jurordigital.moj.domain.PoolRequest, com.cgi.odsc.jurordigital.moj.domain.QPoolRequest> poolRequests = this.<com.cgi.odsc.jurordigital.moj.domain.PoolRequest, com.cgi.odsc.jurordigital.moj.domain.QPoolRequest>createList("poolRequests", com.cgi.odsc.jurordigital.moj.domain.PoolRequest.class, com.cgi.odsc.jurordigital.moj.domain.QPoolRequest.class, PathInits.DIRECT2);

    public final StringPath postcode = createString("postcode");

    public final StringPath signatory = createString("signatory");

    public final NumberPath<Integer> votersLock = createNumber("votersLock", Integer.class);

    public final NumberPath<java.math.BigDecimal> yield = createNumber("yield", java.math.BigDecimal.class);

    public QCourtLocation(String variable) {
        this(CourtLocation.class, forVariable(variable), INITS);
    }

    public QCourtLocation(Path<? extends CourtLocation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCourtLocation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCourtLocation(PathMetadata metadata, PathInits inits) {
        this(CourtLocation.class, metadata, inits);
    }

    public QCourtLocation(Class<? extends CourtLocation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.courtRegion = inits.isInitialized("courtRegion") ? new QCourtRegion(forProperty("courtRegion")) : null;
    }

}

