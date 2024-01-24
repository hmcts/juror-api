package uk.gov.hmcts.juror.api.moj.enumeration.trial;

import lombok.Getter;

@Getter
public enum TrialType {

    CIV("Civil"),
    CRI("Criminal");

    final String description;

    TrialType(String description) {
        this.description = description;
    }

}
