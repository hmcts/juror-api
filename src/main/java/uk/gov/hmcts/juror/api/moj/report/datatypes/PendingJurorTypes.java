package uk.gov.hmcts.juror.api.moj.report.datatypes;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QPendingJuror;
import uk.gov.hmcts.juror.api.moj.report.IDataType;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Getter
@SuppressWarnings({
    "PMD.ArrayIsStoredDirectly",
    "LineLength"
})
public enum PendingJurorTypes implements IDataType {

    JUROR_NUMBER("Juror Number", String.class,
        QPendingJuror.pendingJuror.jurorNumber,
        QPendingJuror.pendingJuror),
    CREATED_ON("Created On", LocalDate.class,
        QPendingJuror.pendingJuror.dateAdded,
        QPendingJuror.pendingJuror),
    CREATED_BY("Created by", String.class,
        QPendingJuror.pendingJuror.addedBy.name,
        QPendingJuror.pendingJuror),
    FIRST_NAME("First name", String.class,
        QPendingJuror.pendingJuror.firstName,
        QPendingJuror.pendingJuror),
    LAST_NAME("Last name", String.class,
        QPendingJuror.pendingJuror.lastName,
        QPendingJuror.pendingJuror),
    ADDRESS_LINE_1("Address Line 1", String.class,
        QPendingJuror.pendingJuror.addressLine1,
        QPendingJuror.pendingJuror),
    ADDRESS_LINE_2("Address Line 2", String.class,
        QPendingJuror.pendingJuror.addressLine2,
        QPendingJuror.pendingJuror),
    ADDRESS_LINE_3("Address Line 3", String.class,
        QPendingJuror.pendingJuror.addressLine3,
        QPendingJuror.pendingJuror),
    ADDRESS_LINE_4("Address Line 4", String.class,
        QPendingJuror.pendingJuror.addressLine4,
        QPendingJuror.pendingJuror),
    ADDRESS_LINE_5("Address Line 5", String.class,
        QPendingJuror.pendingJuror.addressLine5,
        QPendingJuror.pendingJuror),
    POSTCODE("Postcode", String.class,
        QPendingJuror.pendingJuror.postcode,
        QPendingJuror.pendingJuror),
    ADDRESS_COMBINED("Address", List.class,
        ADDRESS_LINE_1, ADDRESS_LINE_2, ADDRESS_LINE_3, ADDRESS_LINE_4, ADDRESS_LINE_5, POSTCODE),
    STATUS("status", String.class,
        QPendingJuror.pendingJuror.status.description,
        QPendingJuror.pendingJuror),
    NOTES("Notes", String.class,
        QPendingJuror.pendingJuror.notes,
        QPendingJuror.pendingJuror),
    POOL_NUMBER("Pool number", String.class,
        QPendingJuror.pendingJuror.poolNumber,
        QPendingJuror.pendingJuror),
    SERVICE_COMPLETED("Service completed", LocalDate.class,
        QJuror.juror.completionDate,
        QJuror.juror),
    ;


    private final List<EntityPath<?>> requiredTables;
    private final String displayName;
    private final Class<?> dataType;
    private final Expression<?> expression;
    private final IDataType[] returnTypes;

    PendingJurorTypes(String displayName, Class<?> dataType, Expression<?> expression,
                      EntityPath<?>... requiredTables) {
        this.displayName = displayName;
        this.dataType = dataType;
        this.expression = expression;
        this.returnTypes = null;
        this.requiredTables = List.of(requiredTables);
    }

    PendingJurorTypes(String displayName, Class<?> dataType, IDataType... dataTypes) {
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
