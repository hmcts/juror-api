package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QBulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;

import java.math.BigDecimal;
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
    JUROR_POOL_COUNT("Count", Long.class, QJurorPool.jurorPool.count(), QJurorPool.jurorPool),
    SUMMONED_RESPONDED("Responded", Boolean.class, QJurorPool.jurorPool.status.status
        .eq(IJurorStatus.RESPONDED)),

    DEFERRALS("Deferrals", String.class, QJuror.juror.noDefPos, QJuror.juror),
    ABSENCES("Absences", Long.class,
        QAppearance.appearance.attendanceType.eq(AttendanceType.ABSENT).count()),

    EXCUSAL_DISQUAL_CODE("Reason for excusal or disqualification", String.class,
        new CaseBuilder()
            .when(QJuror.juror.disqualifyCode.isNotNull())
            .then(QJuror.juror.disqualifyCode)
            .when(QJuror.juror.excusalCode.isNotNull())
            .then(QJuror.juror.excusalCode)
            .otherwise((String) null),
        QJuror.juror),
    EXCUSAL_DISQUAL_DECISION_DATE("Decision date", LocalDate.class,
        new CaseBuilder()
            .when(QJuror.juror.disqualifyDate.isNotNull())
            .then(QJuror.juror.disqualifyDate)
            .when(QJuror.juror.excusalDate.isNotNull())
            .then(QJuror.juror.excusalDate)
            .otherwise((LocalDate) null),
        QJuror.juror),
    EXCUSAL_DISQUAL_TYPE("Excused or disqualified", String.class,
        new CaseBuilder()
            .when(QJuror.juror.disqualifyCode.isNotNull())
            .then("Disqualified")
            .when(QJuror.juror.excusalCode.isNotNull())
            .then("Excused")
            .otherwise("N/A"),
        QJuror.juror),


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

    COMPLETION_DATE("Completion date", LocalDate.class, QJuror.juror.completionDate),

    POOL_NUMBER("Pool Number", String.class, QPoolRequest.poolRequest.poolNumber, QPoolRequest.poolRequest),
    POOL_NUMBER_JP("Pool Number", String.class, QJurorPool.jurorPool.pool.poolNumber, QJurorPool.jurorPool),
    POOL_NUMBER_AND_COURT_TYPE("Pool Number and Type",
        String.class, QPoolRequest.poolRequest.poolNumber.stringValue()
        .concat(",").concat(QPoolRequest.poolRequest.poolType.description),
        QPoolRequest.poolRequest, QPoolRequest.poolRequest),
    POOL_NUMBER_BY_JP("Pool Number", String.class, QJurorPool.jurorPool.pool.poolNumber,
        QJurorPool.jurorPool),
    POOL_RETURN_DATE_BY_JP("Pool Number", String.class, QJurorPool.jurorPool.pool.returnDate,
        QJurorPool.jurorPool),
    POOL_NUMBER_BY_APPEARANCE("Pool Number", String.class, QAppearance.appearance.poolNumber,
        QAppearance.appearance),
    IS_ACTIVE("Active", Boolean.class,
        QJurorPool.jurorPool.isActive, QJurorPool.jurorPool),
    NEXT_ATTENDANCE_DATE("Next attendance date", LocalDate.class, QJurorPool.jurorPool.nextDate, QJurorPool.jurorPool),

    OPTIC_REFERENCE("Optic Reference", String.class, QJuror.juror.opticRef, QJuror.juror),
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
    FAILED_TO_ATTEND_TOTAL("FTA", Integer.class,
        new CaseBuilder()
            .when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.FAILED_TO_ATTEND)).then(1).otherwise(0)
            .sum(),
        QJurorPool.jurorPool),

    JURORS_SUMMONED_TOTAL("Summoned", Long.class, QJurorPool.jurorPool.count(), QJurorPool.jurorPool),
    ATTENDED_TOTAL("Attended", Integer.class, new CaseBuilder()
        .when(QJurorPool.jurorPool.appearances.size().gt(0)).then(1).otherwise(0).sum(), QJurorPool.jurorPool),

    // PERCENTAGES
    RESPONDED_TOTAL_PERCENTAGE("Responded Total Percentage", Double.class),
    ATTENDED_TOTAL_PERCENTAGE("Attended Total Percentage", Double.class),
    PANEL_TOTAL_PERCENTAGE("Panel Total Percentage", Double.class),
    JUROR_TOTAL_PERCENTAGE("Juror Total Percentage", Double.class),
    EXCUSED_TOTAL_PERCENTAGE("Excused Total Percentage", Double.class),
    DISQUALIFIED_TOTAL_PERCENTAGE("Disqualified Total Percentage", Double.class),
    DEFERRED_TOTAL_PERCENTAGE("Deferred Total Percentage", Double.class),
    REASSIGNED_TOTAL_PERCENTAGE("Reassigned Total Percentage", Double.class),
    UNDELIVERABLE_TOTAL_PERCENTAGE("Undeliverable Total Percentage", Double.class),
    TRANSFERRED_TOTAL_PERCENTAGE("Transferred Total Percentage", Double.class),
    FAILED_TO_ATTEND_TOTAL_PERCENTAGE("Failed To Attend Total Percentage", Double.class),

    ATTENDANCE_DATE("Attendance Date", LocalDate.class, QAppearance.appearance.attendanceDate, QAppearance.appearance),
    ATTENDANCE_TYPE("Attendance Type", String.class, QAppearance.appearance.attendanceType, QAppearance.appearance),
    DAY("Day", String.class),
    TOTAL_PAID("Paid", BigDecimal.class, QAppearance.appearance.totalPaid, QAppearance.appearance),

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
        QPoolRequest.poolRequest.courtLocation.name.concat(" (")
            .concat(QPoolRequest.poolRequest.courtLocation.locCode).concat(")"), QPoolRequest.poolRequest),
    COURT_LOCATION_NAME_AND_CODE_JP("Court", String.class,
        QJurorPool.jurorPool.pool.courtLocation.name.concat(" (")
            .concat(QJurorPool.jurorPool.pool.courtLocation.locCode).concat(")"), QJurorPool.jurorPool),

    TRIAL_JUDGE_NAME("Judge", String.class, QTrial.trial.judge.name, QTrial.trial),
    TRIAL_TYPE("Trial Type", String.class, QTrial.trial.trialType, QTrial.trial),
    TRIAL_NUMBER("Trial number", String.class, QTrial.trial.trialNumber, QTrial.trial),
    TRIAL_COURT_LOCATION("Trial court location", String.class, QTrial.trial.courtLocation, QTrial.trial),

    TRIAL_PANELLED_COUNT("Panelled", Long.class, QTrial.trial.panel.size(), QTrial.trial),
    TRIAL_JURORS_COUNT("Jurors", Long.class, QTrial.trial.jurors.size(), QTrial.trial),
    TRIAL_JURORS_NOT_USED("Not used", Long.class, QTrial.trial.notUsedPanel.size(), QTrial.trial),
    TRIAL_START_DATE("Trial start date", LocalDate.class, QTrial.trial.trialStartDate, QTrial.trial),
    TRIAL_END_DATE("Trial end date", LocalDate.class, QTrial.trial.trialEndDate, QTrial.trial),
    ATTENDANCE_COUNT("Attendance count", Long.class, QAppearance.appearance.count(), QAppearance.appearance),


    POLICE_CHECK_RESPONDED("Responded jurors", Long.class,
        QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED).count()),

    POLICE_CHECK_SUBMITTED("Checks submitted", Long.class,
        new CaseBuilder()
            .when(QJuror.juror.policeCheck.notIn(PoliceCheck.NOT_CHECKED, PoliceCheck.INSUFFICIENT_INFORMATION))
            .then(1L)
            .otherwise(0L).sum()),

    POLICE_CHECK_COMPLETE("Checks completed", Long.class,
        new CaseBuilder()
            .when(QJuror.juror.policeCheck.in(PoliceCheck.ELIGIBLE, PoliceCheck.INELIGIBLE,
                PoliceCheck.UNCHECKED_MAX_RETRIES_EXCEEDED))
            .then(1L)
            .otherwise(0L).sum()),


    POLICE_CHECK_TIMED_OUT("Checks timed out", Long.class,
        new CaseBuilder()
            .when(QJuror.juror.policeCheck.in(PoliceCheck.UNCHECKED_MAX_RETRIES_EXCEEDED))
            .then(1L)
            .otherwise(0L).sum()),

    POLICE_CHECK_DISQUALIFIED("Jurors disqualified", Long.class,
        new CaseBuilder()
            .when(QJuror.juror.policeCheck.in(PoliceCheck.INELIGIBLE))
            .then(1L)
            .otherwise(0L).sum()),

    TOTAL_REQUESTED("Requested", Long.class,
        QJurorPool.jurorPool.pool.numberRequested
    ),
    TOTAL_DEFERRED("Deferred", Long.class,
        new CaseBuilder()
            .when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED))
            .then(1L)
            .otherwise(0L).sum(),
        QJurorPool.jurorPool
    ),
    TOTAL_SUMMONED("Summoned", Long.class,
        QJurorPool.jurorPool.count().subtract((NumberExpression<Long>) TOTAL_DEFERRED.getExpression()),
        QJurorPool.jurorPool
    ),
    TOTAL_SUPPLIED("Supplied", Long.class,
        new CaseBuilder()
            .when(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED))
            .then(1L)
            .otherwise(0L).sum(),
        QJurorPool.jurorPool
    ),

    //Due to new the updated system we no longer disqualify people on selection instead we simply do not select them
    DISQUALIFIED_ON_SELECTION("Disqualified on selection", String.class,
        Expressions.nullExpression(), QJuror.juror),
    ;



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
