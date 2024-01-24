package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPoolCourt is a Querydsl query type for PoolCourt
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolCourt extends EntityPathBase<PoolCourt> {

    private static final long serialVersionUID = 285013654L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPoolCourt poolCourt = new QPoolCourt("poolCourt");

    public final StringPath address = createString("address");

    public final StringPath address2 = createString("address2");

    public final StringPath address3 = createString("address3");

    public final StringPath address4 = createString("address4");

    public final StringPath address5 = createString("address5");

    public final StringPath address6 = createString("address6");

    public final StringPath altPhoneNumber = createString("altPhoneNumber");

    public final DateTimePath<java.util.Date> completionDate = createDateTime("completionDate", java.util.Date.class);

    public final QCourtLocation court;

    public final DateTimePath<java.util.Date> dateOfBirth = createDateTime("dateOfBirth", java.util.Date.class);

    public final DateTimePath<java.util.Date> deferralDate = createDateTime("deferralDate", java.util.Date.class);

    public final StringPath disqualifyCode = createString("disqualifyCode");

    public final DateTimePath<java.util.Date> disqualifyDate = createDateTime("disqualifyDate", java.util.Date.class);

    public final StringPath email = createString("email");

    public final StringPath excusalCode = createString("excusalCode");

    public final DateTimePath<java.util.Date> excusalDate = createDateTime("excusalDate", java.util.Date.class);

    public final StringPath excusalRejected = createString("excusalRejected");

    public final StringPath firstName = createString("firstName");

    public final DateTimePath<java.util.Date> hearingDate = createDateTime("hearingDate", java.util.Date.class);

    public final StringPath isActive = createString("isActive");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath lastName = createString("lastName");

    public final NumberPath<Long> noDefPos = createNumber("noDefPos", Long.class);

    public final StringPath notes = createString("notes");

    public final NumberPath<Integer> notifications = createNumber("notifications", Integer.class);

    public final StringPath owner = createString("owner");

    public final DateTimePath<java.util.Date> phoenixDate = createDateTime("phoenixDate", java.util.Date.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath policeCheck = createString("policeCheck");

    public final QPoolExtend poolExtend;

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath postcode = createString("postcode");

    public final BooleanPath readOnly = createBoolean("readOnly");

    public final StringPath responded = createString("responded");

    public final StringPath serviceCompCommsStatus = createString("serviceCompCommsStatus");

    public final StringPath specialNeed = createString("specialNeed");

    public final NumberPath<Long> status = createNumber("status", Long.class);

    public final StringPath title = createString("title");

    public final StringPath userEdtq = createString("userEdtq");

    public final StringPath welsh = createString("welsh");

    public final StringPath workPhone = createString("workPhone");

    public QPoolCourt(String variable) {
        this(PoolCourt.class, forVariable(variable), INITS);
    }

    public QPoolCourt(Path<? extends PoolCourt> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPoolCourt(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPoolCourt(PathMetadata metadata, PathInits inits) {
        this(PoolCourt.class, metadata, inits);
    }

    public QPoolCourt(Class<? extends PoolCourt> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.court = inits.isInitialized("court") ? new QCourtLocation(forProperty("court"), inits.get("court")) : null;
        this.poolExtend = inits.isInitialized("poolExtend") ? new QPoolExtend(forProperty("poolExtend")) : null;
    }

}

