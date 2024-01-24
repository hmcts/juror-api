package uk.gov.hmcts.juror.api.moj.utils.converters;

import uk.gov.hmcts.juror.api.moj.exception.MojException;

public final class ConversionUtils {
    private ConversionUtils() {
        // private constructor
    }

    public static String toProperCase(String stringToConvert) {
        if (stringToConvert.isBlank()) {
            throw new MojException.InternalServerError("Cannot convert an empty string: "
                + stringToConvert, null);
        }
        return stringToConvert.substring(0, 1).toUpperCase() + stringToConvert.substring(1).toLowerCase();
    }
}
