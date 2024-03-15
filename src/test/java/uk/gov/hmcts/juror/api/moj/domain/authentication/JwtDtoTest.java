package uk.gov.hmcts.juror.api.moj.domain.authentication;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.utils.TestConstants;

public class JwtDtoTest extends AbstractValidatorTest<JwtDto> {

    @Override
    protected JwtDto createValidObject() {
        return new JwtDto(TestConstants.JWT);
    }

    @Nested
    class JwtTest extends AbstractValidationFieldTestString {
        protected JwtTest() {
            super("jwt", JwtDto::setJwt);
            addNotBlankTest(null);
        }
    }
}
