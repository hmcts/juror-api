package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PaymentDetailsTest extends AbstractValidatorTest<PaymentDetails> {

    @Test
    void positiveFromTest() {
        Juror juror = mock(Juror.class);
        PaymentDetails expected = createPaymentDetailsDto();

        when(juror.getSortCode()).thenReturn(expected.getSortCode());
        when(juror.getBankAccountName()).thenReturn(expected.getBankAccountName());
        when(juror.getBankAccountNumber()).thenReturn(expected.getBankAccountNumber());
        when(juror.getBuildingSocietyRollNumber()).thenReturn(expected.getBuildingSocietyRollNumber());

        assertEquals(expected,
            PaymentDetails.from(juror),
            "Payment Details should match that from Juror record");

        verify(juror, times(1)).getSortCode();
        verify(juror, times(1)).getBankAccountName();
        verify(juror, times(1)).getBankAccountNumber();
        verify(juror, times(1)).getBuildingSocietyRollNumber();
        verifyNoMoreInteractions(juror);
    }

    public static PaymentDetails createPaymentDetailsDto() {
        return PaymentDetails.builder()
            .sortCode("112233")
            .bankAccountName("Bank Name")
            .bankAccountNumber("12345678")
            .buildingSocietyRollNumber("RollNum")
            .build();
    }

    @Override
    protected PaymentDetails createValidObject() {
        return createPaymentDetailsDto();
    }

    @Nested
    class SortCode extends AbstractValidationFieldTestString {
        protected SortCode() {
            super("sortCode", PaymentDetails::setSortCode);
            addLengthTest(0, 6, null);
        }
    }

    @Nested
    class BankAccountName extends AbstractValidationFieldTestString {
        protected BankAccountName() {
            super("bankAccountName", PaymentDetails::setBankAccountName);
            addLengthTest(0, 18, null);
        }
    }

    @Nested
    class BankAccountNumber extends AbstractValidationFieldTestString {
        protected BankAccountNumber() {
            super("bankAccountNumber", PaymentDetails::setBankAccountNumber);
            addLengthTest(0, 8, null);
        }
    }

    @Nested
    class BuildingSocietyRollNumber extends AbstractValidationFieldTestString {
        protected BuildingSocietyRollNumber() {
            super("buildingSocietyRollNumber", PaymentDetails::setBuildingSocietyRollNumber);
            addLengthTest(0, 18, null);
        }
    }
}

