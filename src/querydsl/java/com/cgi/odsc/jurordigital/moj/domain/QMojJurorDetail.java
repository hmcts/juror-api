package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMojJurorDetail is a Querydsl query type for MojJurorDetail
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QMojJurorDetail extends EntityPathBase<MojJurorDetail> {

    private static final long serialVersionUID = 1017075370L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMojJurorDetail mojJurorDetail = new QMojJurorDetail("mojJurorDetail");

    public final StringPath altPhoneNumber = createString("altPhoneNumber");

    public final com.cgi.odsc.jurordigital.bureau.domain.QStaff assignedStaffMember;

    public final StringPath bail = createString("bail");

    public final StringPath bailDetails = createString("bailDetails");

    public final SetPath<com.cgi.odsc.jurordigital.bureau.domain.ChangeLog, com.cgi.odsc.jurordigital.bureau.domain.QChangeLog> changeLogs = this.<com.cgi.odsc.jurordigital.bureau.domain.ChangeLog, com.cgi.odsc.jurordigital.bureau.domain.QChangeLog>createSet("changeLogs", com.cgi.odsc.jurordigital.bureau.domain.ChangeLog.class, com.cgi.odsc.jurordigital.bureau.domain.QChangeLog.class, PathInits.DIRECT2);

    public final ListPath<com.cgi.odsc.jurordigital.bureau.domain.BureauJurorCJS, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorCJS> cjsEmployments = this.<com.cgi.odsc.jurordigital.bureau.domain.BureauJurorCJS, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorCJS>createList("cjsEmployments", com.cgi.odsc.jurordigital.bureau.domain.BureauJurorCJS.class, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorCJS.class, PathInits.DIRECT2);

    public final DateTimePath<java.util.Date> completedAt = createDateTime("completedAt", java.util.Date.class);

    public final StringPath convictions = createString("convictions");

    public final StringPath convictionsDetails = createString("convictionsDetails");

    public final StringPath courtAddress1 = createString("courtAddress1");

    public final StringPath courtAddress2 = createString("courtAddress2");

    public final StringPath courtAddress3 = createString("courtAddress3");

    public final StringPath courtAddress4 = createString("courtAddress4");

    public final StringPath courtAddress5 = createString("courtAddress5");

    public final StringPath courtAddress6 = createString("courtAddress6");

    public final StringPath courtCode = createString("courtCode");

    public final StringPath courtLocName = createString("courtLocName");

    public final StringPath courtName = createString("courtName");

    public final StringPath courtPostcode = createString("courtPostcode");

    public final DateTimePath<java.util.Date> dateOfBirth = createDateTime("dateOfBirth", java.util.Date.class);

    public final DateTimePath<java.util.Date> dateReceived = createDateTime("dateReceived", java.util.Date.class);

    public final StringPath deferralDate = createString("deferralDate");

    public final StringPath deferralReason = createString("deferralReason");

    public final StringPath email = createString("email");

    public final StringPath excusalReason = createString("excusalReason");

    public final StringPath firstName = createString("firstName");

    public final DateTimePath<java.util.Date> hearingDate = createDateTime("hearingDate", java.util.Date.class);

    public final StringPath hearingTime = createString("hearingTime");

    public final StringPath jurorAddress1 = createString("jurorAddress1");

    public final StringPath jurorAddress2 = createString("jurorAddress2");

    public final StringPath jurorAddress3 = createString("jurorAddress3");

    public final StringPath jurorAddress4 = createString("jurorAddress4");

    public final StringPath jurorAddress5 = createString("jurorAddress5");

    public final StringPath jurorAddress6 = createString("jurorAddress6");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath jurorPostcode = createString("jurorPostcode");

    public final StringPath lastName = createString("lastName");

    public final StringPath mentalHealthAct = createString("mentalHealthAct");

    public final StringPath mentalHealthActDetails = createString("mentalHealthActDetails");

    public final StringPath newAltPhoneNumber = createString("newAltPhoneNumber");

    public final DateTimePath<java.util.Date> newDateOfBirth = createDateTime("newDateOfBirth", java.util.Date.class);

    public final StringPath newEmail = createString("newEmail");

    public final StringPath newFirstName = createString("newFirstName");

    public final StringPath newJurorAddress1 = createString("newJurorAddress1");

    public final StringPath newJurorAddress2 = createString("newJurorAddress2");

    public final StringPath newJurorAddress3 = createString("newJurorAddress3");

    public final StringPath newJurorAddress4 = createString("newJurorAddress4");

    public final StringPath newJurorAddress5 = createString("newJurorAddress5");

    public final StringPath newJurorAddress6 = createString("newJurorAddress6");

    public final StringPath newJurorPostcode = createString("newJurorPostcode");

    public final StringPath newLastName = createString("newLastName");

    public final StringPath newPhoneNumber = createString("newPhoneNumber");

    public final StringPath newTitle = createString("newTitle");

    public final StringPath notes = createString("notes");

    public final ListPath<com.cgi.odsc.jurordigital.bureau.domain.PhoneLog, com.cgi.odsc.jurordigital.bureau.domain.QPhoneLog> phoneLogs = this.<com.cgi.odsc.jurordigital.bureau.domain.PhoneLog, com.cgi.odsc.jurordigital.bureau.domain.QPhoneLog>createList("phoneLogs", com.cgi.odsc.jurordigital.bureau.domain.PhoneLog.class, com.cgi.odsc.jurordigital.bureau.domain.QPhoneLog.class, PathInits.DIRECT2);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final DateTimePath<java.util.Date> poolDate = createDateTime("poolDate", java.util.Date.class);

    public final StringPath poolNumber = createString("poolNumber");

    public final BooleanPath processingComplete = createBoolean("processingComplete");

    public final StringPath processingStatus = createString("processingStatus");

    public final StringPath readOnly = createString("readOnly");

    public final StringPath residency = createString("residency");

    public final StringPath residencyDetail = createString("residencyDetail");

    public final ListPath<com.cgi.odsc.jurordigital.bureau.domain.BureauJurorSpecialNeed, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorSpecialNeed> specialNeeds = this.<com.cgi.odsc.jurordigital.bureau.domain.BureauJurorSpecialNeed, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorSpecialNeed>createList("specialNeeds", com.cgi.odsc.jurordigital.bureau.domain.BureauJurorSpecialNeed.class, com.cgi.odsc.jurordigital.bureau.domain.QBureauJurorSpecialNeed.class, PathInits.DIRECT2);

    public final StringPath specialNeedsArrangements = createString("specialNeedsArrangements");

    public final DateTimePath<java.util.Date> staffAssignmentDate = createDateTime("staffAssignmentDate", java.util.Date.class);

    public final NumberPath<Long> status = createNumber("status", Long.class);

    public final BooleanPath superUrgent = createBoolean("superUrgent");

    public final StringPath thirdPartyAlternatePhoneNumber = createString("thirdPartyAlternatePhoneNumber");

    public final StringPath thirdPartyEmailAddress = createString("thirdPartyEmailAddress");

    public final StringPath thirdPartyFirstName = createString("thirdPartyFirstName");

    public final StringPath thirdPartyLastName = createString("thirdPartyLastName");

    public final StringPath thirdPartyMainPhoneNumber = createString("thirdPartyMainPhoneNumber");

    public final StringPath thirdPartyOtherReason = createString("thirdPartyOtherReason");

    public final StringPath thirdPartyReason = createString("thirdPartyReason");

    public final StringPath thirdPartyRelationship = createString("thirdPartyRelationship");

    public final StringPath title = createString("title");

    public final BooleanPath urgent = createBoolean("urgent");

    public final BooleanPath useJurorEmailDetails = createBoolean("useJurorEmailDetails");

    public final BooleanPath useJurorPhoneDetails = createBoolean("useJurorPhoneDetails");

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public final BooleanPath welsh = createBoolean("welsh");

    public final BooleanPath welshCourt = createBoolean("welshCourt");

    public QMojJurorDetail(String variable) {
        this(MojJurorDetail.class, forVariable(variable), INITS);
    }

    public QMojJurorDetail(Path<? extends MojJurorDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMojJurorDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMojJurorDetail(PathMetadata metadata, PathInits inits) {
        this(MojJurorDetail.class, metadata, inits);
    }

    public QMojJurorDetail(Class<? extends MojJurorDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.assignedStaffMember = inits.isInitialized("assignedStaffMember") ? new com.cgi.odsc.jurordigital.bureau.domain.QStaff(forProperty("assignedStaffMember"), inits.get("assignedStaffMember")) : null;
    }

}

