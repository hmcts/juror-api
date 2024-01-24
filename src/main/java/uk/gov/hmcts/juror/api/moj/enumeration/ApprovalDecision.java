package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;

@Getter
public enum ApprovalDecision {

    APPROVE("approved"),
    REJECT("rejected");

    private final String description;

    ApprovalDecision(String description) {
        this.description = description;
    }

}
