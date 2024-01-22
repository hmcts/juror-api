package uk.gov.hmcts.juror.api.juror.service;

import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;

public interface JurorNotificationService {
    /**
     * Send a notify message.
     *
     * @param jurorResponse      Response to send the notification for.
     * @param notifyTemplateType Template type to use for the message
     * @throws NotificationServiceException Failed sending message
     */
    void sendResponseReceipt(JurorResponse jurorResponse,
                             NotifyTemplateType notifyTemplateType) throws NotificationServiceException;

    /**
     * Build a email.
     *
     * @param jurorResponse      Juror response.
     * @param notifyTemplateType Type of response.
     * @return Email content with correct template selected.
     */
    EmailNotification createEmailNotification(JurorResponse jurorResponse,
                                              NotifyTemplateType notifyTemplateType);
}
