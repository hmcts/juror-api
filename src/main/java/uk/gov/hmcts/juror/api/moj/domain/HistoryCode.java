package uk.gov.hmcts.juror.api.moj.domain;

public enum HistoryCode {

    PHSI("Number of Summons Issued"),
    PHDI("Number of Deferrals in"),
    PDEF("Deferred Pool Member"),
    RSUM("Print Summons"),
    PREQ("Change Pool Request Details"),

    PEDT("Pool Edit"),
    DELP("Pool Deleted"),
    PDIS("Disqualify Pool Member"),
    PTRA("Transfer Pool Member"),
    RESP("Responded"),
    PREA("Reassign Pool Member");

    private final String description;

    HistoryCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
