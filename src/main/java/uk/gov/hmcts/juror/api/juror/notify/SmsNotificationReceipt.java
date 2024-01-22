package uk.gov.hmcts.juror.api.juror.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.service.notify.SendSmsResponse;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public final class SmsNotificationReceipt extends NotificationReceipt {
    private String fromNumber;

    /**
     * Construct from a {@link uk.gov.service.notify.NotificationClient} {@link SendSmsResponse}.
     *
     * @param sendSmsResponse Notify response (not null)
     */
    SmsNotificationReceipt(final SendSmsResponse sendSmsResponse) {
        super(
            sendSmsResponse.getNotificationId(),
            sendSmsResponse.getReference().orElse(""),
            sendSmsResponse.getTemplateId(),
            sendSmsResponse.getTemplateVersion(),
            sendSmsResponse.getTemplateUri(),
            sendSmsResponse.getBody()
        );
        this.fromNumber = sendSmsResponse.getFromNumber().orElse("");
    }
}
