package uk.gov.hmcts.juror.api.juror.domain;

import lombok.Getter;

/**
 * Enum of values of a juror response "processing status".
 */
@Getter
public enum ProcessingStatus {
    TODO("To Do"),

    AWAITING_CONTACT("Awaiting Juror"),
    AWAITING_COURT_REPLY("Awaiting Court"),
    AWAITING_TRANSLATION("Awaiting Translation"),
    CLOSED("Closed"),

    @Deprecated
    REFERRED_TO_TEAM_LEADER("Referred to Team Leader"); // no longer used in code, but may be in db

    private final String description;

    ProcessingStatus(String description) {
        this.description = description;
    }

}
