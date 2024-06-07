package uk.gov.hmcts.juror.api.moj.utils;

public class NumberUtils {

    private NumberUtils() {

    }

    /**
     * Safely unbox an Integer object to a primitive int data type, defaulting to 0 where the Integer value is null.
     *
     * @param integer Integer wrapper class containing the value to "unbox"
     * @return the int value of the wrapped Integer, defaulting to 0 where the Integer is null
     */
    public static int unboxIntegerValues(Integer integer) {
        return integer == null
            ?
            0
            :
                integer;
    }

    public static double calculatePercentage(double actual, double total) {
        return total == 0 ? 0 : actual * 100 / total;
    }
}
