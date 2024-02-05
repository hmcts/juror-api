package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.TotalExpenseDto;

class TotalExpenseDtoTest extends AbstractValidatorTest<TotalExpenseDto> {

    @Override
    protected TotalExpenseDto createValidObject() {
        return new TotalExpenseDto();
    }


    @Nested
    class TotalAmount extends AbstractValidationFieldTestBigDecimal {
        protected TotalAmount() {
            super("totalAmount", TotalExpenseDto::setTotalAmount);
            addRequiredTest(null);
        }
    }

    @Nested
    class TotalAmountPaidToDate extends AbstractValidationFieldTestBigDecimal {
        protected TotalAmountPaidToDate() {
            super("totalAmountPaidToDate", TotalExpenseDto::setTotalAmountPaidToDate);
            addRequiredTest(null);
        }
    }

    @Nested
    class BalanceToPay extends AbstractValidationFieldTestBigDecimal {
        protected BalanceToPay() {
            super("balanceToPay", TotalExpenseDto::setBalanceToPay);
            addRequiredTest(null);
        }
    }


    @Nested
    class TotalDays extends AbstractValidationFieldTestInteger {
        protected TotalDays() {
            super("totalDays", TotalExpenseDto::setTotalDays);
            addRequiredTest(null);
        }
    }
}
