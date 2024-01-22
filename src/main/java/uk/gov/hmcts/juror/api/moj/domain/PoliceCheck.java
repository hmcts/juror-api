package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum PoliceCheck {
    NOT_CHECKED(false, false, "Not Checked"),
    INSUFFICIENT_INFORMATION(false,false,NOT_CHECKED.getDescription()),
    IN_PROGRESS(false, false, "In Progress"),
    ELIGIBLE(false, true, "Passed"),
    INELIGIBLE(false, true, "Failed"),

    ERROR_RETRY_NAME_HAS_NUMERICS(true, false, IN_PROGRESS.getDescription()),
    ERROR_RETRY_CONNECTION_ERROR(true, false, IN_PROGRESS.getDescription()),
    ERROR_RETRY_OTHER_ERROR_CODE(true, false, IN_PROGRESS.getDescription()),
    ERROR_RETRY_NO_ERROR_REASON(true, false, IN_PROGRESS.getDescription()),
    ERROR_RETRY_UNEXPECTED_EXCEPTION(true, false, IN_PROGRESS.getDescription()),

    UNCHECKED_MAX_RETRIES_EXCEEDED(true, false, "Not Checked - There was a problem");

    private final boolean isError;
    private final boolean isChecked;
    private final String description;

    PoliceCheck(boolean isError, boolean isChecked, String description) {
        this.isError = isError;
        this.isChecked = isChecked;
        this.description = description;
    }

    public static Character isChecked(PoliceCheck policeCheck) {
        if (policeCheck == null || policeCheck == NOT_CHECKED) {
            return null;
        }
        return policeCheck.isChecked ? 'C' : 'U';
    }

    public static String getDescription(PoliceCheck policeCheck) {
        return Objects.requireNonNullElse(policeCheck, PoliceCheck.NOT_CHECKED).getDescription();
    }

    public static PoliceCheck getEffectiveValue(PoliceCheck oldValue, PoliceCheck newValue) {
        if (newValue != null
            && newValue.isError()
            && oldValue != null
            && oldValue.isError()) {
            return PoliceCheck.UNCHECKED_MAX_RETRIES_EXCEEDED;
        } else {
            return newValue;
        }
    }
}
