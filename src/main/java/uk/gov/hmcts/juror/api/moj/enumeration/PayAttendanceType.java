package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;

@Getter
public enum PayAttendanceType {
    FULL_DAY("Full day"),
    HALF_DAY("Half day");

    public final String displayName;

    PayAttendanceType(String displayName) {
        this.displayName = displayName;
    }
}
