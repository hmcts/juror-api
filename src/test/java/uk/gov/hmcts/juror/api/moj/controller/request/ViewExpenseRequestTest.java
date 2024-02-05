package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

public class ViewExpenseRequestTest extends AbstractValidatorTest<ViewExpenseRequest> {

    @Override
    protected ViewExpenseRequest createValidObject() {
        return createValid();
    }

    private ViewExpenseRequest createValid() {
        return ViewExpenseRequest.builder()
            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
            .identifier(TestConstants.VALID_POOL_NUMBER)
            .build();
    }

    @Nested
    class JurorNumber extends AbstractValidationFieldTestString {
        protected JurorNumber() {
            super("jurorNumber", ViewExpenseRequest::setJurorNumber);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("ABC", "^\\d{9}$", null);
        }
    }

    @Nested
    class Identifier extends AbstractValidationFieldTestString {
        protected Identifier() {
            super("identifier", ViewExpenseRequest::setIdentifier);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("ABC", "^F\\d+$|^\\d{9}$", null);
        }
    }
}
