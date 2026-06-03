package uk.gov.hmcts.juror.api.juror.notify;

public interface NotifyAdapter {
    /**
     * Sends Email
     *
     * @param notification
     * @return
     * @throws NotifyApiException
     */
    EmailNotificationReceipt sendEmail(EmailNotification notification);

    EmailNotificationReceipt sendCommsEmail(EmailNotification notification);

    SmsNotificationReceipt sendCommsSms(SmsNotification notification);
}
