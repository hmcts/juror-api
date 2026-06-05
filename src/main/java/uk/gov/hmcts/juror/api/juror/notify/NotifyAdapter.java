package uk.gov.hmcts.juror.api.juror.notify;

public interface NotifyAdapter {
    /**
     * Sends Email.
     *
     * @param notification notification to send.
     * @return notification receipt.
     * @throws NotifyApiException if Notify rejects the request.
     */
    EmailNotificationReceipt sendEmail(EmailNotification notification);

    EmailNotificationReceipt sendCommsEmail(EmailNotification notification);

    SmsNotificationReceipt sendCommsSms(SmsNotification notification);
}
