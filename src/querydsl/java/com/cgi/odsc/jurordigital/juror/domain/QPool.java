package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPool is a Querydsl query type for Pool
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPool extends EntityPathBase<Pool> {

    private static final long serialVersionUID = 1577294837L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPool pool = new QPool("pool");

    public final StringPath address = createString("address");

    public final StringPath address2 = createString("address2");

    public final StringPath address3 = createString("address3");

    public final StringPath address4 = createString("address4");

    public final StringPath address5 = createString("address5");

    public final StringPath address6 = createString("address6");

    public final StringPath altPhoneNumber = createString("altPhoneNumber");

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

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath lastName = createString("lastName");

    public final NumberPath<Long> noDefPos = createNumber("noDefPos", Long.class);

    public final StringPath notes = createString("notes");

    public final NumberPath<Integer> notifications = createNumber("notifications", Integer.class);

    public final DateTimePath<java.util.Date> phoenixDate = createDateTime("phoenixDate", java.util.Date.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath policeCheck = createString("policeCheck");

    public final QPoolExtend poolExtend;

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath postcode = createString("postcode");

    public final BooleanPath readOnly = createBoolean("readOnly");

    public final StringPath responded = createString("responded");

    public final DateTimePath<java.util.Date> returnDate = createDateTime("returnDate", java.util.Date.class);

    public final StringPath specialNeed = createString("specialNeed");

    public final NumberPath<Long> status = createNumber("status", Long.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.util.Date> transferDate = createDateTime("transferDate", java.util.Date.class);

    public final StringPath userEdtq = createString("userEdtq");

    public final BooleanPath welsh = createBoolean("welsh");

    public final StringPath workPhone = createString("workPhone");

    public QPool(String variable) {
        this(Pool.class, forVariable(variable), INITS);
    }

    public QPool(Path<? extends Pool> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPool(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPool(PathMetadata metadata, PathInits inits) {
        this(Pool.class, metadata, inits);
    }

    public QPool(Class<? extends Pool> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.court = inits.isInitialized("court") ? new QCourtLocation(forProperty("court"), inits.get("court")) : null;
        this.poolExtend = inits.isInitialized("poolExtend") ? new QPoolExtend(forProperty("poolExtend")) : null;
    }

}

