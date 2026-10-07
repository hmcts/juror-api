package uk.gov.hmcts.juror.api.moj.service.poolmanagement;

public final class JurorManagementConstants {

    static final String[] POOL_MEMBER_IGNORE_PROPERTIES = {"owner", "poolNumber", "startDate",
        "userEdtq", "status", "poolSequence", "court", "nextDate", "completionFlag", "completionDate", "welsh",
        "lastUpdate", "summonsFile", "notifications", "transferDate", "timesSelected", "trialNumber", "mileage",
        "location", "noAttended", "failedToAttendCount", "unauthorisedAbsenceCount", "onCall", "smartCard",
        "amountSpent", "paidCash", "travelTime", "scanCode", "financialLoss", "reminderSent", "editTag"};

    static final char DEFAULT_POOL_NEW_REQUEST = 'N';

    // Validation messages
    public static final String ABOVE_AGE_LIMIT_MESSAGE = "Above maximum age limit on proposed service start date";
    public static final String BELOW_AGE_LIMIT_MESSAGE = "Below minimum age limit on proposed service start date";
    public static final String INVALID_STATUS_MESSAGE = "Invalid Status: %s";
    public static final String NO_ACTIVE_RECORD_MESSAGE = "Unable to find an active record at the source court "
        + "location";

    private JurorManagementConstants() {
        // an empty private constructor
    }
}
