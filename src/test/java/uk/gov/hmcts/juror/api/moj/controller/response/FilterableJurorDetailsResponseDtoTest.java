package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAddressDtoTest;

class FilterableJurorDetailsResponseDtoTest extends AbstractValidatorTest<FilterableJurorDetailsResponseDto> {

    @Override
    protected FilterableJurorDetailsResponseDto createValidObject() {
        return FilterableJurorDetailsResponseDto.builder()
            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
            .jurorVersion(1L)
            .nameDetails(NameDetailsTest.createValidNameDetailsDto())
            .paymentDetails(PaymentDetailsTest.createPaymentDetailsDto())
            .address(JurorAddressDtoTest.createValidJurorAddressDto())
            .build();
    }

    @Nested
    class JurorNumberTest extends AbstractValidationFieldTestString {

        protected JurorNumberTest() {
            super("jurorNumber", FilterableJurorDetailsResponseDto::setJurorNumber);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("INVALID", "^\\d{9}$", null);
        }
    }

    @Nested
    class JurorVersionTest extends AbstractValidationFieldTestLong {

        protected JurorVersionTest() {
            super("jurorVersion", FilterableJurorDetailsResponseDto::setJurorVersion);
            addMustBePositive(null);
        }
    }
}
