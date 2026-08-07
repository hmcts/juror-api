package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;

@Getter
public enum EmailStatus {

    PENDING("PENDING"),
    SENT("SENT");

    private final String description;

    EmailStatus(String description) {
        this.description = description;
    }

}
