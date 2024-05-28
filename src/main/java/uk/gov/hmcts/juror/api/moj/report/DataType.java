package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import lombok.Getter;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QBulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Getter
@SuppressWarnings("PMD.ArrayIsStoredDirectly")
public enum DataType implements IDataType {
    JUROR_NUMBER("Juror Number", String.class, QJuror.juror.jurorNumber, QJuror.juror),
    FIRST_NAME("First Name", String.class, QJuror.juror.firstName, QJuror.juror),
    LAST_NAME("Last Name", String.class, QJuror.juror.lastName, QJuror.juror),
    STATUS("Status", String.class, QJurorPool.jurorPool.status.statusDesc, QJurorPool.jurorPool),
    SUMMONED_RESPONDED("Responded", Boolean.class, QJurorPool.jurorPool.status.status
        .eq(IJurorStatus.RESPONDED)),
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
        QReasonableAdjustments.reasonableAdjustments.code, QReasonableAdjustments.reasonableAdjustments),

    REASONABLE_ADJUSTMENT_CODE_WITH_DESCRIPTION("Reasonable Adjustment Code With Description", String.class,
        QReasonableAdjustments.reasonableAdjustments.code
            .concat(" - ")
            .concat(QReasonableAdjustments.reasonableAdjustments.description),
        QReasonableAdjustments.reasonableAdjustments),

    JUROR_REASONABLE_ADJUSTMENT_MESSAGE("Juror Reasonable Adjustment Message", String.class,
        QJuror.juror.reasonableAdjustmentMessage, QJuror.juror),
    JUROR_REASONABLE_ADJUSTMENT_WITH_MESSAGE("Reasonable Adjustments", List.class,
        REASONABLE_ADJUSTMENT_CODE_WITH_DESCRIPTION, JUROR_REASONABLE_ADJUSTMENT_MESSAGE),

    ON_CALL("On Call", Boolean.class, QJurorPool.jurorPool.onCall, QJurorPool.jurorPool),
    SERVICE_START_DATE("Service Start Date", LocalDate.class, QPoolRequest.poolRequest.returnDate,
        QPoolRequest.poolRequest),
    POOL_NUMBER("Pool Number", String.class, QPoolRequest.poolRequest.poolNumber, QPoolRequest.poolRequest),
    POOL_NUMBER_AND_COURT_TYPE("Pool Number and Type",
                               String.class, QPoolRequest.poolRequest.poolNumber.stringValue()
                                   .concat(",").concat(QPoolRequest.poolRequest.poolType.description),
                               QPoolRequest.poolRequest, QPoolRequest.poolRequest),
    POOL_NUMBER_BY_JP("Pool Number", String.class, QJurorPool.jurorPool.pool.poolNumber,
        QJurorPool.jurorPool),
    POOL_NUMBER_BY_APPEARANCE("Pool Number", String.class, QAppearance.appearance.poolNumber,
        QAppearance.appearance),
    NEXT_ATTENDANCE_DATE("Next attendance date", LocalDate.class, QJurorPool.jurorPool.nextDate, QJurorPool.jurorPool),
    LAST_ATTENDANCE_DATE("Last attended on", LocalDate.class, QAppearance.appearance.attendanceDate.max(),
        QAppearance.appearance),
    DOCUMENT_CODE("Document code", String.class, QBulkPrintData.bulkPrintData.formAttribute.formType,
        QBulkPrintData.bulkPrintData),
    TOTAL_SENT_FOR_PRINTING("Sent for printing", Long.class, QBulkPrintData.bulkPrintData.jurorNo.count(),
        QBulkPrintData.bulkPrintData),
    DATE_SENT("Date sent", LocalDate.class, QBulkPrintData.bulkPrintData.creationDate, QBulkPrintData.bulkPrintData),

    SUMMONS_TOTAL("Summoned", Integer.class,
        new CaseBuilder().when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED)).then(1).otherwise(0).sum(),
        QJurorPool.jurorPool),
    RESPONDED_TOTAL("Responded", Integer.class,
        new CaseBuilder().when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED)).then(1).otherwise(0)
            .sum(),
        QJurorPool.jurorPool),
    PANEL_TOTAL("Panel", Integer.class,
        new CaseBuilder().when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.PANEL)).then(1).otherwise(0).sum(),
        QJurorPool.jurorPool),
    JUROR_TOTAL("Juror", Integer.class,
        new CaseBuilder().when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.JUROR)).then(1).otherwise(0).sum(),
        QJurorPool.jurorPool),
    EXCUSED_TOTAL("Excused", Integer.class,
        new CaseBuilder().when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.EXCUSED)).then(1).otherwise(0).sum(),
        QJurorPool.jurorPool),
    DISQUALIFIED_TOTAL("Disqualified", Integer.class,
        new CaseBuilder().when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.DISQUALIFIED)).then(1).otherwise(0)
            .sum(),
        QJurorPool.jurorPool),
    DEFERRED_TOTAL("Deferred", Integer.class,
        new CaseBuilder().when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED)).then(1).otherwise(0).sum(),
        QJurorPool.jurorPool),
    REASSIGNED_TOTAL("Reassigned", Integer.class,
        new CaseBuilder().when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.REASSIGNED)).then(1).otherwise(0)
            .sum(),
        QJurorPool.jurorPool),
    UNDELIVERABLE_TOTAL("Undeliverable", Integer.class,
        new CaseBuilder().when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.UNDELIVERABLE)).then(1).otherwise(0)
            .sum(),
        QJurorPool.jurorPool),
    TRANSFERRED_TOTAL("Transferred", Integer.class,
        new CaseBuilder().when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.TRANSFERRED)).then(1).otherwise(0)
            .sum(),
        QJurorPool.jurorPool),

    ATTENDANCE_DATE("Attendance Date", LocalDate.class, QAppearance.appearance.attendanceDate, QAppearance.appearance),
    ATTENDANCE_TYPE("Attendance Type", String.class, QAppearance.appearance.attendanceType, QAppearance.appearance),

    EXPENSE_STATUS("Expense Status", String.class,
        new CaseBuilder()
            .when(QAppearance.appearance.isDraftExpense.isTrue()).then("Draft")
            .when(QAppearance.appearance.isDraftExpense.isFalse()
                .and(QAppearance.appearance.appearanceStage.eq(AppearanceStage.EXPENSE_ENTERED)))
            .then("For approval")
            .when(QAppearance.appearance.isDraftExpense.isFalse()
                .and(QAppearance.appearance.appearanceStage.eq(AppearanceStage.EXPENSE_EDITED)))
            .then("For re-approval")
            .when(QAppearance.appearance.isDraftExpense.isFalse()
                .and(QAppearance.appearance.appearanceStage.eq(AppearanceStage.EXPENSE_AUTHORISED)))
            .then("Authorised")
            .otherwise(""), QAppearance.appearance),

    AUDIT_NUMBER("Audit number", String.class, QAppearance.appearance.attendanceAuditNumber, QAppearance.appearance),

    APPEARANCE_TRIAL_NUMBER("Trial Number", String.class, QAppearance.appearance.trialNumber, QAppearance.appearance),
    APPEARANCE_POOL_NUMBER("Pool Number", String.class, QAppearance.appearance.poolNumber, QAppearance.appearance),
    APPEARANCE_CHECKED_IN("Checked In", LocalTime.class, QAppearance.appearance.timeIn, QAppearance.appearance),
    APPEARANCE_CHECKED_OUT("Checked Out", LocalTime.class, QAppearance.appearance.timeOut, QAppearance.appearance),
    APPEARANCE_DATE_AND_POOL_TYPE("Appearance Date And Pool Type", String.class,
        QAppearance.appearance.attendanceDate.stringValue()
            .concat(",").concat(QPoolRequest.poolRequest.poolType.description),
        QAppearance.appearance, QPoolRequest.poolRequest),

    DATE_OF_ABSENCE("Date of absence", LocalDate.class, QAppearance.appearance.attendanceDate, QAppearance.appearance),

    PANEL_STATUS("Panel Status", String.class,
        new CaseBuilder()
            .when(QPanel.panel.result.eq(PanelResult.NOT_USED)).then("Not Used")
            .when(QPanel.panel.result.eq(PanelResult.CHALLENGED)).then("Challenged")
            .when(QPanel.panel.result.eq(PanelResult.JUROR)).then("Juror")
            .when(QPanel.panel.result.eq(PanelResult.RETURNED).and(QPanel.panel.empanelledDate.isNotNull()))
                .then("Returned Juror")
            .when(QPanel.panel.result.eq(PanelResult.RETURNED).and(QPanel.panel.empanelledDate.isNull()))
                .then("Returned")
            .otherwise(""),
        QPanel.panel),
    JUROR_NUMBER_FROM_TRIAL("Juror Number", String.class, QPanel.panel.juror.jurorNumber, QPanel.panel),

    COURT_LOCATION_NAME_AND_CODE("Court Location Name And Code", String.class,
                                 QCourtLocation.courtLocation.name.concat(" (")
        .concat(QCourtLocation.courtLocation.locCode).concat(")"), QPoolRequest.poolRequest);

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
