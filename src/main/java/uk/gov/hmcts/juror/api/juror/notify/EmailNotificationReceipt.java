package uk.gov.hmcts.juror.api.juror.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.service.notify.SendEmailResponse;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public final class EmailNotificationReceipt extends NotificationReceipt {
    private String subject;
    private String fromEmail;

    /**
     * Construct from a {@link uk.gov.service.notify.NotificationClient} {@link SendEmailResponse}.
     *
     * @param sendEmailResponse Notify response (not null)
     */
    EmailNotificationReceipt(final SendEmailResponse sendEmailResponse) {
        super(
            sendEmailResponse.getNotificationId(),
            sendEmailResponse.getReference().orElse(""),
            sendEmailResponse.getTemplateId(),
            sendEmailResponse.getTemplateVersion(),
            sendEmailResponse.getTemplateUri(),
            sendEmailResponse.getBody()
        );
        this.subject = sendEmailResponse.getSubject();
        this.fromEmail = sendEmailResponse.getFromEmail().orElse("");
    }
}
