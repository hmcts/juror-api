package uk.gov.hmcts.juror.api.juror.service;

import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

public interface JurorNotificationService {
    /**
     * Send a notify message.
     *
     * @param digitalResponse      Response to send the notification for.
     * @param notifyTemplateType Template type to use for the message
     * @throws NotificationServiceException Failed sending message
     */
    void sendResponseReceipt(DigitalResponse digitalResponse,
                             NotifyTemplateType notifyTemplateType) throws NotificationServiceException;

    /**
     * Build a email.
     *
     * @param digitalResponse      Juror response.
     * @param notifyTemplateType Type of response.
     * @return Email content with correct template selected.
     */
    EmailNotification createEmailNotification(DigitalResponse digitalResponse,
                                              NotifyTemplateType notifyTemplateType);
}
