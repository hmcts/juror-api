package uk.gov.hmcts.juror.api.testsupport;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DisableIfWeekendCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult DISABLED =
        ConditionEvaluationResult.disabled("Test can only be run on weekdays.");

    private static final ConditionEvaluationResult ENABLED =
        ConditionEvaluationResult.enabled("Day is weekday. Test can be run.");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return DISABLED;
        }
        return ENABLED;
    }
}
