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
        int numerator = 5;
        int denominator = 10;
        int expectedPercentage = 50;
        assertThat(NumberUtils.calculatePercentage(numerator, denominator)).isEqualTo(expectedPercentage);
    }

    @Test
    public void calculatePercentage_zeroDenominator() {
        int numerator = 10;
        int denominator = 0;
        int expectedPercentage = 0;
        assertThat(NumberUtils.calculatePercentage(numerator, denominator)).isEqualTo(expectedPercentage);
    }

}
