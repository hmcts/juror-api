package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

import java.time.LocalDate;
import java.util.List;

public class GetEnteredExpenseRequestTest extends AbstractValidatorTest<GetEnteredExpenseRequest> {
    @Override
    protected GetEnteredExpenseRequest createValidObject() {
        return GetEnteredExpenseRequest.builder()
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
}
