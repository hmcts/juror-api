package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPoolMember is a Querydsl query type for PoolMember
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPoolMember extends EntityPathBase<PoolMember> {

    private static final long serialVersionUID = 1620592269L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPoolMember poolMember = new QPoolMember("poolMember");

    public final StringPath addressLine1 = createString("addressLine1");

    public final StringPath addressLine2 = createString("addressLine2");

    public final StringPath addressLine3 = createString("addressLine3");

    public final StringPath addressLine4 = createString("addressLine4");

    public final StringPath addressLine5 = createString("addressLine5");

    public final StringPath addressLine6 = createString("addressLine6");

    public final StringPath altPhoneNumber = createString("altPhoneNumber");

    public final NumberPath<Double> amountSpent = createNumber("amountSpent", Double.class);

    public final StringPath bankAccountName = createString("bankAccountName");

    public final StringPath bankAccountNumber = createString("bankAccountNumber");

    public final StringPath buildingSocietyRollNumber = createString("buildingSocietyRollNumber");

    public final DatePath<java.time.LocalDate> completionDate = createDate("completionDate", java.time.LocalDate.class);

    public final BooleanPath completionFlag = createBoolean("completionFlag");

    public final NumberPath<Integer> contactPreference = createNumber("contactPreference", Integer.class);

    public final com.cgi.odsc.jurordigital.juror.domain.QCourtLocation court;

    public final DateTimePath<java.util.Date> dateOfBirth = createDateTime("dateOfBirth", java.util.Date.class);

    public final DatePath<java.time.LocalDate> deferralDate = createDate("deferralDate", java.time.LocalDate.class);

    public final StringPath disqualifyCode = createString("disqualifyCode");

    public final DatePath<java.time.LocalDate> disqualifyDate = createDate("disqualifyDate", java.time.LocalDate.class);

    public final ComparablePath<Character> editTag = createComparable("editTag", Character.class);

    public final StringPath email = createString("email");

    public final StringPath excusalCode = createString("excusalCode");

    public final DatePath<java.time.LocalDate> excusalDate = createDate("excusalDate", java.time.LocalDate.class);

    public final StringPath excusalRejected = createString("excusalRejected");

    public final NumberPath<Integer> failedToAttendCount = createNumber("failedToAttendCount", Integer.class);

    public final NumberPath<Double> financialLoss = createNumber("financialLoss", Double.class);

    public final StringPath firstName = createString("firstName");

    public final ComparablePath<Character> idChecked = createComparable("idChecked", Character.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath lastName = createString("lastName");

    public final DateTimePath<java.time.LocalDateTime> lastUpdate = createDateTime("lastUpdate", java.time.LocalDateTime.class);

    public final StringPath location = createString("location");

    public final NumberPath<Integer> mileage = createNumber("mileage", Integer.class);

    public final DatePath<java.time.LocalDate> nextDate = createDate("nextDate", java.time.LocalDate.class);

    public final NumberPath<Integer> noAttendances = createNumber("noAttendances", Integer.class);

    public final NumberPath<Integer> noAttended = createNumber("noAttended", Integer.class);

    public final NumberPath<Long> noDefPos = createNumber("noDefPos", Long.class);

    public final StringPath notes = createString("notes");

    public final NumberPath<Integer> notifications = createNumber("notifications", Integer.class);

    public final BooleanPath onCall = createBoolean("onCall");

    public final StringPath owner = createString("owner");

    public final BooleanPath paidCash = createBoolean("paidCash");

    public final BooleanPath payCountyEmp = createBoolean("payCountyEmp");

    public final BooleanPath payExpenses = createBoolean("payExpenses");

    public final BooleanPath permanentlyDisqualify = createBoolean("permanentlyDisqualify");

    public final ComparablePath<Character> phoenixChecked = createComparable("phoenixChecked", Character.class);

    public final DatePath<java.time.LocalDate> phoenixDate = createDate("phoenixDate", java.time.LocalDate.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath policeCheck = createString("policeCheck");

    public final StringPath pollNumber = createString("pollNumber");

    public final QPoolMemberExt poolMemberExt;

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath poolSequence = createString("poolSequence");

    public final QPoolType poolType;

    public final StringPath postcode = createString("postcode");

    public final BooleanPath postpone = createBoolean("postpone");

    public final BooleanPath readOnly = createBoolean("readOnly");

    public final ComparablePath<Character> regularOrSpecial = createComparable("regularOrSpecial", Character.class);

    public final BooleanPath reminderSent = createBoolean("reminderSent");

    public final BooleanPath responded = createBoolean("responded");

    public final StringPath scanCode = createString("scanCode");

    public final StringPath smartCard = createString("smartCard");

    public final StringPath sortCode = createString("sortCode");

    public final StringPath specialNeed = createString("specialNeed");

    public final StringPath specialNeedMessage = createString("specialNeedMessage");

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final QPoolStatus status;

    public final StringPath summonsFile = createString("summonsFile");

    public final NumberPath<Integer> timesSelected = createNumber("timesSelected", Integer.class);

    public final StringPath title = createString("title");

    public final DatePath<java.time.LocalDate> transferDate = createDate("transferDate", java.time.LocalDate.class);

    public final NumberPath<Double> travelTime = createNumber("travelTime", Double.class);

    public final StringPath trialNumber = createString("trialNumber");

    public final NumberPath<Integer> unauthorisedAbsenceCount = createNumber("unauthorisedAbsenceCount", Integer.class);

    public final StringPath userEdtq = createString("userEdtq");

    public final BooleanPath wasDeferred = createBoolean("wasDeferred");

    public final BooleanPath welsh = createBoolean("welsh");

    public final StringPath workPhone = createString("workPhone");

    public final StringPath workPhoneExtension = createString("workPhoneExtension");

    public QPoolMember(String variable) {
        this(PoolMember.class, forVariable(variable), INITS);
    }

    public QPoolMember(Path<? extends PoolMember> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPoolMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPoolMember(PathMetadata metadata, PathInits inits) {
        this(PoolMember.class, metadata, inits);
    }

    public QPoolMember(Class<? extends PoolMember> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.court = inits.isInitialized("court") ? new com.cgi.odsc.jurordigital.juror.domain.QCourtLocation(forProperty("court"), inits.get("court")) : null;
        this.poolMemberExt = inits.isInitialized("poolMemberExt") ? new QPoolMemberExt(forProperty("poolMemberExt")) : null;
        this.poolType = inits.isInitialized("poolType") ? new QPoolType(forProperty("poolType")) : null;
        this.status = inits.isInitialized("status") ? new QPoolStatus(forProperty("status")) : null;
    }

}

