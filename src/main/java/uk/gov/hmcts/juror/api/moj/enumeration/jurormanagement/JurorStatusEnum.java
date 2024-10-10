package uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JurorStatusEnum {
    POOL(0),
    SUMMONED(1),
    RESPONDED(2),
    PANEL(3),
    JUROR(4),
    EXCUSED(5),
    DISQUALIFIED(6),
    DEFERRED(7),
    REASSIGNED(8),
    UNDELIVERABLE(9),
    TRANSFERRED(10),
    AWAITINGINFO(11),
    FAILEDTOATTEND(12),
    COMPLETED(13);

    private final int status;

    public static JurorStatusEnum fromStatus(int status) {
        for (JurorStatusEnum jurorStatus : JurorStatusEnum.values()) {
            if (jurorStatus.getStatus() == status) {
                return jurorStatus;
            }
        }
        throw new IllegalArgumentException("Invalid Juror Status: " + status);
    }
}
