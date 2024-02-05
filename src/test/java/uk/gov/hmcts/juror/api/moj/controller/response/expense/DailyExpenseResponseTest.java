package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

public class DailyExpenseResponseTest extends AbstractValidatorTest<DailyExpenseResponse> {
    @Override
    protected DailyExpenseResponse createValidObject() {
        return new DailyExpenseResponse();
    }

    @Nested
    class FinancialLossWarningTest extends AbstractValidationFieldTestBase<FinancialLossWarning> {
        protected FinancialLossWarningTest() {
            super("financialLossWarning", DailyExpenseResponse::setFinancialLossWarning);
            addAllowNullTest(null);
            addAllowNotNullTest(
                uk.gov.hmcts.juror.api.moj.controller.response.expense.FinancialLossWarningTest.getValidObject(), null);
        }
    }
}
