package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

import java.util.function.BiConsumer;

public class ViewExpenseRequestTest extends AbstractValidatorTest {

    class AbstractViewExpenseRequestTest extends AbstractValidatorTest.AbstractValidationFieldTestString<ViewExpenseRequest> {

        private final BiConsumer<ViewExpenseRequest, String> setFieldConsumer;

        protected AbstractViewExpenseRequestTest(String fieldName,
                                                 BiConsumer<ViewExpenseRequest, String> setFieldConsumer) {
            super(fieldName);
            this.setFieldConsumer = setFieldConsumer;
        }


        @Override
        protected void setField(ViewExpenseRequest baseObject, String value) {
            setFieldConsumer.accept(baseObject, value);
        }

        @Override
        protected ViewExpenseRequest createValidObject() {
            return createValid();
        }
    }

    private ViewExpenseRequest createValid() {
        return ViewExpenseRequest.builder()
            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
            .identifier(TestConstants.VALID_POOL_NUMBER)
            .build();
    }

    @Nested
    class JurorNumber extends AbstractViewExpenseRequestTest {
        protected JurorNumber() {
            super("jurorNumber", ViewExpenseRequest::setJurorNumber);
            ignoreAdditionalFailures();
            addNotBlankTest(new AbstractValidatorTest.FieldTestSupport(null));
            addInvalidPatternTest("ABC", "^\\d{9}$", null);
        }
    }
    @Nested
    class Identifier extends AbstractViewExpenseRequestTest {
        protected Identifier() {
            super("identifier", ViewExpenseRequest::setIdentifier);
            ignoreAdditionalFailures();
            addNotBlankTest(new AbstractValidatorTest.FieldTestSupport(null));
            addInvalidPatternTest("ABC", "^F\\d+$|^\\d{9}$", null);
        }
    }
}
