package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;

@Getter
public enum CommunicationChannel {

    EMAIL("EMAIL"),
    LETTER("LETTER");

    private final String description;

    CommunicationChannel(String description) {
        this.description = description;
    }

}
