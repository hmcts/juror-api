package uk.gov.hmcts.juror.api.moj.report.datatypes;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;

import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.QReportsJurorPayments;
import uk.gov.hmcts.juror.api.moj.report.IDataType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;


@Getter
@SuppressWarnings({
    "PMD.ArrayIsStoredDirectly",
    "LineLength"
})
public enum ReportsJurorPaymentsDataTypes implements IDataType {
    TRIAL_NUMBER("Trial Number", String.class,
        QReportsJurorPayments.reportsJurorPayments.trialNumber,
        QReportsJurorPayments.reportsJurorPayments),

    ATTENDANCE_DATE("Attendance Date", LocalDate.class,
        QReportsJurorPayments.reportsJurorPayments.attendanceDate,
        QReportsJurorPayments.reportsJurorPayments),

    JUROR_NUMBER("Juror Number", String.class,
        QReportsJurorPayments.reportsJurorPayments.jurorNumber,
        QReportsJurorPayments.reportsJurorPayments),

    FIRST_NAME("First Name", String.class,
        QReportsJurorPayments.reportsJurorPayments.firstName,
        QReportsJurorPayments.reportsJurorPayments),

    LAST_NAME("Last Name", String.class,
        QReportsJurorPayments.reportsJurorPayments.lastName,
        QReportsJurorPayments.reportsJurorPayments),

    POOL_NUMBER("Pool Number", String.class,
        QReportsJurorPayments.reportsJurorPayments.poolNumber,
        QReportsJurorPayments.reportsJurorPayments),

    PAYMENT_AUDIT("Payment Audit", String.class,
        QReportsJurorPayments.reportsJurorPayments.latestPaymentFAuditId,
        QReportsJurorPayments.reportsJurorPayments),

    CHECKED_IN("Checked In", LocalTime.class,
        QReportsJurorPayments.reportsJurorPayments.checkedIn,
        QReportsJurorPayments.reportsJurorPayments),

    CHECKED_OUT("Checked Out", LocalTime.class,
        QReportsJurorPayments.reportsJurorPayments.checkedOut,
        QReportsJurorPayments.reportsJurorPayments),

    HOURS_ATTENDED("Hours Attended", LocalTime.class,
        QReportsJurorPayments.reportsJurorPayments.hoursAttended,
        QReportsJurorPayments.reportsJurorPayments),

    ATTENDANCE_AUDIT("Attendance Audit", String.class,
        QReportsJurorPayments.reportsJurorPayments.attendanceAudit,
        QReportsJurorPayments.reportsJurorPayments),

    PAYMENT_DATE("Payment Date", LocalDateTime.class,
        QReportsJurorPayments.reportsJurorPayments.paymentDate,
        QReportsJurorPayments.reportsJurorPayments),

    TOTAL_TRAVEL_DUE("Travel Due", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.totalTravelDue,
        QReportsJurorPayments.reportsJurorPayments),

    TOTAL_FINANCIAL_LOSS_DUE("Financial Loss Due", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.totalFinancialLossDue,
        QReportsJurorPayments.reportsJurorPayments),

    SUBSISTENCE_DUE("Food & Drink Due", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.subsistenceDue,
        QReportsJurorPayments.reportsJurorPayments),

    SMART_CARD_DUE("Smart Card Due", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.smartCardDue,
        QReportsJurorPayments.reportsJurorPayments),

    TOTAL_DUE("Total Due", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.totalDue,
        QReportsJurorPayments.reportsJurorPayments),

    TOTAL_PAID("Total Paid", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.totalPaid,
        QReportsJurorPayments.reportsJurorPayments),
    ;


    private final List<EntityPath<?>> requiredTables;
    private final String displayName;
    private final Class<?> dataType;
    private final Expression<?> expression;
    private final IDataType[] returnTypes;

    ReportsJurorPaymentsDataTypes(String displayName, Class<?> dataType, Expression<?> expression,
                                  EntityPath<?>... requiredTables) {
        this.displayName = displayName;
        this.dataType = dataType;
        this.expression = expression;
        this.returnTypes = null;
        this.requiredTables = List.of(requiredTables);
    }

    public final String getId() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
