package uk.gov.hmcts.juror.api.moj.controller.request.messages;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageType;

import java.util.List;


class MessageSendRequestTest extends AbstractValidatorTest<MessageSendRequest> {
    @Override
    protected MessageSendRequest createValidObject() {
        return MessageSendRequest.builder()
            .jurors(List.of(
                JurorAndSendTypeTest.getValidObject()
            ))
            .build();
    }

    @Nested
    class JurorsTest extends AbstractValidationFieldTestList<MessageSendRequest.JurorAndSendType> {
        protected JurorsTest() {
            super("jurors", MessageSendRequest::setJurors);
            addNotEmptyTest(null);
            addNullValueInListTest(null);
        }
    }

    @Nested
    class PlaceholderValuesTest extends AbstractValidationFieldTestMap<String, String> {
        protected PlaceholderValuesTest() {
            super("placeholderValues", MessageSendRequest::setPlaceholderValues);
            addNullKeyValueInMapTest(null);
            addNullValueInMapTest(null);
            addNotRequiredTest(null);
        }

        @Override
        protected String getValidKey() {
            return "Any";
        }

        @Override
        protected String getValidValue() {
            return "Any";
        }
    }

    @Nested
    class JurorAndSendTypeTest extends AbstractValidatorTest<MessageSendRequest.JurorAndSendType> {


        public static MessageSendRequest.JurorAndSendType getValidObject() {
            return MessageSendRequest.JurorAndSendType.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .poolNumber(TestConstants.VALID_POOL_NUMBER)
                .type(MessageType.SendType.EMAIL_AND_SMS)
                .build();
        }

        @Override
        protected MessageSendRequest.JurorAndSendType createValidObject() {
            return getValidObject();
        }

        @Nested
        class JurorNumberTest extends AbstractValidationFieldTestString {

            protected JurorNumberTest() {
                super("jurorNumber", MessageSendRequest.JurorAndSendType::setJurorNumber);
                ignoreAdditionalFailures();
                addNotBlankTest(null);
                addInvalidPatternTest("INVALID", "^\\d{9}$", null);
            }
        }

        @Nested
        class PoolNumberTest extends AbstractValidationFieldTestString {
            protected PoolNumberTest() {
                super("poolNumber", MessageSendRequest.JurorAndSendType::setPoolNumber);
                ignoreAdditionalFailures();
                addNotBlankTest(null);
                addInvalidPatternTest("INVALID", "^\\d{9}$", null);
            }
        }

        @Nested
        class TypeTest extends AbstractValidationFieldTestBase<MessageType.SendType> {
            protected TypeTest() {
                super("type", MessageSendRequest.JurorAndSendType::setType);
                ignoreAdditionalFailures();
                addRequiredTest(null);
            }
        }
    }
}
