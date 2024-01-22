package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.SmsNotification;
import uk.gov.hmcts.juror.api.juror.service.NotificationServiceException;

import java.util.Map;

public interface JurorCommsNotificationService {

    /**
     * Determine Notify Template and send a notify message.
     *
     * @param poolDetails                  Resp to send the notification for.
     * @param jurorCommsNotifyTemplateType Template type to use for the message
     * @throws NotificationServiceException Failed sending message
     */
    void sendJurorComms(Pool poolDetails, JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                        String commsTemplateId, String detailData, Boolean smsComms);

    /**
     * Build an email.
     *
     * @param poolDetails                  pool details.
     * @param jurorCommsNotifyTemplateType Type of response.
     * @return Email content with correct template selected.
     */
    EmailNotification createEmailNotification(Pool poolDetails,
                                              JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                                              String templateId, Map<String, String> payLoad);


    /**
     * Determine Notify Template and send a notify message.
     *
     * @param poolDetails                  Resp to send the notification for.
     * @param jurorCommsNotifyTemplateType Template type to use for the message
     * @throws NotificationServiceException Failed sending message
     */
    void sendJurorCommsSms(Pool poolDetails, JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                           String commsTemplateId, String detailData, Boolean smsComms);

    /**
     * Build an SMS.
     *
     * @param poolDetails                  pool details.
     * @param jurorCommsNotifyTemplateType Type of response.
     * @return Email content with correct template selected.
     */
    SmsNotification createSmsNotification(Pool poolDetails,
                                          JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                                          String templateId, Map<String, String> payLoad);


}
