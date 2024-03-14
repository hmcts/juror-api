package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.List;

public class GetEnteredExpenseRequestTest extends AbstractValidatorTest<GetEnteredExpenseRequest> {
    @Override
    protected GetEnteredExpenseRequest createValidObject() {
        return GetEnteredExpenseRequest.builder()
            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
            .poolNumber(TestConstants.VALID_POOL_NUMBER)
            .expenseDates(List.of(LocalDate.now()))
            .build();
    }

    @Nested
    class ExpenseDatesTest extends AbstractValidationFieldTestList<LocalDate> {
        protected ExpenseDatesTest() {
            super("expenseDates", GetEnteredExpenseRequest::setExpenseDates);
            addNotEmptyTest(null);
            addNullValueInListTest(null);
        }
    }

    @Nested
    class JurorNumberTest extends AbstractValidationFieldTestString {
        protected JurorNumberTest() {
            super("jurorNumber", GetEnteredExpenseRequest::setJurorNumber);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("INVALID", ValidationConstants.JUROR_NUMBER, null);
        }
    }

    @Nested
    class PoolNumberTest extends AbstractValidationFieldTestString {
        protected PoolNumberTest() {
            super("poolNumber", GetEnteredExpenseRequest::setPoolNumber);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("INVALID", ValidationConstants.POOL_NUMBER, null);
        }
    }
}
