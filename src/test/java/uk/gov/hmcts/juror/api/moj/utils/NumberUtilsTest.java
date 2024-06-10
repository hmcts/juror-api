package uk.gov.hmcts.juror.api.moj.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class NumberUtilsTest {

    @Test
    public void test_unboxIntegerValues_positiveInteger() {
        int expectedUnboxedValue = 5;
        Integer value = expectedUnboxedValue;
        assertThat(NumberUtils.unboxIntegerValues(value)).isEqualTo(expectedUnboxedValue);
    }

    @Test
    public void test_unboxIntegerValues_negativeInteger() {
        int expectedUnboxedValue = -5;
        Integer value = expectedUnboxedValue;
        assertThat(NumberUtils.unboxIntegerValues(value)).isEqualTo(expectedUnboxedValue);
    }

    @Test
    public void test_unboxIntegerValues_nullValue() {
        int expectedUnboxedValue = 0;
        Integer value = null;
        assertThat(NumberUtils.unboxIntegerValues(value)).isEqualTo(expectedUnboxedValue);
    }

    @Test
    public void calculatePercentage_positiveValues() {
        double actual = 5;
        double total = 10;
        double expectedPercentage = 50.0;
        assertThat(NumberUtils.calculatePercentage(actual, total)).isEqualTo(expectedPercentage);
    }

    @Test
    public void calculatePercentage_positiveRounded() {
        double actual = 1;
        double total = 6;
        double expectedPercentage = 16.666_67;
        assertThat(NumberUtils.calculatePercentage(actual, total)).isEqualTo(expectedPercentage);
    }

    @Test
    public void calculatePercentage_zeroDenominator() {
        double actual = 10;
        double total = 0;
        double expectedPercentage = 0.0;
        assertThat(NumberUtils.calculatePercentage(actual, total)).isEqualTo(expectedPercentage);
    }

}
