package uk.gov.hmcts.juror.api.moj.domain.authentication;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

import java.math.BigDecimal;

public class UpdateUserDtoTest extends AbstractValidatorTest<UpdateUserDto> {


    @Override
    protected UpdateUserDto createValidObject() {
        return UpdateUserDto.builder()
            .isActive(true)
            .email(TestConstants.VALID_EMAIL)
            .name("name123")
            .approvalLimit(new BigDecimal("123"))
            .build();
    }


    @Nested
    class IsActiveTest extends AbstractValidationFieldTestBoolean {
        protected IsActiveTest() {
            super("isActive", UpdateUserDto::setIsActive);
            addRequiredTest(null);
        }
    }

    @Nested
    class EmailTest extends AbstractValidationFieldTestString {
        protected EmailTest() {
            super("email", UpdateUserDto::setEmail);
            addNotBlankTest(null);
        }
    }

    @Nested
    class NameTest extends AbstractValidationFieldTestString {
        protected NameTest() {
            super("name", UpdateUserDto::setName);
            addNotBlankTest(null);
        }
    }

    @Nested
    class ApprovalLimitTest extends AbstractValidationFieldTestBigDecimal {
        protected ApprovalLimitTest() {
            super("approvalLimit", UpdateUserDto::setApprovalLimit);
            addNotRequiredTest(null);
            addMin(BigDecimal.ZERO, null);
        }
    }
}
