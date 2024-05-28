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
    TRIAL_NUMBER("Trial number", String.class,
        QReportsJurorPayments.reportsJurorPayments.trialNumber,
        QReportsJurorPayments.reportsJurorPayments),

    ATTENDANCE_DATE("Attendance date", LocalDate.class,
        QReportsJurorPayments.reportsJurorPayments.attendanceDate,
        QReportsJurorPayments.reportsJurorPayments),

    JUROR_NUMBER("Juror number", String.class,
        QReportsJurorPayments.reportsJurorPayments.jurorNumber,
        QReportsJurorPayments.reportsJurorPayments),

    FIRST_NAME("First name", String.class,
        QReportsJurorPayments.reportsJurorPayments.firstName,
        QReportsJurorPayments.reportsJurorPayments),

    LAST_NAME("Last name", String.class,
        QReportsJurorPayments.reportsJurorPayments.lastName,
        QReportsJurorPayments.reportsJurorPayments),

    POOL_NUMBER("Pool number", String.class,
        QReportsJurorPayments.reportsJurorPayments.poolNumber,
        QReportsJurorPayments.reportsJurorPayments),

    PAYMENT_AUDIT("Payment audit", String.class,
        QReportsJurorPayments.reportsJurorPayments.latestPaymentFAuditId.prepend("F"),
        QReportsJurorPayments.reportsJurorPayments),

    CHECKED_IN("Checked in", LocalTime.class,
        QReportsJurorPayments.reportsJurorPayments.checkedIn,
        QReportsJurorPayments.reportsJurorPayments),

    CHECKED_OUT("Checked out", LocalTime.class,
        QReportsJurorPayments.reportsJurorPayments.checkedOut,
        QReportsJurorPayments.reportsJurorPayments),

    HOURS_ATTENDED("Hours attended", LocalTime.class,
        QReportsJurorPayments.reportsJurorPayments.hoursAttended,
        QReportsJurorPayments.reportsJurorPayments),

    ATTENDANCE_AUDIT("Attendance audit", String.class,
        QReportsJurorPayments.reportsJurorPayments.attendanceAudit,
        QReportsJurorPayments.reportsJurorPayments),

    PAYMENT_DATE("Payment date", LocalDateTime.class,
        QReportsJurorPayments.reportsJurorPayments.paymentDate,
        QReportsJurorPayments.reportsJurorPayments),

    TOTAL_TRAVEL_DUE("Travel due", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.totalTravelDue,
        QReportsJurorPayments.reportsJurorPayments),

    TOTAL_FINANCIAL_LOSS_DUE("Financial loss due", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.totalFinancialLossDue,
        QReportsJurorPayments.reportsJurorPayments),

    SUBSISTENCE_DUE("Food & drink due", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.subsistenceDue,
        QReportsJurorPayments.reportsJurorPayments),

    SMART_CARD_DUE("Smart card due", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.smartCardDue,
        QReportsJurorPayments.reportsJurorPayments),

    TOTAL_DUE("Total due", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.totalDue,
        QReportsJurorPayments.reportsJurorPayments),

    TOTAL_PAID("Paid", BigDecimal.class,
        QReportsJurorPayments.reportsJurorPayments.totalPaid,
        QReportsJurorPayments.reportsJurorPayments),

    FINANCIAL_LOSS_DUE_SUM("Financial loss", BigDecimal.class,
                       QReportsJurorPayments.reportsJurorPayments.totalFinancialLossDue.sum(),
                       QReportsJurorPayments.reportsJurorPayments),
    TRAVEL_DUE_SUM("Travel", BigDecimal.class,
                   QReportsJurorPayments.reportsJurorPayments.totalTravelDue.sum(),
                   QReportsJurorPayments.reportsJurorPayments),
    SUBSISTENCE_DUE_SUM("Food and drink", BigDecimal.class,
                        QReportsJurorPayments.reportsJurorPayments.subsistenceDue.sum(),
                        QReportsJurorPayments.reportsJurorPayments),
    SMARTCARD_DUE_SUM("Smartcard", BigDecimal.class,
                      QReportsJurorPayments.reportsJurorPayments.smartCardDue.sum(),
                      QReportsJurorPayments.reportsJurorPayments),
    TOTAL_DUE_SUM("Total due", BigDecimal.class,
                  QReportsJurorPayments.reportsJurorPayments.totalDue.sum(),
                  QReportsJurorPayments.reportsJurorPayments),
    TOTAL_PAID_SUM("Paid", BigDecimal.class,
                   QReportsJurorPayments.reportsJurorPayments.totalPaid.sum(),
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
