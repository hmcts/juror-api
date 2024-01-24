package uk.gov.hmcts.juror.api.moj.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExcusalDecision {

    GRANT("Grant"),

    REFUSE("Refuse");

    private final String description;

}