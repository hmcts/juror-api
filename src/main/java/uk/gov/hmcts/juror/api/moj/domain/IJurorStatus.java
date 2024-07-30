package uk.gov.hmcts.juror.api.moj.domain;

// Todo: look at refactoring this class as an enum

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Constants for <code>JUROR_MOD.JUROR_STATUS</code>.
 */
public abstract class IJurorStatus {
    public static final int SUMMONED = 1;
    public static final int RESPONDED = 2;
    public static final int PANEL = 3;
    public static final int JUROR = 4;
    public static final int EXCUSED = 5;
    public static final int DISQUALIFIED = 6;
    public static final int DEFERRED = 7;
    public static final int REASSIGNED = 8;
    public static final int UNDELIVERABLE = 9;
    public static final int TRANSFERRED = 10;
    public static final int ADDITIONAL_INFO = 11;
    public static final int FAILED_TO_ATTEND = 12;
    public static final int COMPLETED = 13;


    public static List<Integer> getAllExcluding(int... excluding) {
        return Stream.of(SUMMONED, RESPONDED, PANEL, JUROR, EXCUSED, DISQUALIFIED, DEFERRED, REASSIGNED,
                UNDELIVERABLE, TRANSFERRED, ADDITIONAL_INFO, FAILED_TO_ATTEND, COMPLETED)
            .filter(i -> Arrays.stream(excluding).noneMatch(j -> j == i))
            .collect(Collectors.toList());
    }
}
