package uk.gov.hmcts.juror.api.juror.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public final class EmailNotification extends Notification {
    public EmailNotification(final String templateId, final String recipientEmail, final Map<String, String> payload) {
        this.templateId = templateId;
        this.recipientEmail = recipientEmail;
        this.payload = payload;
    }

    private String recipientEmail;
}
