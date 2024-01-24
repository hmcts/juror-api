package uk.gov.hmcts.juror.api.moj.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtils {

    private BigDecimalUtils() {
        // private constructor to prevent instantiation of static util class
    }

    public static BigDecimal getOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
