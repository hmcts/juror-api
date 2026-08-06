package uk.gov.hmcts.juror.api.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class BigDecimalUtilsTest {


    abstract class ComparisonTest {
        private final List<DynamicTest> tests;

        protected ComparisonTest(String symbol, boolean passLessThan, boolean passEqualTo, boolean passGreaterThan) {
            this.tests = new ArrayList<>();
            BigDecimal value1 = new BigDecimal("1.00");
            BigDecimal value2 = new BigDecimal("2.00");

            tests.add(
                DynamicTest.dynamicTest(value1 + " " + symbol + " " + value2 + " = " + passLessThan,
                                        () -> assertThat(compare(value1, value2)).isEqualTo(passLessThan)));

            tests.add(
                DynamicTest.dynamicTest(value2 + " " + symbol + " " + value1 + " = " + passGreaterThan,
                                        () -> assertThat(compare(value2, value1)).isEqualTo(passGreaterThan)));

            tests.add(
                DynamicTest.dynamicTest(value1 + " " + symbol + " " + value1 + " = " + passEqualTo,
                                        () -> assertThat(compare(value1, value1)).isEqualTo(passEqualTo)));
        }

        abstract boolean compare(BigDecimal value1, BigDecimal value2);

        @TestFactory
        @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
        Stream<DynamicTest> comparisonTests() {
            return tests.stream();
        }
    }


    @Nested
    @DisplayName("isGreaterThan")
    class IsGreaterThan extends ComparisonTest {

        protected IsGreaterThan() {
            super("greater than", false, false, true);
        }

        @Override
        boolean compare(BigDecimal value1, BigDecimal value2) {
            return BigDecimalUtils.isGreaterThan(value1, value2);
        }
    }

    @Nested
    @DisplayName("isLessThan")
    class IsLessThan extends ComparisonTest {

        protected IsLessThan() {
            super("less than", true, false, false);
        }

        @Override
        boolean compare(BigDecimal value1, BigDecimal value2) {
            return BigDecimalUtils.isLessThan(value1, value2);
        }
    }

    @Nested
    @DisplayName("isEqualTo")
    class IsEqualTo extends ComparisonTest {

        protected IsEqualTo() {
            super("equal to", false, true, false);
        }

        @Override
        boolean compare(BigDecimal value1, BigDecimal value2) {
            return BigDecimalUtils.isEqualTo(value1, value2);
        }
    }

    @Nested
    @DisplayName("isGreaterThanOrEqualTo")
    class IsGreaterThanOrEqualTo extends ComparisonTest {

        protected IsGreaterThanOrEqualTo() {
            super("greater than or equal to", false, true, true);
        }

        @Override
        boolean compare(BigDecimal value1, BigDecimal value2) {
            return BigDecimalUtils.isGreaterThanOrEqualTo(value1, value2);
        }
    }

    @Nested
    @DisplayName("isLessThanOrEqualTo")
    class IsLessThanOrEqualTo extends ComparisonTest {

        protected IsLessThanOrEqualTo() {
            super("less that or equal to", true, true, false);
        }

        @Override
        boolean compare(BigDecimal value1, BigDecimal value2) {
            return BigDecimalUtils.isLessThanOrEqualTo(value1, value2);
        }
    }

    @Test
    void currencyFormatFor103() {
        assertThat(BigDecimalUtils.currencyFormat(new BigDecimal("1.03")))
            .isEqualTo("£1.03");
    }

    @Test
    void currencyFormatForOne() {
        assertThat(BigDecimalUtils.currencyFormat(BigDecimal.ONE))
            .isEqualTo("£1.00");
    }

    @Test
    void currencyFormatFor10233() {
        assertThat(BigDecimalUtils.currencyFormat(new BigDecimal("1.0233")))
            .isEqualTo("£1.02");
    }
}
