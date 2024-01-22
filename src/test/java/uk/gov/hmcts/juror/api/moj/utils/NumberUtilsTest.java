package uk.gov.hmcts.juror.api.moj.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class NumberUtilsTest {

    @Test
    public void test_unboxIntegerValues_positiveInteger() {
        int expectedUnboxedValue = 5;
        Integer value = expectedUnboxedValue;
        Assertions.assertThat(NumberUtils.unboxIntegerValues(value)).isEqualTo(expectedUnboxedValue);
    }

    @Test
    public void test_unboxIntegerValues_negativeInteger() {
        int expectedUnboxedValue = -5;
        Integer value = expectedUnboxedValue;
        Assertions.assertThat(NumberUtils.unboxIntegerValues(value)).isEqualTo(expectedUnboxedValue);
    }

    @Test
    public void test_unboxIntegerValues_nullValue() {
        int expectedUnboxedValue = 0;
        Integer value = null;
        Assertions.assertThat(NumberUtils.unboxIntegerValues(value)).isEqualTo(expectedUnboxedValue);
    }

}
