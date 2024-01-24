package uk.gov.hmcts.juror.api.bureau.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.domain.NotifyTemplateMapping;
import uk.gov.hmcts.juror.api.bureau.domain.NotifyTemplateMappingRepository;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.NotifyApiException;
import uk.gov.hmcts.juror.api.juror.notify.SmsNotification;

import java.util.Map;

@Service
@Slf4j
public class JurorCommsNotificationServiceImpl implements JurorCommsNotificationService {

    private final NotifyAdapter notifyAdapter;
    private final NotifyTemplateMappingRepository notifyTemplateMappingRepository;
    private final JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService;

    @Autowired
    public JurorCommsNotificationServiceImpl(final NotifyAdapter notifyAdapter,
                                             final NotifyTemplateMappingRepository notifyTemplateMappingRepository,
                                             final JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService) {
        Assert.notNull(notifyAdapter, "NotifyAdapter cannot be null");
        Assert.notNull(notifyTemplateMappingRepository, "NotifyTemplateMappingRepository cannot be null");
        Assert.notNull(jurorCommsNotifyPayLoadService, "JurorCommsNotifyPayLoadService cannot be null");
        this.notifyAdapter = notifyAdapter;
        this.notifyTemplateMappingRepository = notifyTemplateMappingRepository;
        this.jurorCommsNotifyPayLoadService = jurorCommsNotifyPayLoadService;
    }

    /**
     * Handles the identification of the templateId and calls the notify client to send the
     * notification comms to notify.
     *
     * @param poolDetails                  Response to send the notification for.
     * @param jurorCommsNotifyTemplateType Template type to use for the message
     * @param commsTemplateId              TemplateId provided for LETTER_COMMS
     * @param detailData                   additional data to establish payload for the message.
     * @param smsComms                     is a sms message to be sent.
     */
    @Override
    public void sendJurorComms(final Pool poolDetails, final JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                               final String commsTemplateId, final String detailData, final Boolean smsComms) {

        String templateId;
        Map<String, String> payLoad;
        if (jurorCommsNotifyTemplateType == JurorCommsNotifyTemplateType.LETTER_COMMS) {
            if (commsTemplateId == null || commsTemplateId.isEmpty()) {
                log.error("Missing templateId. Cannot send notify communication: ");
                throw new IllegalStateException("templateId null or empty");
            }
            templateId = commsTemplateId;
            if (detailData == null || detailData.isEmpty()) {
                log.error("Missing detailData. Cannot determine the payload for this notification.");
                throw new IllegalStateException("detailData null or empty");
            }
            log.trace("Inside sendJurorComms: calling generatePayLoadData.");
            payLoad = jurorCommsNotifyPayLoadService.generatePayLoadData(templateId, detailData, poolDetails);
        } else {
            final String templateKey = getTemplateKey(poolDetails, jurorCommsNotifyTemplateType, smsComms);
            // get template for given template key.
            log.debug(" template key obtained as {}", templateKey);
            NotifyTemplateMapping template = getTemplate(templateKey);
            if (template == null) {
                log.error("Missing Template. Cannot determine the template to use for this notification.");
                throw new IllegalStateException("Cannot find template");
            }
            templateId = template.getTemplateId();
            log.debug("Inside sendJurorComms: templateId obtained as : {}", templateId);
            //Deal with payload.
            payLoad = jurorCommsNotifyPayLoadService.generatePayLoadData(templateId, poolDetails);
        }

        log.trace("sendJurorComms- calling createEmailNotification");
        final EmailNotification emailNotification = createEmailNotification(poolDetails, jurorCommsNotifyTemplateType,
            templateId, payLoad
        );

        try {
            if (notifyAdapter.sendCommsEmail(emailNotification) == null) {
                throw new JurorCommsNotificationServiceException(
                    "Failed to Send Comms to Notify : " + poolDetails.getJurorNumber());
            }

        } catch (NotifyApiException nae) {
            log.warn("Failed to send to Notify service: {}", nae.getMessage());
            throw new JurorCommsNotificationServiceException("notifyApiAdapter failed to send", nae.getCause());
        } catch (Exception e) {
            log.error("Error sending notification! {}", e.getMessage());
        }

        log.info("Sent Juror Notify Comms.");
        if (log.isDebugEnabled()) {
            log.debug("Sent {}", emailNotification);
        }
    }


    /**
     * Handles the identification of the templateId and calls the notify client to send the
     * notification comms to notify.
     *
     * @param poolDetails                  Response to send the notification for.
     * @param jurorCommsNotifyTemplateType Template type to use for the message
     * @param commsTemplateId              TemplateId provided for LETTER_COMMS
     * @param detailData                   additional data to establish payload for the message.
     * @param smsComms                     is a sms message to be sent.
     */
    @Override
    public void sendJurorCommsSms(final Pool poolDetails,
                                  final JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                                  final String commsTemplateId, final String detailData, final Boolean smsComms) {

        String templateId;
        Map<String, String> payLoad;
        final String templateKey = getTemplateKey(poolDetails, jurorCommsNotifyTemplateType, smsComms);
        // get template for given template key.
        log.debug(" sms template key obtained as {}", templateKey);
        NotifyTemplateMapping template = getTemplate(templateKey);
        if (template == null) {
            log.error("Missing Template. Cannot determine the sms template to use for this notification.");
            throw new IllegalStateException("Cannot find template");
        }
        templateId = template.getTemplateId();
        log.debug("Inside sendJurorCommsSms: templateId obtained as : {}", templateId);
        //Deal with payload.
        payLoad = jurorCommsNotifyPayLoadService.generatePayLoadData(templateId, poolDetails);

        log.debug("sendJurorCommsSms - calling createSmsNotification");
        final SmsNotification smsNotification = createSmsNotification(poolDetails, jurorCommsNotifyTemplateType,
            templateId, payLoad
        );

        try {
            if (notifyAdapter.sendCommsSms(smsNotification) == null) {
                throw new JurorCommsNotificationServiceException(
                    "Failed to Send SMS Comms to Notify : " + poolDetails.getJurorNumber());
            }

        } catch (NotifyApiException nae) {
            log.warn("Failed to send SMS to Notify service: {}", nae.getMessage());
            throw new JurorCommsNotificationServiceException("notifyApiAdapter failed to send SMS", nae.getCause());
        } catch (Exception e) {
            log.error("Error sending SMS notification! {}", e.getMessage());
        }

        log.info("Sent Juror Notify SMS nComms.");
        if (log.isDebugEnabled()) {
            log.debug("Sent SMS {}", smsNotification);
        }
    }


    /**
     * Identify and return the templateId based on the paramters proivided.
     *
     * @param poolDetails                  pool details
     * @param jurorCommsNotifyTemplateType Type of Notify Comms ie weekly, sentToCourt
     * @param smsComms                     is sms required for this type of comms.
     * @return String - TemplateId.
     */
    private String getTemplateKey(Pool poolDetails, JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                                  Boolean smsComms) {
        try {
            Boolean isWelsh = Boolean.FALSE;

            if (poolDetails.getWelsh() != null) {
                isWelsh = jurorCommsNotifyPayLoadService.isWelshCourtAndComms(
                    poolDetails.getWelsh(),
                    jurorCommsNotifyPayLoadService.getWelshCourtLocation(poolDetails.getCourt().getLocCode())
                );
            }

            // covers TYPE 4 : informational weekly comms
            if (jurorCommsNotifyTemplateType == JurorCommsNotifyTemplateType.COMMS) {
                return jurorCommsNotifyTemplateType.getNotifyTemplateKey(
                    isWelsh,
                    poolDetails.getNotifications() + 1
                );
            } else { // covers TYPE 2, 3 : send to court comms
                return jurorCommsNotifyTemplateType.getNotifyTemplateKey(
                    isWelsh,
                    smsComms
                );
            }
        } catch (Exception e) {
            log.info(" Here throwing exception ........");
            throw new JurorCommsNotificationServiceException(e.getMessage(), e);
        }
    }

    /**
     * Obtain the correct Notify Template based on the Template Id (key).
     *
     * @param templateKey Notify template Id
     * @return NotifyTemplateMapping details for given template Id.
     */
    private NotifyTemplateMapping getTemplate(String templateKey) {
        return notifyTemplateMappingRepository.findByTemplateName(templateKey);
    }

    /**
     * Create a notification email payload from a juror response.
     *
     * @param poolDetails extract notification payload from
     * @return Email notification payload
     */
    @Override
    public EmailNotification createEmailNotification(final Pool poolDetails,
                                                     final JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                                                     final String templateId, final Map<String, String> payLoad) {
        try {
            log.debug("Creating 9wks juror comms email");
            final EmailNotification emailNotification = new EmailNotification(
                templateId,
                payLoad.get("email address"),
                payLoad
            );
            emailNotification.setReferenceNumber(poolDetails.getJurorNumber());
            return emailNotification;

        } catch (Exception e) {
            throw new JurorCommsNotificationServiceException(e.getMessage(), e);
        }
    }

    /**
     * Create a notification sms payload from a juror response.
     *
     * @param poolDetails extract notification payload from
     * @return Sms notification payload
     */
    @Override
    public SmsNotification createSmsNotification(final Pool poolDetails,
                                                 final JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                                                 final String templateId, final Map<String, String> payLoad) {
        try {
            log.debug("Creating 9wks juror comms sms");
            final SmsNotification smsNotification = new SmsNotification(
                templateId,
                payLoad.get("phone number"),
                payLoad
            );
            smsNotification.setReferenceNumber(poolDetails.getJurorNumber());
            return smsNotification;

        } catch (Exception e) {
            throw new JurorCommsNotificationServiceException(e.getMessage(), e);
        }
    }
}
