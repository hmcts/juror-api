package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;

import java.math.BigDecimal;

public class DailyExpenseFoodAndDrinkTest extends AbstractValidatorTest<DailyExpenseFoodAndDrink> {
    public static DailyExpenseFoodAndDrink getValidObject() {
        return DailyExpenseFoodAndDrink.builder()
            .foodAndDrinkClaimType(FoodDrinkClaimType.NONE)
            .build();
    }

    @Override
    protected DailyExpenseFoodAndDrink createValidObject() {
        return getValidObject();
    }

    @Nested
    class FoodAndDrinkClaimTypeTest extends AbstractValidationFieldTestBase<FoodDrinkClaimType> {
        protected FoodAndDrinkClaimTypeTest() {
            super("foodAndDrinkClaimType", DailyExpenseFoodAndDrink::setFoodAndDrinkClaimType);
            addRequiredTest(new FieldTestSupport()
                .setGroups(
                    DailyExpense.AttendanceDay.class,
                    DailyExpense.NonAttendanceDay.class
                )
            );
        }
    }

    @Nested
    class SmartCardAmountTest extends AbstractValidationFieldTestBigDecimal {
        protected SmartCardAmountTest() {
            super("smartCardAmount", DailyExpenseFoodAndDrink::setSmartCardAmount);
            addNotRequiredTest(null);
            addMin(BigDecimal.ZERO, null);
        }
    }

}
