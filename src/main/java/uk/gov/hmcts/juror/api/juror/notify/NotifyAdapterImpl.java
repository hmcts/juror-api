package uk.gov.hmcts.juror.api.juror.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.config.NotifyConfigurationProperties;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

import java.util.function.Function;

@Component
@Slf4j
public class NotifyAdapterImpl implements NotifyAdapter {
    private final NotifyConfigurationProperties notifyProperties;
    private final NotificationClientApi notifyClient;

    private static final String MESSAGE_1 = "Notify send is disabled? {}";
    private static final String MESSAGE_2 = "Notify http response code: {}";
    private static final String MESSAGE_3 = "Unexpected exception: {}";

    /**
     * Messages and failure behaviour for each kind of send.
     *
     * @param nullNotificationMessage exception message when the notification is null
     * @param disabledWarning         warning logged when Notify sending is disabled
     * @param sendingMessage          debug message logged before sending
     * @param outgoingMessage         trace message (with notification) logged before sending
     * @param respondedMessage        trace message (with response) logged after sending
     * @param nullResponseMessage     warning logged when Notify returns null
     * @param failureMessage          message used when Notify rejects the request
     * @param rethrowFailures         true to throw {@link NotifyApiException} on a Notify failure,
     *                                false to log it and return null
     */
    private record Channel(String nullNotificationMessage,
                           String disabledWarning,
                           String sendingMessage,
                           String outgoingMessage,
                           String respondedMessage,
                           String nullResponseMessage,
                           String failureMessage,
                           boolean rethrowFailures) {
    }

    private static final Channel EMAIL = new Channel(
        "EmailNotification cannot be null",
        "Dummy email notification - logging instead of sending to Notify!",
        "Sending via notify",
        "Outgoing message: {}",
        "Notify responded: {}",
        "Notify response was null!",
        "Failed to send via Notify: {}",
        false
    );

    private static final Channel COMMS_EMAIL = new Channel(
        "EmailNotification cannot be null",
        "Dummy email notification - logging instead of sending to Notify!",
        "Sending Juror Comms via notify",
        "Juror Comms Outgoing message: {}",
        "Juror Comms Notify responded: {}",
        "Juror Comms Notify response was null!",
        "Failed to send Juror Comms via Notify: {}",
        true
    );

    private static final Channel COMMS_SMS = new Channel(
        "SmsNotification cannot be null",
        "Dummy SMS notification - logging instead of sending to Notify!",
        "Sending Juror Comms SMS via notify",
        "Juror Comms SMS Outgoing message: {}",
        "Juror Comms SMS Notify responded: {}",
        "Juror Comms SMS Notify response was null!",
        "Failed to send Juror Comms SMS via Notify: {}",
        true
    );

    /**
     * A call to the Notify client that may be rejected by Notify.
     *
     * @param <R> Notify response type
     */
    @FunctionalInterface
    private interface NotifyCall<R> {
        R send() throws NotificationClientException;
    }

    @Autowired
    public NotifyAdapterImpl(final NotifyConfigurationProperties notifyProperties,
                             final NotificationClientApi notifyClient) {
        Assert.notNull(notifyProperties, "NotifyConfigurationProperties cannot be null");
        Assert.notNull(notifyClient, "NotificationClient cannot be null");

        this.notifyProperties = notifyProperties;
        this.notifyClient = notifyClient;
    }

    /**
     * Sends email.
     * @param notification notification to send.
     * @return notification receipt.
     * @throws NotifyApiException if the notification is null.
     */
    @Override
    public EmailNotificationReceipt sendEmail(final EmailNotification notification) {
        return send(notification, EMAIL,
                    () -> notifyClient.sendEmail(
                        notification.getTemplateId(),
                        notification.getRecipientEmail(),
                        notification.getPayload(),
                        notification.getReferenceNumber()
                    ),
                    EmailNotificationReceipt::new
        );
    }

    /**
     * Sends Comms Email.
     * @param notification notification to send.
     * @return notification receipt.
     * @throws NotifyApiException if Notify rejects the request.
     */
    @Override
    public EmailNotificationReceipt sendCommsEmail(final EmailNotification notification) {
        return send(notification, COMMS_EMAIL,
                    () -> notifyClient.sendEmail(
                        notification.getTemplateId(),
                        notification.getRecipientEmail(),
                        notification.getPayload(),
                        notification.getReferenceNumber()
                    ),
                    EmailNotificationReceipt::new
        );
    }

    /**
     * Sends Comms SMS.
     * @param notification notification to send.
     * @return notification receipt.
     * @throws NotifyApiException if Notify rejects the request.
     */
    @Override
    public SmsNotificationReceipt sendCommsSms(final SmsNotification notification) {
        return send(notification, COMMS_SMS,
                    () -> notifyClient.sendSms(
                        notification.getTemplateId(),
                        notification.getRecipientPhoneNumber(),
                        notification.getPayload(),
                        notification.getReferenceNumber()
                    ),
                    SmsNotificationReceipt::new
        );
    }

    /**
     * Sends a notification via Notify, or just logs it when sending is disabled.
     *
     * @param notification notification to send (checked for null before {@code call} is invoked)
     * @param channel      messages and failure behaviour for this kind of send
     * @param call         the Notify client call
     * @param toReceipt    converts a non-null Notify response into a receipt
     * @param <R>          Notify response type
     * @param <T>          receipt type
     * @return the receipt, or null if sending is disabled, Notify returned null, or the send failed
     *         without being rethrown
     * @throws NotifyApiException if the notification is null, or Notify rejects the request and
     *                            the channel rethrows failures
     */
    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.CyclomaticComplexity"})
    private <R, T> T send(final Object notification, final Channel channel,
                          final NotifyCall<R> call, final Function<R, T> toReceipt) {
        if (log.isDebugEnabled()) {
            log.debug(MESSAGE_1, notifyProperties.isDisabled());
        }

        if (notification == null) {
            throw new NotifyApiException(channel.nullNotificationMessage());
        }

        if (notifyProperties.isDisabled()) {
            //just log
            log.warn(channel.disabledWarning());
            log.debug("{}", notification);
            return null;
        }

        try {
            log.debug(channel.sendingMessage());
            if (log.isTraceEnabled()) {
                log.trace(channel.outgoingMessage(), notification);
            }
            final R response = call.send();
            if (log.isTraceEnabled()) {
                log.trace(channel.respondedMessage(), response);
            }

            if (response != null) {
                return toReceipt.apply(response);
            }
            log.warn(channel.nullResponseMessage());
        } catch (NotificationClientException e) {
            if (channel.rethrowFailures()) {
                log.trace(MESSAGE_2, e.getHttpResult());
                throw new NotifyApiException(channel.failureMessage(), e);
            }
            log.error(channel.failureMessage(), e);
            log.trace(MESSAGE_2, e.getHttpResult());
        } catch (Exception e) {
            log.error(MESSAGE_3, e);
        }
        return null;
    }
}
