package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAddressDtoTest;

public class FilterableJurorDetailsResponseDtoTest extends AbstractValidatorTest {

    private FilterableJurorDetailsResponseDto createValidNameDetailsDto() {
        return FilterableJurorDetailsResponseDto.builder()
            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
            .jurorVersion(1L)
            .nameDetails(NameDetailsTest.createValidNameDetailsDto())
            .paymentDetails(PaymentDetailsTest.createPaymentDetailsDto())
            .address(JurorAddressDtoTest.createValidJurorAddressDto())
            .build();
    }

    @Nested
    class JurorNumberTest extends
        AbstractValidatorTest.AbstractValidationFieldTestString<FilterableJurorDetailsResponseDto> {

        protected JurorNumberTest() {
            super("jurorNumber");
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("INVALID", "^\\d{9}$", null);
        }

        @Override
        protected void setField(FilterableJurorDetailsResponseDto baseObject, String value) {
            baseObject.setJurorNumber(value);
        }

        @Override
        protected FilterableJurorDetailsResponseDto createValidObject() {
            return createValidNameDetailsDto();
        }
    }

    @Nested
    class JurorVersionTest extends
        AbstractValidatorTest.AbstractValidationFieldTestNumeric<FilterableJurorDetailsResponseDto, Long> {

        protected JurorVersionTest() {
            super("jurorVersion");
            addMustBePositive(null);
        }


        @Override
        protected void setField(FilterableJurorDetailsResponseDto baseObject, Long value) {
            baseObject.setJurorVersion(value);
        }

        @Override
        protected FilterableJurorDetailsResponseDto createValidObject() {
            return createValidNameDetailsDto();
        }

        @Override
        protected Long toNumber(String value) {
            return Long.parseLong(value);
        }
    }
}
