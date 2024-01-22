package uk.gov.hmcts.juror.api.moj.utils.converters;

import jakarta.persistence.Converter;
import org.hibernate.type.CharBooleanConverter;

/**
 * This is a temporary converter that supports Y & 1 for true and N & 0 for false
 * This will be removed once new database structure is added and consuming annotations will be replaced with the
 * relevant converter.
 */
@Converter
public class YesNo10Converter extends CharBooleanConverter {
    /**
     * Singleton access.
     */
    public static final YesNo10Converter INSTANCE = new YesNo10Converter();
    private static final String[] VALUES = {"N", "Y", "0", "1"};

    @Override
    protected String[] getValues() {
        return VALUES;
    }

    @Override
    public Boolean toDomainValue(Character relationalForm) {
        if (relationalForm == null) {
            return null;
        }
        return switch (relationalForm) {
            case 'Y', '1' -> true;
            case 'N', '0' -> false;
            default -> null;
        };
    }

    @Override
    public Character toRelationalValue(Boolean domainForm) {
        if (domainForm == null) {
            return null;
        }
        return domainForm ? 'Y' : 'N';
    }
}
