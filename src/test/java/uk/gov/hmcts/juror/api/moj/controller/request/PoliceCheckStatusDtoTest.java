package uk.gov.hmcts.juror.api.moj.controller.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;

import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class PoliceCheckStatusDtoTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void beforeAll() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    public static void afterAll() {
        validatorFactory.close();
    }

    @ParameterizedTest(name = "Checking status {0} is accepted")
    @EnumSource(PoliceCheck.class)
    void positiveVerifyStatus(PoliceCheck status) {
        PoliceCheckStatusDto policeCheckStatusDto = new PoliceCheckStatusDto(status);
        Set<ConstraintViolation<PoliceCheckStatusDto>> violations = validator.validate(policeCheckStatusDto);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    void negativeNullStatus() {
        PoliceCheckStatusDto policeCheckStatusDto = new PoliceCheckStatusDto(null);
        Set<ConstraintViolation<PoliceCheckStatusDto>> violations = validator.validate(policeCheckStatusDto);
        assertThat(violations).as("1 validation violation expected")
            .hasSize(1)
            .allMatch(
                policeCheckStatusDtoConstraintViolation -> Objects.nonNull(policeCheckStatusDtoConstraintViolation)
                    && policeCheckStatusDtoConstraintViolation.getMessage().equals("must not be null")
                    && policeCheckStatusDtoConstraintViolation.getPropertyPath().toString().equals("status"));

    }
}
