package uk.gov.hmcts.juror.api.juror.service;

public enum StraightThroughType {
    ACCEPTANCE("STRAIGHT_THROUGH_ACCEPTANCE_DISABLED", "Acceptance"),
    DECEASED_EXCUSAL("STRAIGHT_THROUGH_DECEASED_EXCUSAL_DISABLED", "Deceased Excusal"),
    AGE_EXCUSAL("STRAIGHT_THROUGH_AGE_EXCUSAL_DISABLED", "Age Excusal");

    private final String dbName;
    private final String readableName;

    StraightThroughType(String dbName, String readableName) {
        this.dbName = dbName;
        this.readableName = readableName;
    }

    public String getDbName() {
        return dbName;
    }

    public String getReadableName() {
        return readableName;
    }
}
