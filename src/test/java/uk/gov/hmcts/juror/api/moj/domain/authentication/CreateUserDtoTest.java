package uk.gov.hmcts.juror.api.moj.domain.authentication;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.math.BigDecimal;
import java.util.Set;

public class CreateUserDtoTest extends AbstractValidatorTest<CreateUserDto> {

    @Override
    protected CreateUserDto createValidObject() {
        return CreateUserDto.builder()
            .userType(UserType.BUREAU)
            .email(TestConstants.VALID_EMAIL)
            .name("name123")
            .approvalLimit(new BigDecimal("123"))
            .roles(Set.of(Role.MANAGER))
            .build();
    }


    @Nested
    class UserTypeTest extends AbstractValidationFieldTestBase<UserType> {
        protected UserTypeTest() {
            super("userType", CreateUserDto::setUserType);
            addRequiredTest(null);
        }
    }

    @Nested
    class EmailTest extends AbstractValidationFieldTestString {
        protected EmailTest() {
            super("email", CreateUserDto::setEmail);
            addNotBlankTest(null);
        }
    }

    @Nested
    class NameTest extends AbstractValidationFieldTestString {
        protected NameTest() {
            super("name", CreateUserDto::setName);
            addNotBlankTest(null);
        }
    }

    @Nested
    class ApprovalLimitTest extends AbstractValidationFieldTestBigDecimal {
        protected ApprovalLimitTest() {
            super("approvalLimit", CreateUserDto::setApprovalLimit);
            addNotRequiredTest(null);
            addMin(BigDecimal.ZERO, null);
        }
    }
}
