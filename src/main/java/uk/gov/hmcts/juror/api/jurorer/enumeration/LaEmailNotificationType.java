package uk.gov.hmcts.juror.api.jurorer.enumeration;

public enum LaEmailNotificationType {
    INITIAL_EMAIL("LA_INITIAL_EMAIL_%s"),
    REMINDER_EMAIL("LA_REMINDER_EMAIL_%s");

    private final String referenceFormat;
    LaEmailNotificationType(String referenceFormat) {
        this.referenceFormat = referenceFormat;
    }

    public String reference(String email) {
        return referenceFormat.formatted(email);
    }

    public enum InitialEmailStatus {
        SENT,
        NOT_SENT
    }
}

