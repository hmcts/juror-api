package uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement;

import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;

import java.util.List;

@Getter
public enum JurorStatusGroup {

    AT_COURT(List.of(IJurorStatus.RESPONDED, IJurorStatus.PANEL, IJurorStatus.JUROR)),
    IN_WAITING(IJurorStatus.getAllExcluding(IJurorStatus.JUROR)),
    ON_TRIAL(List.of(IJurorStatus.JUROR)),
    COMPLETED(List.of(IJurorStatus.COMPLETED)),
    PANELLED(List.of(IJurorStatus.PANEL)),

    ALL(IJurorStatus.getAllExcluding());

    private List<Integer> statusList;

    JurorStatusGroup(List<Integer> statusList) {
        this.statusList = statusList;
    }

}
