package uk.gov.hmcts.juror.api.bureau.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.NotifyApiException;
import uk.gov.hmcts.juror.api.juror.notify.SmsNotification;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateMappingMod;
import uk.gov.hmcts.juror.api.moj.repository.NotifyTemplateMappingRepositoryMod;

import java.util.Map;

@Service
@Slf4j
public class JurorCommsNotificationServiceImpl implements JurorCommsNotificationService {

    private final NotifyAdapter notifyAdapter;
    private final NotifyTemplateMappingRepositoryMod notifyTemplateMappingRepositoryMod;
    private final JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService;

    @Autowired
    public JurorCommsNotificationServiceImpl(
        final NotifyAdapter notifyAdapter,
        final NotifyTemplateMappingRepositoryMod notifyTemplateMappingRepositoryMod,
        final JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService) {

        Assert.notNull(notifyAdapter, "NotifyAdapter cannot be null");
        Assert.notNull(notifyTemplateMappingRepositoryMod, "NotifyTemplateMappingRepositoryMod cannot be null");
        Assert.notNull(jurorCommsNotifyPayLoadService, "JurorCommsNotifyPayLoadService cannot be null");
        this.notifyAdapter = notifyAdapter;
        this.notifyTemplateMappingRepositoryMod = notifyTemplateMappingRepositoryMod;
        this.jurorCommsNotifyPayLoadService = jurorCommsNotifyPayLoadService;
    }

    /**
     * Handles the identification of the templateId and calls the notify client to send the
     * notification comms to notify.
     *
     * @param jurorDetails                 Response to send the notification for.
     * @param jurorCommsNotifyTemplateType Template type to use for the message
     * @param commsTemplateId              TemplateId provided for LETTER_COMMS
     * @param detailData                   additional data to establish payload for the message.
     * @param smsComms                     is a sms message to be sent.
     */
    @Override
    public void sendJurorComms(
        final JurorPool jurorDetails,
        final JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
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
            payLoad = jurorCommsNotifyPayLoadService.generatePayLoadData(templateId, detailData, jurorDetails);
        } else {
            final String templateKey = getTemplateKey(jurorDetails, jurorCommsNotifyTemplateType, smsComms);
            // get template for given template key.
            log.debug(" template key obtained as {}", templateKey);
            NotifyTemplateMappingMod template = getTemplate(templateKey);
            if (template == null) {
                log.error("Missing Template. Cannot determine the template to use for this notification.");
                throw new IllegalStateException("Cannot find template");
            }
            templateId = template.getTemplateId();
            log.debug("Inside sendJurorComms: templateId obtained as : {}", templateId);
            //Deal with payload.
            payLoad = jurorCommsNotifyPayLoadService.generatePayLoadData(templateId, jurorDetails);
        }

        log.trace("sendJurorComms- calling createEmailNotification");
        final EmailNotification emailNotification = createEmailNotification(jurorDetails, jurorCommsNotifyTemplateType,
            templateId, payLoad
        );

        try {
            if (notifyAdapter.sendCommsEmail(emailNotification) == null) {
                throw new JurorCommsNotificationServiceException(
                    "Failed to Send Comms to Notify : " + jurorDetails.getJurorNumber());
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
     * @param jurorDetails                 Response to send the notification for.
     * @param jurorCommsNotifyTemplateType Template type to use for the message
     * @param commsTemplateId              TemplateId provided for LETTER_COMMS
     * @param detailData                   additional data to establish payload for the message.
     * @param smsComms                     is a sms message to be sent.
     */
    @Override
    public void sendJurorCommsSms(final JurorPool jurorDetails,
                                  final JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                                  final String commsTemplateId, final String detailData, final Boolean smsComms) {

        String templateId;
        final String templateKey = getTemplateKey(jurorDetails, jurorCommsNotifyTemplateType, smsComms);
        // get template for given template key.
        log.debug(" sms template key obtained as {}", templateKey);
        NotifyTemplateMappingMod template = getTemplate(templateKey);
        if (template == null) {
            log.error("Missing Template. Cannot determine the sms template to use for this notification.");
            throw new IllegalStateException("Cannot find template");
        }
        templateId = template.getTemplateId();
        log.debug("Inside sendJurorCommsSms: templateId obtained as : {}", templateId);
        //Deal with payload.
        Map<String, String> payLoad = jurorCommsNotifyPayLoadService.generatePayLoadData(templateId, jurorDetails);

        log.debug("sendJurorCommsSms - calling createSmsNotification");
        final SmsNotification smsNotification = createSmsNotification(jurorDetails, jurorCommsNotifyTemplateType,
            templateId, payLoad
        );

        try {
            if (notifyAdapter.sendCommsSms(smsNotification) == null) {
                throw new JurorCommsNotificationServiceException(
                    "Failed to Send SMS Comms to Notify : " + jurorDetails.getJurorNumber());
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
     * @param jurorDetails                 juror details
     * @param jurorCommsNotifyTemplateType Type of Notify Comms ie weekly, sentToCourt
     * @param smsComms                     is sms required for this type of comms.
     * @return String - TemplateId.
     */
    private String getTemplateKey(JurorPool jurorDetails, JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                                  Boolean smsComms) {
        try {
            Boolean isWelsh = Boolean.FALSE;

            if (jurorDetails.getJuror().getWelsh() != null) {
                isWelsh = jurorCommsNotifyPayLoadService.isWelshCourtAndComms(
                    jurorDetails.getJuror().getWelsh(),
                    jurorCommsNotifyPayLoadService.getWelshCourtLocation(jurorDetails.getCourt().getLocCode())
                );
            }

            // covers TYPE 4 : informational weekly comms
            if (jurorCommsNotifyTemplateType == JurorCommsNotifyTemplateType.COMMS
                ||
                jurorCommsNotifyTemplateType == JurorCommsNotifyTemplateType.TEMP_COMMS) {
                return jurorCommsNotifyTemplateType.getNotifyTemplateKey(
                    isWelsh,
                    jurorDetails.getJuror().getNotifications() + 1
                );
            } else if (jurorCommsNotifyTemplateType == JurorCommsNotifyTemplateType.SENT_TO_COURT
                ||
                jurorCommsNotifyTemplateType == JurorCommsNotifyTemplateType.SENT_TO_COURT_TEMP) {


                // covers TYPE 2, 3 : send to court comms
                return jurorCommsNotifyTemplateType.getNotifyTemplateKey(
                    isWelsh,
                    smsComms
                );
            } else {
                throw new JurorCommsNotificationServiceException("template type: " + jurorCommsNotifyTemplateType);
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
    private NotifyTemplateMappingMod getTemplate(String templateKey) {
        //return notifyTemplateMappingRepository.findByTemplateName(templateKey);
        return notifyTemplateMappingRepositoryMod.findByTemplateName(templateKey);
    }

    /**
     * Create a notification email payload from a juror response.
     *
     * @param jurorDetails extract notification payload from
     * @return Email notification payload
     */
    @Override
    public EmailNotification createEmailNotification(final JurorPool jurorDetails,
                                                     final JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                                                     final String templateId, final Map<String, String> payLoad) {
        try {
            log.debug("Creating 9wks juror comms email");
            final EmailNotification emailNotification = new EmailNotification(
                templateId,
                payLoad.get("email address"),
                payLoad
            );
            emailNotification.setReferenceNumber(jurorDetails.getJurorNumber());
            return emailNotification;

        } catch (Exception e) {
            throw new JurorCommsNotificationServiceException(e.getMessage(), e);
        }
    }

    /**
     * Create a notification sms payload from a juror response.
     *
     * @param jurorDetails extract notification payload from
     * @return Sms notification payload
     */
    @Override
    public SmsNotification createSmsNotification(final JurorPool jurorDetails,
                                                 final JurorCommsNotifyTemplateType jurorCommsNotifyTemplateType,
                                                 final String templateId, final Map<String, String> payLoad) {
        try {
            log.debug("Creating 9wks juror comms sms");
            final SmsNotification smsNotification = new SmsNotification(
                templateId,
                payLoad.get("phone number"),
                payLoad
            );
            smsNotification.setReferenceNumber(jurorDetails.getJurorNumber());
            return smsNotification;

        } catch (Exception e) {
            throw new JurorCommsNotificationServiceException(e.getMessage(), e);
        }
    }
}
