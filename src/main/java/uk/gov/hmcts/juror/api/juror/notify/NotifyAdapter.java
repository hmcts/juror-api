package uk.gov.hmcts.juror.api.juror.notify;

public interface NotifyAdapter {
    EmailNotificationReceipt sendEmail(EmailNotification notification) throws NotifyApiException;

    EmailNotificationReceipt sendCommsEmail(EmailNotification notification);

    SmsNotificationReceipt sendCommsSms(SmsNotification notification);
}
