package uk.gov.hmcts.juror.api.moj.utils;

import uk.gov.hmcts.juror.api.config.Settings;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Optional;

public final class BigDecimalUtils {

    private BigDecimalUtils() {
        // private constructor to prevent instantiation of static util class
    }

    public static BigDecimal getOrZero(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO);
    }

    public static boolean isGreaterThan(BigDecimal value1, BigDecimal value2) {
        return compare(value1, value2) == 1;
    }

    public static boolean isLessThan(BigDecimal value1, BigDecimal value2) {
        return compare(value1, value2) == -1;
    }

    public static boolean isEqualTo(BigDecimal value1, BigDecimal value2) {
        return compare(value1, value2) == 0;
    }

    public static boolean isGreaterThanOrEqualTo(BigDecimal value1, BigDecimal value2) {
        return compare(value1, value2) >= 0;
    }

    public static boolean isLessThanOrEqualTo(BigDecimal value1, BigDecimal value2) {
        return compare(value1, value2) <= 0;
    }


    /**
     * Compares two big decimals.
     *
     * @return 1 if a is greater than b
     *      -1 if a is less than b
     *      0 if a is equal to b
     */
    private static int compare(BigDecimal value1, BigDecimal value2) {
        return value1.compareTo(value2);
    }

    public static String currencyFormat(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Settings.LOCALE).format(value);
    }

    public static BigDecimal round(BigDecimal value, int precision) {
        if (value == null) {
            return null;
        }
        return value.setScale(precision, RoundingMode.HALF_UP);
    }
}
