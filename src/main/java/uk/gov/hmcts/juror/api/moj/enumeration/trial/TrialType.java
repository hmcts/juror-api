package uk.gov.hmcts.juror.api.moj.enumeration.trial;

import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;

@Getter
public enum TrialType implements HasCodeAndDescription {

    CIV("Civil"),
    CRI("Criminal");

    final String description;

    TrialType(String description) {
        this.description = description;
    }

    @Override
    public String getCode() {
        return this.name();
    }
}
