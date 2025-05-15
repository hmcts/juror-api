package uk.gov.hmcts.juror.api.juror.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public final class SmsNotification extends Notification {
    public SmsNotification(final String templateId, final String recipientPhoneNumber,
                           final Map<String, String> payload) {
        this.templateId = templateId;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.payload = payload;
    }

    private String recipientPhoneNumber;
}
