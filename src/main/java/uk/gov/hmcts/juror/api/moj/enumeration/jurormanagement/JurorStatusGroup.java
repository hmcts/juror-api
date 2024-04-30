package uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement;

import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;

import java.util.List;

@Getter
public enum JurorStatusGroup {

    AT_COURT(List.of(IJurorStatus.RESPONDED, IJurorStatus.PANEL, IJurorStatus.JUROR)),
    IN_WAITING(List.of(IJurorStatus.RESPONDED, IJurorStatus.PANEL)),
    ON_TRIAL(List.of(IJurorStatus.JUROR)),
    COMPLETED(List.of(IJurorStatus.COMPLETED));

    private List<Integer> statusList;

    JurorStatusGroup(List<Integer> statusList) {
        this.statusList = statusList;
    }

}
