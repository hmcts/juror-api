package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinancialLossWarningTest extends AbstractValidatorTest<FinancialLossWarning> {

    public static FinancialLossWarning getValidObject() {
        return FinancialLossWarning.builder()
            .date(LocalDate.now())
            .jurorsLoss(BigDecimal.ONE)
            .limit(BigDecimal.ONE)
            .attendanceType(PayAttendanceType.FULL_DAY)
            .isLongTrialDay(true)
            .message("message")
            .build();
    }

    @Override
    protected FinancialLossWarning createValidObject() {
        return getValidObject();
    }

    @Nested
    class DateTest extends AbstractValidationFieldTestLocalDate {
        protected DateTest() {
            super("date", FinancialLossWarning::setDate);
            addRequiredTest(null);
        }
    }

    @Nested
    class JurorsLossTest extends AbstractValidationFieldTestBigDecimal {
        protected JurorsLossTest() {
            super("jurorsLoss", FinancialLossWarning::setJurorsLoss);
            addRequiredTest(null);
        }
    }

    @Nested
    class LimitTest extends AbstractValidationFieldTestBigDecimal {
        protected LimitTest() {
            super("limit", FinancialLossWarning::setLimit);
            addRequiredTest(null);
        }
    }

    @Nested
    class AttendanceTypeTest extends AbstractValidationFieldTestBase<PayAttendanceType> {
        protected AttendanceTypeTest() {
            super("attendanceType", FinancialLossWarning::setAttendanceType);
            addRequiredTest(null);
        }
    }

    @Nested
    class IsLongTrialDayTest extends AbstractValidationFieldTestBase<Boolean> {
        protected IsLongTrialDayTest() {
            super("isLongTrialDay", FinancialLossWarning::setIsLongTrialDay);
            addRequiredTest(null);
        }
    }

    @Nested
    class MessageTest extends AbstractValidationFieldTestString {
        protected MessageTest() {
            super("message", FinancialLossWarning::setMessage);
            addRequiredTest(null);
        }
    }
}
