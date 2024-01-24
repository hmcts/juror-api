package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJurorResponse is a Querydsl query type for JurorResponse
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QJurorResponse extends EntityPathBase<JurorResponse> {

    private static final long serialVersionUID = 533795666L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJurorResponse jurorResponse = new QJurorResponse("jurorResponse");

    public final StringPath address = createString("address");

    public final StringPath address2 = createString("address2");

    public final StringPath address3 = createString("address3");

    public final StringPath address4 = createString("address4");

    public final StringPath address5 = createString("address5");

    public final StringPath altPhoneNumber = createString("altPhoneNumber");

    public final BooleanPath bail = createBoolean("bail");

    public final StringPath bailDetails = createString("bailDetails");

    public final ListPath<com.cgi.odsc.jurordigital.bureau.domain.BureauJurorCJS, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorCJS> cjsEmployments = this.<com.cgi.odsc.jurordigital.bureau.domain.BureauJurorCJS, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorCJS>createList("cjsEmployments", com.cgi.odsc.jurordigital.bureau.domain.BureauJurorCJS.class, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorCJS.class, PathInits.DIRECT2);

    public final DateTimePath<java.util.Date> completedAt = createDateTime("completedAt", java.util.Date.class);

    public final BooleanPath convictions = createBoolean("convictions");

    public final StringPath convictionsDetails = createString("convictionsDetails");

    public final DateTimePath<java.util.Date> dateOfBirth = createDateTime("dateOfBirth", java.util.Date.class);

    public final DateTimePath<java.util.Date> dateReceived = createDateTime("dateReceived", java.util.Date.class);

    public final StringPath deferralDate = createString("deferralDate");

    public final StringPath deferralReason = createString("deferralReason");

    public final StringPath email = createString("email");

    public final StringPath emailAddress = createString("emailAddress");

    public final StringPath excusalReason = createString("excusalReason");

    public final StringPath firstName = createString("firstName");

    public final BooleanPath jurorEmailDetails = createBoolean("jurorEmailDetails");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final BooleanPath jurorPhoneDetails = createBoolean("jurorPhoneDetails");

    public final StringPath lastName = createString("lastName");

    public final StringPath mainPhone = createString("mainPhone");

    public final BooleanPath mentalHealthAct = createBoolean("mentalHealthAct");

    public final StringPath mentalHealthActDetails = createString("mentalHealthActDetails");

    public final StringPath otherPhone = createString("otherPhone");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath postcode = createString("postcode");

    public final BooleanPath processingComplete = createBoolean("processingComplete");

    public final EnumPath<ProcessingStatus> processingStatus = createEnum("processingStatus", ProcessingStatus.class);

    public final StringPath relationship = createString("relationship");

    public final BooleanPath residency = createBoolean("residency");

    public final StringPath residencyDetail = createString("residencyDetail");

    public final ListPath<com.cgi.odsc.jurordigital.bureau.domain.BureauJurorSpecialNeed, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorSpecialNeed> specialNeeds = this.<com.cgi.odsc.jurordigital.bureau.domain.BureauJurorSpecialNeed, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorSpecialNeed>createList("specialNeeds", com.cgi.odsc.jurordigital.bureau.domain.BureauJurorSpecialNeed.class, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorSpecialNeed.class, PathInits.DIRECT2);

    public final StringPath specialNeedsArrangements = createString("specialNeedsArrangements");

    public final com.cgi.odsc.jurordigital.bureau.domain.QStaff staff;

    public final DateTimePath<java.util.Date> staffAssignmentDate = createDateTime("staffAssignmentDate", java.util.Date.class);

    public final BooleanPath superUrgent = createBoolean("superUrgent");

    public final StringPath thirdPartyFName = createString("thirdPartyFName");

    public final StringPath thirdPartyLName = createString("thirdPartyLName");

    public final StringPath thirdPartyOtherReason = createString("thirdPartyOtherReason");

    public final StringPath thirdPartyReason = createString("thirdPartyReason");

    public final StringPath title = createString("title");

    public final BooleanPath urgent = createBoolean("urgent");

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public final BooleanPath welsh = createBoolean("welsh");

    public QJurorResponse(String variable) {
        this(JurorResponse.class, forVariable(variable), INITS);
    }

    public QJurorResponse(Path<? extends JurorResponse> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJurorResponse(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJurorResponse(PathMetadata metadata, PathInits inits) {
        this(JurorResponse.class, metadata, inits);
    }

    public QJurorResponse(Class<? extends JurorResponse> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.staff = inits.isInitialized("staff") ? new com.cgi.odsc.jurordigital.bureau.domain.QStaff(forProperty("staff"), inits.get("staff")) : null;
    }

}

