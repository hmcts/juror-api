package uk.gov.hmcts.juror.api.moj.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberUtils {


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
}
