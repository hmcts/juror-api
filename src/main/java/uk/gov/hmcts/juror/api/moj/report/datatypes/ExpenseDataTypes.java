package uk.gov.hmcts.juror.api.moj.report.datatypes;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetailsIncludingApprovedAmounts;
import uk.gov.hmcts.juror.api.moj.report.IDataType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Getter
@SuppressWarnings({
    "PMD.ArrayIsStoredDirectly",
    "LineLength"
})
public enum ExpenseDataTypes implements IDataType {

    JUROR_NUMBER("Juror Number", String.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.jurorNumber,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

    PAYMENT_AUDIT_RAW("Payment Audit", String.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.financialAudit,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

    PAYMENT_AUDIT("Payment Audit", String.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.financialAudit.prepend(
            "F"),
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

    ATTENDANCE_DATE("Attendance Date", LocalDate.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.attendanceDate,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),


    CREATED_ON_DATETIME("Created On", LocalDate.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.createdOn,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

    CREATED_ON_DATE("Created On", LocalDate.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.createdOnDate,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),


    TOTAL_LOSS_OF_EARNINGS_APPROVED("Loss of earnings", BigDecimal.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalFinancialLossApproved,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),
    TOTAL_SUBSISTENCE_APPROVED("Food and drink", BigDecimal.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalSubsistenceApproved,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),
    TOTAL_SMARTCARD_APPROVED("Smartcard", BigDecimal.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalSmartCardApproved,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),
    TOTAL_TRAVEL_APPROVED("Travel", BigDecimal.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalTravelApproved,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),
    TOTAL_APPROVED("Total", BigDecimal.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalApproved,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

    IS_CASH("Is Cash", Boolean.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.payCash,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),


    TOTAL_LOSS_OF_EARNINGS_APPROVED_SUM("Loss of earnings", BigDecimal.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalFinancialLossApproved.sum(),
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

    TOTAL_LOSS_OF_EARNINGS_APPROVED_COUNT("Loss of earnings Count", Long.class,
        new CaseBuilder().when(
                QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalFinancialLossApproved.eq(
                    BigDecimal.ZERO))
            .then(0).otherwise(1).sum(),
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),


    TOTAL_SUBSISTENCE_APPROVED_SUM("Food and drink", BigDecimal.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalSubsistenceApproved.sum(),
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

    TOTAL_SUBSISTENCE_APPROVED_COUNT("Food and drink Count", Long.class,
        new CaseBuilder().when(
                QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalSubsistenceApproved.eq(
                    BigDecimal.ZERO))
            .then(0).otherwise(1).sum(),
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

    TOTAL_SMARTCARD_APPROVED_SUM("Smartcard", BigDecimal.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalSmartCardApproved.sum(),
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

    TOTAL_SMARTCARD_APPROVED_COUNT("Smartcard Count", Long.class,
        new CaseBuilder().when(
                QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalSmartCardApproved.eq(
                    BigDecimal.ZERO)).then(0)
            .otherwise(1).sum(),
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),


    TOTAL_TRAVEL_APPROVED_SUM("Travel", BigDecimal.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalTravelApproved.sum(),
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

    TOTAL_TRAVEL_APPROVED_COUNT("Travel Count", Long.class,
        new CaseBuilder().when(
                QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalTravelApproved.eq(
                    BigDecimal.ZERO)).then(0)
            .otherwise(1).sum(),
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),


    TOTAL_APPROVED_SUM("Total", BigDecimal.class,
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.totalApproved.sum(),
        QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts),

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
