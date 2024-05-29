package uk.gov.hmcts.juror.api.moj.report.datatypes;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.QPaymentData;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Getter
@SuppressWarnings({
    "PMD.ArrayIsStoredDirectly",
    "LineLength"
})
public enum PaymentStatusDataTypes implements IDataType {
    EXTRACTED("Extracted", Boolean.class,
        QPaymentData.paymentData.extracted, QPaymentData.paymentData),

    CREATION_DATE("Date Approved", LocalDate.class,
        DataUtils.asDate(QPaymentData.paymentData.creationDateTime),
        QPaymentData.paymentData),


    TOTAL_AMOUNT("Amount", BigDecimal.class,
        QPaymentData.paymentData.expenseTotal.sum(), QPaymentData.paymentData),

    PAYMENTS("Payments", Long.class,
        QPaymentData.paymentData.count(), QPaymentData.paymentData),


    CONSOLIDATED_FILE_REFERENCE("Consolidated file reference", Long.class,
        QPaymentData.paymentData.expenseFileName, QPaymentData.paymentData),


    ;


    private final List<EntityPath<?>> requiredTables;
    private final String displayName;
    private final Class<?> dataType;
    private final Expression<?> expression;
    private final IDataType[] returnTypes;

    PaymentStatusDataTypes(String displayName, Class<?> dataType, Expression<?> expression,
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
