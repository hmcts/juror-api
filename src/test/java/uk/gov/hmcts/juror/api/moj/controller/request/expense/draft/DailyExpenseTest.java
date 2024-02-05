package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.List;

public class DailyExpenseTest extends AbstractValidatorTest<DailyExpense> {
    @Override
    protected DailyExpense createValidObject() {
        return DailyExpense.builder()
            .dateOfExpense(LocalDate.now())
            .poolNumber(TestConstants.VALID_POOL_NUMBER)
            .payCash(true)
            .build();
    }

    @Nested
    class DateOfExpenseTest extends AbstractValidationFieldTestLocalDate {
        protected DateOfExpenseTest() {
            super("dateOfExpense", DailyExpense::setDateOfExpense);
            addRequiredTest(new FieldTestSupport()
                .setGroups(
                    DailyExpense.AttendanceDay.class,
                    DailyExpense.NonAttendanceDay.class
                )
            );
        }
    }

    @Nested
    class PoolNumberTest extends AbstractValidationFieldTestString {
        protected PoolNumberTest() {
            super("poolNumber", DailyExpense::setPoolNumber);
            ignoreAdditionalFailures();
            addNotBlankTest(new FieldTestSupport()
                .setGroups(
                    DailyExpense.AttendanceDay.class,
                    DailyExpense.NonAttendanceDay.class
                )
            );
            addInvalidPatternTest("INVALID", ValidationConstants.POOL_NUMBER, null);
        }
    }

    @Nested
    class PayCashTest extends AbstractValidationFieldTestBase<Boolean> {
        protected PayCashTest() {
            super("payCash", DailyExpense::setPayCash);
            addRequiredTest(new FieldTestSupport()
                .setGroups(
                    DailyExpense.AttendanceDay.class,
                    DailyExpense.NonAttendanceDay.class
                ));
        }
    }

    @Nested
    class TimeTest extends AbstractValidationFieldTestBase<DailyExpenseTime> {
        protected TimeTest() {
            super("time", DailyExpense::setTime);
            addAllowNotNullTest(DailyExpenseTimeTest.getValidObject(), new FieldTestSupport()
                .setGroups(DailyExpense.NonAttendanceDay.class));
            addAllowNotNullTest(DailyExpenseTimeTest.getValidObject(), new FieldTestSupport()
                .setGroups(DailyExpense.AttendanceDay.class));

            addAllowNullTest(new FieldTestSupport()
                .setGroups(DailyExpense.NonAttendanceDay.class));
            addAllowNullTest(new FieldTestSupport()
                .setGroups(DailyExpense.AttendanceDay.class));
        }
    }

    @Nested
    class FinancialLossTest extends AbstractValidationFieldTestBase<DailyExpenseFinancialLoss> {
        protected FinancialLossTest() {
            super("financialLoss", DailyExpense::setFinancialLoss);
            addAllowNotNullTest(DailyExpenseFinancialLossTest.getValidObject(), new FieldTestSupport()
                .setGroups(DailyExpense.NonAttendanceDay.class));
            addAllowNotNullTest(DailyExpenseFinancialLossTest.getValidObject(), new FieldTestSupport()
                .setGroups(DailyExpense.AttendanceDay.class));

            addAllowNullTest(new FieldTestSupport()
                .setGroups(DailyExpense.NonAttendanceDay.class));
            addAllowNullTest(new FieldTestSupport()
                .setGroups(DailyExpense.AttendanceDay.class));
        }
    }

    @Nested
    class TravelTest extends AbstractValidationFieldTestBase<DailyExpenseTravel> {
        protected TravelTest() {
            super("travel", DailyExpense::setTravel);
            addNullTest(DailyExpenseTravelTest.getValidObject(),
                new FieldTestSupport()
                    .setGroups(DailyExpense.NonAttendanceDay.class));
            addAllowNotNullTest(DailyExpenseTravelTest.getValidObject(), new FieldTestSupport()
                .setGroups(DailyExpense.AttendanceDay.class));
        }
    }

    @Nested
    class FoodAndDrinkTest extends AbstractValidationFieldTestBase<DailyExpenseFoodAndDrink> {
        protected FoodAndDrinkTest() {
            super("foodAndDrink", DailyExpense::setFoodAndDrink);
            addNullTest(DailyExpenseFoodAndDrinkTest.getValidObject(),
                new FieldTestSupport()
                    .setGroups(DailyExpense.NonAttendanceDay.class));
            addAllowNotNullTest(DailyExpenseFoodAndDrinkTest.getValidObject(), new FieldTestSupport()
                .setGroups(DailyExpense.AttendanceDay.class));
        }
    }

    @Nested
    class ApplyToAllDaysListTest extends AbstractValidationFieldTestList<DailyExpenseApplyToAllDays> {
        protected ApplyToAllDaysListTest() {
            super("applyToAllDays", DailyExpense::setApplyToAllDays);
            addNullValueInListTest(null);
        }

        @Test
        @DisplayName("Non Attendance Days should only allow [LOSS_OF_EARNINGS, EXTRA_CARE_COSTS, OTHER_COSTS, "
            + "PAY_CASH]")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void positiveNonAttendanceDayOnlyAllowedSomeTypes() {
            DailyExpense dto = createValidObject();
            dto.setApplyToAllDays(List.of(
                DailyExpenseApplyToAllDays.EXTRA_CARE_COSTS,
                DailyExpenseApplyToAllDays.OTHER_COSTS,
                DailyExpenseApplyToAllDays.PAY_CASH,
                DailyExpenseApplyToAllDays.LOSS_OF_EARNINGS
            ));
            expectNoViolations(dto);
        }

        @Test
        @DisplayName("Non Attendance Days should not allow any types that are not "
            + "[LOSS_OF_EARNINGS, EXTRA_CARE_COSTS, OTHER_COSTS, PAY_CASH]")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void negativeNonAttendanceDayOnlyAllowedSomeTypes() {
            DailyExpense dto = createValidObject();
            dto.setApplyToAllDays(List.of(
                DailyExpenseApplyToAllDays.TRAVEL_COSTS
            ));
            expectViolations(dto,
                new FieldTestSupport().setGroups(DailyExpense.NonAttendanceDay.class),
                new Violation("applyToAllDays[0].<list element>",
                    "Non Attendance day can only apply to all for [EXTRA_CARE_COSTS, OTHER_COSTS, PAY_CASH]")
            );
        }

        @Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void attendanceDayOnlyAllowedAllTypes() {
            DailyExpense dto = createValidObject();
            dto.setApplyToAllDays(List.of(
                DailyExpenseApplyToAllDays.values()
            ));
            expectNoViolations(dto);
        }
    }
}
