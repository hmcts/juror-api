package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJurorPaperResponse is a Querydsl query type for JurorPaperResponse
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QJurorPaperResponse extends EntityPathBase<JurorPaperResponse> {

    private static final long serialVersionUID = 1871266586L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJurorPaperResponse jurorPaperResponse = new QJurorPaperResponse("jurorPaperResponse");

    public final StringPath address = createString("address");

    public final StringPath address2 = createString("address2");

    public final StringPath address3 = createString("address3");

    public final StringPath address4 = createString("address4");

    public final StringPath address5 = createString("address5");

    public final StringPath altPhoneNumber = createString("altPhoneNumber");

    public final BooleanPath bail = createBoolean("bail");

    public final ListPath<JurorPaperResponseCJS, QJurorPaperResponseCJS> cjsEmployments = this.<JurorPaperResponseCJS, QJurorPaperResponseCJS>createList("cjsEmployments", JurorPaperResponseCJS.class, QJurorPaperResponseCJS.class, PathInits.DIRECT2);

    public final DatePath<java.time.LocalDate> completedAt = createDate("completedAt", java.time.LocalDate.class);

    public final ListPath<ContactLog, QContactLog> contactLog = this.<ContactLog, QContactLog>createList("contactLog", ContactLog.class, QContactLog.class, PathInits.DIRECT2);

    public final BooleanPath convictions = createBoolean("convictions");

    public final DatePath<java.time.LocalDate> dateOfBirth = createDate("dateOfBirth", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> dateReceived = createDate("dateReceived", java.time.LocalDate.class);

    public final BooleanPath deferral = createBoolean("deferral");

    public final StringPath email = createString("email");

    public final BooleanPath excusal = createBoolean("excusal");

    public final StringPath firstName = createString("firstName");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath lastName = createString("lastName");

    public final BooleanPath mentalHealthAct = createBoolean("mentalHealthAct");

    public final BooleanPath mentalHealthCapacity = createBoolean("mentalHealthCapacity");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath postcode = createString("postcode");

    public final BooleanPath processingComplete = createBoolean("processingComplete");

    public final EnumPath<com.cgi.odsc.jurordigital.juror.domain.ProcessingStatus> processingStatus = createEnum("processingStatus", com.cgi.odsc.jurordigital.juror.domain.ProcessingStatus.class);

    public final StringPath relationship = createString("relationship");

    public final BooleanPath residency = createBoolean("residency");

    public final BooleanPath signed = createBoolean("signed");

    public final ListPath<JurorPaperResponseSpecialNeed, QJurorPaperResponseSpecialNeed> specialNeeds = this.<JurorPaperResponseSpecialNeed, QJurorPaperResponseSpecialNeed>createList("specialNeeds", JurorPaperResponseSpecialNeed.class, QJurorPaperResponseSpecialNeed.class, PathInits.DIRECT2);

    public final StringPath specialNeedsArrangements = createString("specialNeedsArrangements");

    public final com.cgi.odsc.jurordigital.bureau.domain.QStaff staff;

    public final BooleanPath superUrgent = createBoolean("superUrgent");

    public final StringPath thirdPartyReason = createString("thirdPartyReason");

    public final StringPath title = createString("title");

    public final BooleanPath urgent = createBoolean("urgent");

    public final BooleanPath welsh = createBoolean("welsh");

    public QJurorPaperResponse(String variable) {
        this(JurorPaperResponse.class, forVariable(variable), INITS);
    }

    public QJurorPaperResponse(Path<? extends JurorPaperResponse> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJurorPaperResponse(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJurorPaperResponse(PathMetadata metadata, PathInits inits) {
        this(JurorPaperResponse.class, metadata, inits);
    }

    public QJurorPaperResponse(Class<? extends JurorPaperResponse> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.staff = inits.isInitialized("staff") ? new com.cgi.odsc.jurordigital.bureau.domain.QStaff(forProperty("staff"), inits.get("staff")) : null;
    }

}

