package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.TotalExpenseDto;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

public class TotalExpenseDtoTest extends AbstractValidatorTest {

    class AbstractTotalExpenseDtoTest extends AbstractValidatorTest.AbstractValidationFieldTestBase<TotalExpenseDto,
        BigDecimal> {

        private final BiConsumer<TotalExpenseDto, BigDecimal> setFieldConsumer;

        protected AbstractTotalExpenseDtoTest(String fieldName,
                                              BiConsumer<TotalExpenseDto, BigDecimal> setFieldConsumer) {
            super(fieldName);
            this.setFieldConsumer = setFieldConsumer;
        }


        @Override
        protected void setField(TotalExpenseDto baseObject, BigDecimal value) {
            setFieldConsumer.accept(baseObject, value);
        }

        @Override
        protected TotalExpenseDto createValidObject() {
            return new TotalExpenseDto();
        }
    }

    @Nested
    class TotalAmount extends TotalExpenseDtoTest.AbstractTotalExpenseDtoTest {
        protected TotalAmount() {
            super("totalAmount", TotalExpenseDto::setTotalAmount);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class TotalAmountPaidToDate extends TotalExpenseDtoTest.AbstractTotalExpenseDtoTest {
        protected TotalAmountPaidToDate() {
            super("totalAmountPaidToDate", TotalExpenseDto::setTotalAmountPaidToDate);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class BalanceToPay extends TotalExpenseDtoTest.AbstractTotalExpenseDtoTest {
        protected BalanceToPay() {
            super("balanceToPay", TotalExpenseDto::setBalanceToPay);
            addRequiredTest(new FieldTestSupport(null));
        }
    }


    @Nested
    class TotalDays extends AbstractValidatorTest.AbstractValidationFieldTestNumeric<TotalExpenseDto, Integer> {
        protected TotalDays() {
            super("totalDays");
            addRequiredTest(null);
        }

        @Override
        protected void setField(TotalExpenseDto baseObject, Integer value) {
            baseObject.setTotalDays(value);
        }

        @Override
        protected TotalExpenseDto createValidObject() {
            return new TotalExpenseDto();
        }

        @Override
        protected Integer toNumber(String value) {
            return Integer.parseInt(value);
        }
    }
}
