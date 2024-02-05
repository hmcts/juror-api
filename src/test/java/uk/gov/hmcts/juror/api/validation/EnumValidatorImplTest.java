package uk.gov.hmcts.juror.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EnumValidatorImplTest extends AbstractValidatorTest<EnumValidator> {

    private EnumValidatorImpl validator;

    @BeforeEach
    void setUp() {
        validator = new EnumValidatorImpl();
    }

    @Test
    void positiveInitializeTest() {
        EnumValidator enumValidator =
            createAnnotation(Map.of("values", new String[]{"TEST_1", "TEST_2"}),
                EnumValidator.class);
        validator.initialize(enumValidator);
        assertThat(validator.mode).isEqualTo(EnumValidator.Mode.INCLUDE);
        assertThat(validator.values).isEqualTo(List.of("TEST_1", "TEST_2"));
    }

    @Test
    void positiveInitializeWithModeTest() {
        EnumValidator enumValidator =
            createAnnotation(Map.of("values", new String[]{"TEST_1", "TEST_2"},
                    "mode", EnumValidator.Mode.EXCLUDE),
                EnumValidator.class);
        validator.initialize(enumValidator);
        assertThat(validator.mode).isEqualTo(EnumValidator.Mode.EXCLUDE);
        assertThat(validator.values).isEqualTo(List.of("TEST_1", "TEST_2"));
    }

    @Test
    void negativeIsValidInclude() {
        EnumValidator enumValidator =
            createAnnotation(Map.of("values", new String[]{"TEST_1", "TEST_2"},
                    "mode", EnumValidator.Mode.INCLUDE),
                EnumValidator.class);
        validator.initialize(enumValidator);

        assertThat(validator.isValid(TestEnum.TEST_3, null)).isFalse();
    }

    @Test
    void negativeIsValidExclude() {
        EnumValidator enumValidator =
            createAnnotation(Map.of("values", new String[]{"TEST_1", "TEST_2"},
                    "mode", EnumValidator.Mode.EXCLUDE),
                EnumValidator.class);
        validator.initialize(enumValidator);

        assertThat(validator.isValid(TestEnum.TEST_1, null)).isFalse();
        assertThat(validator.isValid(TestEnum.TEST_2, null)).isFalse();

    }

    @Test
    void positiveIsValidInclude() {
        EnumValidator enumValidator =
            createAnnotation(Map.of("values", new String[]{"TEST_1", "TEST_2"},
                    "mode", EnumValidator.Mode.INCLUDE),
                EnumValidator.class);
        validator.initialize(enumValidator);

        assertThat(validator.isValid(TestEnum.TEST_1, null)).isTrue();
        assertThat(validator.isValid(TestEnum.TEST_2, null)).isTrue();

    }

    @Test
    void positiveIsValidExclude() {
        EnumValidator enumValidator =
            createAnnotation(Map.of("values", new String[]{"TEST_1", "TEST_2"},
                    "mode", EnumValidator.Mode.EXCLUDE),
                EnumValidator.class);
        validator.initialize(enumValidator);

        assertThat(validator.isValid(TestEnum.TEST_3, null)).isTrue();
    }


    enum TestEnum {
        TEST_1, TEST_2, TEST_3;
    }

}
