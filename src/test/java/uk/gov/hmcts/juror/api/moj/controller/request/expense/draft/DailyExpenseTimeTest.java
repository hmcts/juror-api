package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;

public class DailyExpenseTimeTest extends AbstractValidatorTest<DailyExpenseTime> {
    public static DailyExpenseTime getValidObject() {
        return DailyExpenseTime.builder()
            .payAttendance(PayAttendanceType.FULL_DAY)
            .build();
    }

    @Override
    protected DailyExpenseTime createValidObject() {
        return getValidObject();
    }

    @Nested
    class PayAttendanceTest extends AbstractValidationFieldTestBase<PayAttendanceType> {
        protected PayAttendanceTest() {
            super("payAttendance", DailyExpenseTime::setPayAttendance);
            addNotRequiredTest(PayAttendanceType.FULL_DAY, new FieldTestSupport().setGroups(
                DailyExpense.NonAttendanceDay.class,
                DailyExpense.AttendanceDay.class));
        }
    }
}
