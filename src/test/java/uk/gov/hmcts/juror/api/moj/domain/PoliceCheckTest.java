package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("PoliceCheck")
class PoliceCheckTest {


    @Nested
    @DisplayName("public static Character isChecked(PoliceCheck policeCheck)")
    class IsChecked {
        @ParameterizedTest(name = "Expect null when {0} is provided")
        @NullSource
        @EnumSource(value = PoliceCheck.class,
            mode = EnumSource.Mode.INCLUDE,
            names = "NOT_CHECKED")
        void isCheckedNullResponse(PoliceCheck policeCheck) {
            assertNull(PoliceCheck.isChecked(policeCheck),
                "Null should be returned when: '" + policeCheck + "' is provide");
        }

        @ParameterizedTest(name = "Expect 'C' when {0} is provided")
        @EnumSource(value = PoliceCheck.class,
            mode = EnumSource.Mode.INCLUDE,
            names = {"ELIGIBLE", "INELIGIBLE"})
        void isCheckedTrueExpectCResponse(PoliceCheck policeCheck) {
            assertEquals('C', PoliceCheck.isChecked(policeCheck),
                "'C' should be returned when: '" + policeCheck + "' is provide");
        }

        @ParameterizedTest(name = "Expect 'U' when {0} is provided")
        @EnumSource(value = PoliceCheck.class,
            mode = EnumSource.Mode.EXCLUDE,
            names = {"ELIGIBLE", "INELIGIBLE", "NOT_CHECKED"})
        void isCheckedFalseExpectUResponse(PoliceCheck policeCheck) {
            assertEquals('U', PoliceCheck.isChecked(policeCheck),
                "'U' should be returned when: '" + policeCheck + "' is provide");
        }
    }

    @Nested
    @DisplayName("public static String getDescription(PoliceCheck policeCheck)")
    class GetDescription {

        @ParameterizedTest(name = "Expect correct description when {0} is provided")
        @EnumSource(PoliceCheck.class)
        void correctDescription(PoliceCheck policeCheck) {
            assertEquals(policeCheck.getDescription(), PoliceCheck.getDescription(policeCheck),
                "Correct description should be returned when: '" + policeCheck + "' is provide");
        }

        @Test
        @DisplayName("Expect NOT_CHECKED description when null is provided")
        void nullPoliceCheck() {
            assertEquals(PoliceCheck.NOT_CHECKED.getDescription(), PoliceCheck.getDescription(null),
                "Correct description should be returned when: 'null' is provide");
        }
    }

    @Nested
    @DisplayName("public static PoliceCheck getEffectiveValue(PoliceCheck oldValue, PoliceCheck newValue)")
    class GetEffectiveValue {

        public static Stream<Arguments> getEffectiveValueSource() {
            Stream.Builder<Arguments> builder = Stream.builder();

            List<PoliceCheck> policeCheckValues = new ArrayList<>();
            policeCheckValues.add(null);
            policeCheckValues.addAll(List.of(PoliceCheck.values()));

            for (PoliceCheck oldValue : policeCheckValues) {
                for (PoliceCheck newValue : policeCheckValues) {
                    PoliceCheck expectedValue;
                    if (oldValue == null || newValue == null) {
                        expectedValue = newValue;
                    } else if (oldValue.isError() && newValue.isError()) {
                        expectedValue = PoliceCheck.UNCHECKED_MAX_RETRIES_EXCEEDED;
                    } else {
                        expectedValue = newValue;
                    }
                    builder.add(arguments(oldValue, newValue, expectedValue));
                }
            }

            return builder.build();
        }

        @ParameterizedTest(name = "Expect expected date to be {2} when old value is {0} and new value is {1}")
        @DisplayName("getEffectiveValue(oldValue, newValue)")
        @MethodSource("getEffectiveValueSource")
        void getEffectiveValue(PoliceCheck oldValue, PoliceCheck newValue, PoliceCheck expectedValue) {
            assertEquals(expectedValue,
                PoliceCheck.getEffectiveValue(oldValue, newValue),
                "Effected value should return '" + expectedValue
                    + "' when old value is '" + oldValue
                    + "' and new value is '" + newValue + "'");
        }
    }
}
