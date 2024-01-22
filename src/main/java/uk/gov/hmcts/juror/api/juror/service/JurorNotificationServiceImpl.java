package uk.gov.hmcts.juror.api.juror.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.domain.AppSetting;
import uk.gov.hmcts.juror.api.bureau.domain.AppSettingRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.NotifyApiException;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JurorNotificationServiceImpl implements JurorNotificationService {
    /**
     * Key applied to the juror number value in Notify template payload.
     */
    String KEY_JUROR_NUMBER = "jurorNumber";

    private final NotifyAdapter notifyAdapter;
    private final ResponseInspector responseInspector;
    private final AppSettingRepository appSettingRepository;

    @Autowired
    public JurorNotificationServiceImpl(final NotifyAdapter notifyAdapter,
                                        final ResponseInspector responseInspector,
                                        final AppSettingRepository appSettingRepository) {
        Assert.notNull(notifyAdapter, "NotifyAdapter cannot be null");
        Assert.notNull(responseInspector, "ResponseInspector cannot be null");
        Assert.notNull(appSettingRepository, "AppSettingRepository cannot be null");
        this.notifyAdapter = notifyAdapter;
        this.responseInspector = responseInspector;
        this.appSettingRepository = appSettingRepository;
    }


    @Override
    public void sendResponseReceipt(final JurorResponse jurorResponse, final NotifyTemplateType notifyTemplateType)
        throws NotificationServiceException {
        final EmailNotification emailNotification = createEmailNotification(jurorResponse, notifyTemplateType);

        try {
            notifyAdapter.sendEmail(emailNotification);
        } catch (NotifyApiException nae) {
            log.warn("Failed to send to Notify service: {}", nae.getMessage());
        } catch (Exception e) {
            log.error("Error sending notification! {}", e.getMessage());
        }

        log.info("Sent response receipt.");
        if (log.isDebugEnabled()) {
            log.debug("Sent {}", emailNotification);
        }
    }

    /**
     * Create a notification email payload from a juror response.
     *
     * @param jurorResponse juror response extract notification payload from
     * @return Email notification payload
     */
    @Override
    public EmailNotification createEmailNotification(final JurorResponse jurorResponse,
                                                     final NotifyTemplateType notifyTemplateType) {
        try {
            log.info("Creating response receipt email");
            final Map<String, String> payload = extractMessageValues(jurorResponse);

            final String templateKey = notifyTemplateType.getAppSettingKey(
                responseInspector.hasAdjustments(jurorResponse),
                responseInspector.isThirdPartyResponse(jurorResponse),
                responseInspector.isWelshCourt(jurorResponse),   //welsh response or not.
                responseInspector.isIneligible(jurorResponse)
            );
            log.trace("Creating mail using template {}", templateKey);
            //final AppSetting template = appSettingRepository.findOne(QAppSetting.appSetting.setting.eq(templateKey)
            // ).get();
            final AppSetting template = appSettingRepository.findById(templateKey).get();

            if (template != null) {
                final EmailNotification emailNotification = new EmailNotification(
                    template.getValue(),
                    responseInspector.activeContactEmail(jurorResponse),
                    payload
                );
                emailNotification.setReferenceNumber(jurorResponse.getJurorNumber());
                return emailNotification;
            } else {
                log.error("Missing Notify template in DB: {}", templateKey);
                throw new IllegalStateException("Template not found: " + templateKey);
            }
        } catch (Exception e) {
            throw new NotificationServiceException(e.getMessage(), e);
        }
    }


    /**
     * Extracts relevant data (names and juror number) from a juror response to a map of key/value pairs.
     *
     * @param jurorResponse Entity to extract message payload from.
     * @return Notification message payload
     * @since 2.0
     */
    private Map<String, String> extractMessageValues(final JurorResponse jurorResponse) {
        final Map<String, String> map = new HashMap<>();
        map.put(KEY_JUROR_NUMBER, jurorResponse.getJurorNumber());

        //strip null values
        map.values().removeIf(Objects::isNull);

        if (log.isTraceEnabled()) {
            log.trace("Added {} key/value pairs: {}", map.size(), map.entrySet().stream()
                .map(it -> it.getKey() + "=" + it.getValue())
                .collect(Collectors.joining(","))
            );
        }

        return Collections.unmodifiableMap(map);
    }
}
