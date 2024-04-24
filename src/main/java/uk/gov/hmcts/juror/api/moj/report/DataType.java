package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Getter
@SuppressWarnings("PMD.ArrayIsStoredDirectly")
public enum DataType implements IDataType {
    JUROR_NUMBER("Juror Number", String.class, QJuror.juror.jurorNumber, QJuror.juror),
    FIRST_NAME("First Name", String.class, QJuror.juror.firstName, QJuror.juror),
    LAST_NAME("Last Name", String.class, QJuror.juror.lastName, QJuror.juror),
    STATUS("Status", String.class, QJurorPool.jurorPool.status.statusDesc, QJurorPool.jurorPool),
    DEFERRALS("Deferrals", String.class, QJuror.juror.noDefPos, QJuror.juror),
    ABSENCES("Absences", Long.class,
        QAppearance.appearance.attendanceType.eq(AttendanceType.ABSENT).count()),
    MAIN_PHONE("Main Phone", String.class, QJuror.juror.phoneNumber, QJuror.juror),
    MOBILE_PHONE("Mobile Phone", String.class, QJuror.juror.altPhoneNumber, QJuror.juror),
    HOME_PHONE("Home Phone", String.class, QJuror.juror.phoneNumber, QJuror.juror),

    OTHER_PHONE("Other Phone", String.class, QJuror.juror.altPhoneNumber, QJuror.juror),
    WORK_PHONE("Work Phone", String.class, QJuror.juror.workPhone, QJuror.juror),
    EMAIL("Email", String.class, QJuror.juror.email, QJuror.juror),
    CONTACT_DETAILS("Contact Details", List.class, MAIN_PHONE, OTHER_PHONE, WORK_PHONE, EMAIL),
    WARNING("Warning", String.class, new CaseBuilder()
        .when(QJuror.juror.policeCheck.isNull()
            .or(QJuror.juror.policeCheck.notIn(PoliceCheck.ELIGIBLE, PoliceCheck.INELIGIBLE)))
        .then("Not police checked")
        .when(QJuror.juror.policeCheck.eq(PoliceCheck.INELIGIBLE)).then("Failed police check")
        .otherwise(""), QJuror.juror),

    // juror address data types
    JUROR_ADDRESS_LINE_1("Address Line 1", String.class, QJuror.juror.addressLine1, QJuror.juror),
    JUROR_ADDRESS_LINE_2("Address Line 2", String.class, QJuror.juror.addressLine2, QJuror.juror),
    JUROR_ADDRESS_LINE_3("Address Line 3", String.class, QJuror.juror.addressLine3, QJuror.juror),
    JUROR_ADDRESS_LINE_4("Address Line 4", String.class, QJuror.juror.addressLine4, QJuror.juror),
    JUROR_ADDRESS_LINE_5("Address Line 5", String.class, QJuror.juror.addressLine5, QJuror.juror),
    JUROR_POSTCODE("Postcode", String.class, QJuror.juror.postcode, QJuror.juror),
    JUROR_POSTAL_ADDRESS("Address", List.class, JUROR_ADDRESS_LINE_1, JUROR_ADDRESS_LINE_2,
        JUROR_ADDRESS_LINE_3, JUROR_ADDRESS_LINE_4, JUROR_ADDRESS_LINE_5, JUROR_POSTCODE),

    POSTPONED_TO("Postponed to", LocalDate.class, QJurorPool.jurorPool.deferralDate, QJuror.juror),

    DEFERRED_TO("Deferred to", LocalDate.class, QJurorPool.jurorPool.deferralDate, QJuror.juror),
    NUMBER_DEFERRED("Number Deferred", Long.class, QJurorPool.jurorPool.count(), QJurorPool.jurorPool),

    REASONABLE_ADJUSTMENT_CODE("Reasonable Adjustment Code", String.class,
        QJuror.juror.reasonableAdjustmentCode, QJuror.juror),
    REASONABLE_ADJUSTMENT_MESSAGE("Reasonable Adjustment Message", String.class,
        QJuror.juror.reasonableAdjustmentMessage, QJuror.juror),
    REASONABLE_ADJUSTMENT("Reasonable Adjustment", List.class, REASONABLE_ADJUSTMENT_CODE,
        REASONABLE_ADJUSTMENT_MESSAGE),

    ON_CALL("On Call", Boolean.class, QJurorPool.jurorPool.onCall, QJurorPool.jurorPool),
    SERVICE_START_DATE("Service Start Date", LocalDate.class, QPoolRequest.poolRequest.returnDate,
        QPoolRequest.poolRequest),
    POOL_NUMBER("Pool Number", String.class, QPoolRequest.poolRequest.poolNumber, QPoolRequest.poolRequest),
    NEXT_ATTENDANCE_DATE("Next attendance date", LocalDate.class, QJurorPool.jurorPool.nextDate, QJurorPool.jurorPool);


    private final List<EntityPath<?>> requiredTables;
    private final String displayName;
    private final Class<?> dataType;
    private final Expression<?> expression;
    private final IDataType[] returnTypes;

    DataType(String displayName, Class<?> dataType, Expression<?> expression,
             EntityPath<?>... requiredTables) {
        this.displayName = displayName;
        this.dataType = dataType;
        this.expression = expression;
        this.returnTypes = null;
        this.requiredTables = List.of(requiredTables);
    }

    DataType(String displayName, Class<?> dataType, IDataType... dataTypes) {
        this.displayName = displayName;
        this.dataType = dataType;
        this.returnTypes = dataTypes;
        this.expression = null;
        this.requiredTables = null;
    }

    public final String getId() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
