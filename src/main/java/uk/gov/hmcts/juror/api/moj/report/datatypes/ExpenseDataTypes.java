package uk.gov.hmcts.juror.api.moj.report.datatypes;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.report.IDataType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Getter
@SuppressWarnings("PMD.ArrayIsStoredDirectly")
public enum ExpenseDataTypes implements IDataType {

    JUROR_NUMBER("Juror Number", String.class, QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.jurorNumber,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),
    PAYMENT_AUDIT("Payment Audit", String.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.fAudit.prepend("F"),
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),

    ATTENDANCE_DATE("Attendance Date", LocalDate.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.attendanceDate,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),

    TOTAL_LOSS_OF_EARNINGS_PAID("Loss of earnings", BigDecimal.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalFinancialLossPaid,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),
    TOTAL_SUBSISTENCE_PAID("Food and drink", BigDecimal.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalSubsistencePaid,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),
    TOTAL_SMARTCARD_PAID("Smartcard", BigDecimal.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalSmartCardPaid,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),
    TOTAL_TRAVEL_PAID("Travel", BigDecimal.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalTravelPaid,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),
    TOTAL_PAID("Total", BigDecimal.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalPaid,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),

    FIRST_NAME("First name", String.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.firstName,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),
    LAST_NAME("Last name", String.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.lastName,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),

    IS_CASH("Is Cash", Boolean.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.payCash,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),


    TOTAL_LOSS_OF_EARNINGS_PAID_SUM("Loss of earnings", BigDecimal.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalFinancialLossPaid.sum(),
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),

    TOTAL_LOSS_OF_EARNINGS_PAID_COUNT("Loss of earnings Count", Long.class,
        new CaseBuilder().when(
                QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalFinancialLossPaid.eq(BigDecimal.ZERO))
            .then(0).otherwise(1).sum(),
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),


    TOTAL_SUBSISTENCE_PAID_SUM("Food and drink", BigDecimal.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalSubsistencePaid.sum(),
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),

    TOTAL_SUBSISTENCE_PAID_COUNT("Food and drink Count", Long.class,
        new CaseBuilder().when(
                QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalSubsistencePaid.eq(BigDecimal.ZERO))
            .then(0).otherwise(1).sum(),
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),

    TOTAL_SMARTCARD_PAID_SUM("Smartcard", BigDecimal.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalSmartCardPaid.sum(),
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),

    TOTAL_SMARTCARD_PAID_COUNT("Smartcard Count", Long.class,
        new CaseBuilder().when(
                QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalSmartCardPaid.eq(BigDecimal.ZERO)).then(0)
            .otherwise(1).sum(),
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),


    TOTAL_TRAVEL_PAID_SUM("Travel", BigDecimal.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalTravelPaid.sum(),
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),

    TOTAL_TRAVEL_PAID_COUNT("Travel Count", Long.class,
        new CaseBuilder().when(
                QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalTravelPaid.eq(BigDecimal.ZERO)).then(0)
            .otherwise(1).sum(),
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),


    TOTAL_PAID_SUM("Total", BigDecimal.class,
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.totalPaid.sum(),
        QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails),

    ;


    private final List<EntityPath<?>> requiredTables;
    private final String displayName;
    private final Class<?> dataType;
    private final Expression<?> expression;
    private final IDataType[] returnTypes;

    ExpenseDataTypes(String displayName, Class<?> dataType, Expression<?> expression,
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
