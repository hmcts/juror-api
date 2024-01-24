package uk.gov.hmcts.juror.api.moj.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DeferralDecision {

    GRANT("Grant"),

    REFUSE("Refuse");

    private String description;

}