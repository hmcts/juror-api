package uk.gov.hmcts.juror.api.juror.domain;

/**
 * Enum of values of a juror response "processing status".
 */
public enum ProcessingStatus {
    TODO("To Do"),

    AWAITING_CONTACT("Awaiting Juror"),
    AWAITING_COURT_REPLY("Awaiting Court"),
    AWAITING_TRANSLATION("Awaiting Translation"),

    @Deprecated
    REFERRED_TO_TEAM_LEADER("Referred to Team Leader"), // no longer used in code, but may be in db

    CLOSED("Closed");

    private final String description;

    ProcessingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
