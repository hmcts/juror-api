package uk.gov.hmcts.juror.api.juror.notify;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class NotificationReceipt {
    private UUID notificationId;
    private String reference;
    private UUID templateId;
    private int templateVersion;
    private String templateUri;
    private String body;

    protected NotificationReceipt() {
        throw new IllegalArgumentException("NotificationReceipt should not be instantiated.");
    }
}
