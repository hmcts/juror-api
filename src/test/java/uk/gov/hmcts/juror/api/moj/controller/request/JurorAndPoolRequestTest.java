package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

public class JurorAndPoolRequestTest extends AbstractValidatorTest<JurorAndPoolRequest> {

    @Override
    protected JurorAndPoolRequest createValidObject() {
        return JurorAndPoolRequest.builder()
            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
            .poolNumber(TestConstants.VALID_POOL_NUMBER)
            .build();
    }

    @Nested
    class JurorNumberTest extends AbstractValidationFieldTestString {

        protected JurorNumberTest() {
            super("jurorNumber", JurorAndPoolRequest::setJurorNumber);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("INVALID", "^\\d{9}$", null);
        }

        @Nested
        class PoolNumberTest extends AbstractValidationFieldTestString {

            protected PoolNumberTest() {
                super("poolNumber", JurorAndPoolRequest::setPoolNumber);
                ignoreAdditionalFailures();
                addNotBlankTest(null);
                addInvalidPatternTest("INVALID", "^\\d{9}$", null);
            }
        }
    }
}