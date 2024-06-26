package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.response.FilterableJurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorPoolDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.NameDetails;
import uk.gov.hmcts.juror.api.moj.controller.response.PaymentDetails;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class FilterableJurorDetailsRequestDtoTest extends AbstractValidatorTest<FilterableJurorDetailsRequestDto> {

    @Override
    protected FilterableJurorDetailsRequestDto createValidObject() {
        return createValidFilterableJurorDetailsRequestDto();
    }

    @Nested
    @DisplayName("Include Type")
    class IncludeTypeTest {
        private MockedStatic<PaymentDetails> paymentDetailsMockedStatic;
        private MockedStatic<NameDetails> nameDetailsMockedStatic;
        private MockedStatic<JurorAddressDto> jurorAddressDtoMockedStatic;
        private MockedStatic<JurorPoolDetailsDto> jurorPoolDetailsDtoMockedStatic;

        @AfterEach
        void mockCurrentUser() {
            if (paymentDetailsMockedStatic != null) {
                paymentDetailsMockedStatic.close();
            }
            if (nameDetailsMockedStatic != null) {
                nameDetailsMockedStatic.close();
            }
            if (jurorAddressDtoMockedStatic != null) {
                jurorAddressDtoMockedStatic.close();
            }
            if (jurorPoolDetailsDtoMockedStatic != null) {
                jurorPoolDetailsDtoMockedStatic.close();
            }
        }

        @Test
        void positivePaymentDetails() {
            Juror juror = mock(Juror.class);
            JurorPool jurorPool = mock(JurorPool.class);
            FilterableJurorDetailsResponseDto responseDto = mock(FilterableJurorDetailsResponseDto.class);

            FilterableJurorDetailsRequestDto.FilterContext filterContext =
                new FilterableJurorDetailsRequestDto.FilterContext(juror, jurorPool);

            PaymentDetails paymentDetails = mock(PaymentDetails.class);
            paymentDetailsMockedStatic = Mockito.mockStatic(PaymentDetails.class);
            paymentDetailsMockedStatic.when(() -> PaymentDetails.from(juror)).thenReturn(paymentDetails);

            FilterableJurorDetailsRequestDto.IncludeType.PAYMENT_DETAILS.apply(responseDto, filterContext);

            verify(responseDto, times(1)).setPaymentDetails(paymentDetails);
            verifyNoMoreInteractions(responseDto);
            paymentDetailsMockedStatic.verify(() -> PaymentDetails.from(juror), times(1));
            paymentDetailsMockedStatic.verifyNoMoreInteractions();
        }

        @Test
        void positiveNameDetails() {
            Juror juror = mock(Juror.class);
            JurorPool jurorPool = mock(JurorPool.class);
            FilterableJurorDetailsResponseDto responseDto = mock(FilterableJurorDetailsResponseDto.class);

            FilterableJurorDetailsRequestDto.FilterContext filterContext =
                new FilterableJurorDetailsRequestDto.FilterContext(juror, jurorPool);

            NameDetails nameDetails = mock(NameDetails.class);
            nameDetailsMockedStatic = Mockito.mockStatic(NameDetails.class);
            nameDetailsMockedStatic.when(() -> NameDetails.from(juror)).thenReturn(nameDetails);

            FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS.apply(responseDto, filterContext);

            verify(responseDto, times(1)).setNameDetails(nameDetails);
            verifyNoMoreInteractions(responseDto);
            nameDetailsMockedStatic.verify(() -> NameDetails.from(juror), times(1));
            nameDetailsMockedStatic.verifyNoMoreInteractions();
        }

        @Test
        void positiveAddressDetails() {
            Juror juror = mock(Juror.class);
            JurorPool jurorPool = mock(JurorPool.class);
            FilterableJurorDetailsResponseDto responseDto = mock(FilterableJurorDetailsResponseDto.class);

            FilterableJurorDetailsRequestDto.FilterContext filterContext =
                new FilterableJurorDetailsRequestDto.FilterContext(juror, jurorPool);

            JurorAddressDto addressDto = mock(JurorAddressDto.class);
            jurorAddressDtoMockedStatic = Mockito.mockStatic(JurorAddressDto.class);
            jurorAddressDtoMockedStatic.when(() -> JurorAddressDto.from(juror)).thenReturn(addressDto);

            FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS.apply(responseDto, filterContext);

            verify(responseDto, times(1)).setAddress(addressDto);
            verifyNoMoreInteractions(responseDto);
            jurorAddressDtoMockedStatic.verify(() -> JurorAddressDto.from(juror), times(1));
            jurorAddressDtoMockedStatic.verifyNoMoreInteractions();
        }

        @Test
        void positiveActivePoolDetails() {
            Juror juror = mock(Juror.class);
            JurorPool jurorPool = mock(JurorPool.class);
            FilterableJurorDetailsResponseDto responseDto = mock(FilterableJurorDetailsResponseDto.class);

            FilterableJurorDetailsRequestDto.FilterContext filterContext =
                new FilterableJurorDetailsRequestDto.FilterContext(juror, jurorPool);

            JurorPoolDetailsDto jurorPoolDetailsDto = mock(JurorPoolDetailsDto.class);
            jurorPoolDetailsDtoMockedStatic = Mockito.mockStatic(JurorPoolDetailsDto.class);
            jurorPoolDetailsDtoMockedStatic.when(() -> JurorPoolDetailsDto.from(jurorPool))
                .thenReturn(jurorPoolDetailsDto);

            FilterableJurorDetailsRequestDto.IncludeType.ACTIVE_POOL.apply(responseDto, filterContext);

            verify(responseDto, times(1)).setActivePool(jurorPoolDetailsDto);
            verifyNoMoreInteractions(responseDto);
            jurorPoolDetailsDtoMockedStatic.verify(() -> JurorPoolDetailsDto.from(jurorPool), times(1));
            jurorPoolDetailsDtoMockedStatic.verifyNoMoreInteractions();
        }
    }

    private FilterableJurorDetailsRequestDto createValidFilterableJurorDetailsRequestDto() {
        return FilterableJurorDetailsRequestDto.builder()
            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
            .jurorVersion(1L)
            .include(List.of(FilterableJurorDetailsRequestDto.IncludeType.values()))
            .build();
    }

    @Nested
    class JurorNumberTest extends AbstractValidationFieldTestString {

        protected JurorNumberTest() {
            super("jurorNumber", FilterableJurorDetailsRequestDto::setJurorNumber);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("INVALID", "^\\d{9}$", null);
        }
    }

    @Nested
    class JurorVersionTest extends AbstractValidationFieldTestLong {

        protected JurorVersionTest() {
            super("jurorVersion", FilterableJurorDetailsRequestDto::setJurorVersion);
            addMustBePositive(null);
        }
    }

    @Nested
    class IncludeTest extends AbstractValidationFieldTestList<FilterableJurorDetailsRequestDto.IncludeType> {

        protected IncludeTest() {
            super("include", FilterableJurorDetailsRequestDto::setInclude);
            addNotEmptyTest(null);
            addNullValueInListTest(null);
        }
    }
}
