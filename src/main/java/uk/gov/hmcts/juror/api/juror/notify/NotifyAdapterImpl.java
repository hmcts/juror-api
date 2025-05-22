package uk.gov.hmcts.juror.api.juror.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.config.NotifyConfigurationProperties;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

@Component
@Slf4j
public class NotifyAdapterImpl implements NotifyAdapter {
    private final NotifyConfigurationProperties notifyProperties;
    private final NotificationClientApi notifyClient;

    private static final String MESSAGE_1 = "Notify send is disabled? {}";
    private static final String MESSAGE_2 = "Notify http response code: {}";
    private static final String MESSAGE_3 = "Unexpected exception: {}";

    @Autowired
    public NotifyAdapterImpl(final NotifyConfigurationProperties notifyProperties,
                             final NotificationClientApi notifyClient) {
        Assert.notNull(notifyProperties, "NotifyConfigurationProperties cannot be null");
        Assert.notNull(notifyClient, "NotificationClient cannot be null");

        this.notifyProperties = notifyProperties;
        this.notifyClient = notifyClient;
    }

    @Override
    public EmailNotificationReceipt sendEmail(final EmailNotification notification) throws NotifyApiException {
        if (log.isDebugEnabled()) {
            log.debug(MESSAGE_1, notifyProperties.isDisabled());
        }

        if (notification == null) {
            throw new NotifyApiException("EmailNotification cannot be null");
        }

        if (!notifyProperties.isDisabled()) {
            //send email
            try {
                log.debug("Sending via notify");
                if (log.isTraceEnabled()) {
                    log.trace("Outgoing message: {}", notification);
                }
                final SendEmailResponse sendEmailResponse = notifyClient.sendEmail(
                    notification.getTemplateId(),
                    notification.getRecipientEmail(),
                    notification.getPayload(),
                    notification.getReferenceNumber()
                );
                if (log.isTraceEnabled()) {
                    log.trace("Notify responded: {}", sendEmailResponse);
                }

                if (sendEmailResponse != null) {
                    return new EmailNotificationReceipt(sendEmailResponse);
                } else {
                    log.warn("Notify response was null!");
                }
            } catch (NotificationClientException e) {
                log.error("Failed to send via Notify: {}", e);
                log.trace(MESSAGE_2, e.getHttpResult());
            } catch (Exception e) {
                log.error(MESSAGE_3, e);
            }
        } else {
            //just log
            log.warn("Dummy email notification - logging instead of sending to Notify!");
            log.debug("{}", notification);
        }
        return null;
    }


    @Override
    public EmailNotificationReceipt sendCommsEmail(final EmailNotification notification) throws NotifyApiException {
        if (log.isDebugEnabled()) {
            log.debug(MESSAGE_1, notifyProperties.isDisabled());
        }

        if (notification == null) {
            throw new NotifyApiException("EmailNotification cannot be null");
        }

        if (!notifyProperties.isDisabled()) {
            //send email
            try {
                log.debug("Sending Juror Comms via notify");
                if (log.isTraceEnabled()) {
                    log.trace("Juror Comms Outgoing message: {}", notification);
                }
                final SendEmailResponse sendEmailResponse = notifyClient.sendEmail(
                    notification.getTemplateId(),
                    notification.getRecipientEmail(),
                    notification.getPayload(),
                    notification.getReferenceNumber()
                );
                if (log.isTraceEnabled()) {
                    log.trace("Juror Comms Notify responded: {}", sendEmailResponse);
                }

                if (sendEmailResponse != null) {
                    return new EmailNotificationReceipt(sendEmailResponse);
                } else {
                    log.warn("Juror Comms Notify response was null!");
                }
            } catch (NotificationClientException e) {
                log.trace(MESSAGE_2, e.getHttpResult());
                throw new NotifyApiException("Failed to send Juror Comms via Notify: {}", e);
            } catch (Exception e) {
                log.error(MESSAGE_3, e);
            }
        } else {
            //just log
            log.warn("Dummy email notification - logging instead of sending to Notify!");
            log.debug("{}", notification);
        }
        return null;
    }

    @Override
    public SmsNotificationReceipt sendCommsSms(final SmsNotification notification) throws NotifyApiException {
        if (log.isDebugEnabled()) {
            log.debug(MESSAGE_1, notifyProperties.isDisabled());
        }

        if (notification == null) {
            throw new NotifyApiException("SmsNotification cannot be null");
        }

        if (!notifyProperties.isDisabled()) {
            //send SMS
            try {
                log.debug("Sending Juror Comms SMS via notify");
                if (log.isTraceEnabled()) {
                    log.trace("Juror Comms SMS Outgoing message: {}", notification);
                }
                final SendSmsResponse sendSmsResponse = notifyClient.sendSms(
                    notification.getTemplateId(),
                    notification.getRecipientPhoneNumber(),
                    notification.getPayload(),
                    notification.getReferenceNumber()
                );
                if (log.isTraceEnabled()) {
                    log.trace("Juror Comms SMS Notify responded: {}", sendSmsResponse);
                }

                if (sendSmsResponse != null) {
                    return new SmsNotificationReceipt(sendSmsResponse);
                } else {
                    log.warn("Juror Comms SMS Notify response was null!");
                }
            } catch (NotificationClientException e) {
                log.trace(MESSAGE_2, e.getHttpResult());
                throw new NotifyApiException("Failed to send Juror Comms SMS via Notify: {}", e);
            } catch (Exception e) {
                log.error(MESSAGE_3, e);
            }
        } else {
            //just log
            log.warn("Dummy SMS notification - logging instead of sending to Notify!");
            log.debug("{}", notification);
        }
        return null;
    }

}
