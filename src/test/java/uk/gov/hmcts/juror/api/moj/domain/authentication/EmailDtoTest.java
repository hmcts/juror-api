package uk.gov.hmcts.juror.api.moj.domain.authentication;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

public class EmailDtoTest extends AbstractValidatorTest<EmailDto> {

    @Override
    protected EmailDto createValidObject() {
        return new EmailDto(TestConstants.VALID_EMAIL);
    }

    @Nested
    class EmailTest extends AbstractValidationFieldTestString {
        protected EmailTest() {
            super("email", EmailDto::setEmail);
            addNotBlankTest(null);
        }
    }
}
