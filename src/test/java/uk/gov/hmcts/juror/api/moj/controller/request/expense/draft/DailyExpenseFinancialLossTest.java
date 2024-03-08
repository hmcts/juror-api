package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

import java.math.BigDecimal;

public class DailyExpenseFinancialLossTest extends AbstractValidatorTest<DailyExpenseFinancialLoss> {
    public static DailyExpenseFinancialLoss getValidObject() {
        return DailyExpenseFinancialLoss.builder().build();
    }

    @Override
    protected DailyExpenseFinancialLoss createValidObject() {
        return getValidObject();
    }

    @Nested
    class LossOfEarningsOrBenefitsTest extends AbstractValidationFieldTestBigDecimal {
        protected LossOfEarningsOrBenefitsTest() {
            super("lossOfEarningsOrBenefits", DailyExpenseFinancialLoss::setLossOfEarningsOrBenefits);
            addNotRequiredTest(BigDecimal.ONE);
            addMin(BigDecimal.ZERO, null);
            addMax(new BigDecimal("1000000"),null);
        }
    }

    @Nested
    class ExtraCareCostTest extends AbstractValidationFieldTestBigDecimal {
        protected ExtraCareCostTest() {
            super("extraCareCost", DailyExpenseFinancialLoss::setExtraCareCost);
            addNotRequiredTest(BigDecimal.ONE);
            addMin(BigDecimal.ZERO, null);
            addMax(new BigDecimal("1000000"),null);
        }
    }

    @Nested
    class OtherCostsTest extends AbstractValidationFieldTestBigDecimal {
        protected OtherCostsTest() {
            super("otherCosts", DailyExpenseFinancialLoss::setOtherCosts);
            addNotRequiredTest(BigDecimal.ONE);
            addMin(BigDecimal.ZERO, null);
            addMax(new BigDecimal("1000000"),null);
        }
    }

    @Nested
    class OtherCostsDescriptionTest extends AbstractValidationFieldTestString {
        protected OtherCostsDescriptionTest() {
            super("otherCostsDescription", DailyExpenseFinancialLoss::setOtherCostsDescription);
            addNotRequiredTest("Desc");
        }
    }
}
