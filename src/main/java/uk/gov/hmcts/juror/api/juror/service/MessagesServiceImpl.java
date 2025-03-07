package uk.gov.hmcts.juror.api.juror.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.service.BureauProcessService;
import uk.gov.hmcts.juror.api.config.NotifyConfigurationProperties;
import uk.gov.hmcts.juror.api.config.NotifyRegionsConfigurationProperties;
import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;
import uk.gov.hmcts.juror.api.moj.domain.CourtRegionMod;
import uk.gov.hmcts.juror.api.moj.domain.RegionNotifyTemplateMod;
import uk.gov.hmcts.juror.api.moj.domain.messages.Message;
import uk.gov.hmcts.juror.api.moj.repository.CourtRegionModRepository;
import uk.gov.hmcts.juror.api.moj.repository.MessageQueries;
import uk.gov.hmcts.juror.api.moj.repository.MessageRepository;
import uk.gov.hmcts.juror.api.moj.repository.RegionNotifyTemplateQueriesMod;
import uk.gov.hmcts.juror.api.moj.repository.RegionNotifyTemplateRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;
import uk.gov.hmcts.juror.api.moj.utils.NotifyUtil;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessagesServiceImpl implements BureauProcessService {
    private static final String MESSAGE_PLACEHOLDER_MESSAGE = "MESSAGETEXT";
    private static final String MESSAGE_PLACEHOLDER_JUROR = "JURORNUMBER";
    private static final String MESSAGE_READ = "SN";
    private static final String MESSAGE_READ_APP_ERROR = "NS";
    private static final int CHECK_NUM = 1;

    private static final String LOG_ERROR_MESSAGE_TEMPLATE_ID = " Missing templateId. Cannot send notify "
        + "communication:";
    private final AppSettingService appSetting;
    private final MessageRepository messageRepository;
    private final CourtRegionModRepository courtRegionModRepository;
    private final RegionNotifyTemplateRepositoryMod regionNotifyTemplateRepositoryMod;
    private final NotifyConfigurationProperties notifyConfigurationProperties;
    private final NotifyRegionsConfigurationProperties notifyRegionsConfigurationProperties;
    private Proxy proxy;

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror.messages table and sends the appropriate email notifications to
     * the juror
     */
    @Override
    @Transactional
    @SuppressWarnings("checkstyle:LineLength") // false positive
    public SchedulerServiceClient.Result process() {
        // Process court comms
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Court Comms Processing : STARTED- {}", dateFormat.format(new Date()));

        Proxy gotProxy = setUpConnection();

        log.debug("gotProxy {} ", gotProxy);

        // Hash map of regionID as key and notify region
        // keys as values

        Map<String, String> myRegionMap = new HashMap<>();

        List<String> notifyRegionKeys = setUpNotifyRegionKeys();
        List<String> regionIds = setUpRegionIds();
        for (int i = 0; i < notifyRegionKeys.size(); i++) {
            myRegionMap.put(regionIds.get(i), notifyRegionKeys.get(i));
        }

        log.debug("Display myRegionMap {}", myRegionMap);

        String welshTempComparisonText = appSetting.getWelshTranslation();

        BooleanExpression unreadMessagesFilter = MessageQueries.messageReadStatus();

        final List<Message> messageDetailList = Lists.newLinkedList(messageRepository.findAll(unreadMessagesFilter));

        log.info("messageDetailList Number of records to process : {}", messageDetailList.size());


        Map<String, TemplateDetails> templateDetailsMap = new HashMap<>();

        int errorCount = 0;
        int invalidPhoneCount = 0;
        int invalidEmailCount = 0;
        int missingApiKeyCount = 0;
        int missingEmailAndPhone = 0;
        int emailSuccess = 0;
        int smsSuccess = 0;

        for (Message messagesDetail : messageDetailList) {
            log.info("messagesDetail  Juror number : {}", messagesDetail.getJurorNumber());

            try {
                String templateDetailsKey = messagesDetail.getMessageId()
                    + "--"
                    + messagesDetail.getLocationCode().getCourtRegion().getRegionId();
                TemplateDetails templateDetails = templateDetailsMap.get(templateDetailsKey);
                if (templateDetails == null) {
                    templateDetails = createTemplateDetails(messagesDetail);
                    templateDetailsMap.put(templateDetailsKey, templateDetails);
                }
                templateCheck(messagesDetail, templateDetails);


                final String jurorNumber = messagesDetail.getJurorNumber();
                final String phoneNumber = messagesDetail.getPhone();
                final String email = messagesDetail.getEmail();
                final String textMessage = messagesDetail.getMessageText();
                final String reference = (messagesDetail.getJurorNumber());

                final String regionId = messagesDetail.getLocationCode().getCourtRegion().getRegionId();
                final String regionApikey = myRegionMap.get(regionId);
                log.debug("regionApikey {} ", regionApikey);


                if (regionApikey == null || regionApikey.isEmpty()) {
                    log.error("Missing Notify Api Account key Cannot send notify communication. RegionId: {}",
                        regionId);
                    messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
                    updateMessageFlag(messagesDetail);
                    missingApiKeyCount++;
                    continue;
                }

                Map<String, String> personalisation = new HashMap<>();
                personalisation.put(MESSAGE_PLACEHOLDER_MESSAGE, textMessage);
                personalisation.put(MESSAGE_PLACEHOLDER_JUROR, jurorNumber);


                final boolean isEmail = StringUtils.isNotBlank(email);
                final boolean isPhone = StringUtils.isNotBlank(phoneNumber);

                if (!isEmail && !isPhone) {
                    messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
                    updateMessageFlag(messagesDetail);
                    missingEmailAndPhone++;
                    continue;
                }

                List<RegionNotifyTemplateMod> regionNotifyTemplateMods;
                if (Objects.equals(welshTempComparisonText, messagesDetail.getSubject())) {
                    regionNotifyTemplateMods = isEmail
                        ? templateDetails.getRegionNotifyTemplateListEmailWelsh()
                        : templateDetails.getRegionNotifyTemplateListSmsWelsh();
                } else {
                    regionNotifyTemplateMods = isEmail
                        ? templateDetails.getRegionNotifyTemplateListEmail()
                        : templateDetails.getRegionNotifyTemplateListSms();
                }

                NotificationClient notifyClient = new NotificationClient(regionApikey, gotProxy);
                for (RegionNotifyTemplateMod regionNotifyTemplateSms : regionNotifyTemplateMods) {

                    String smsTemplateId = regionNotifyTemplateSms != null
                        ? regionNotifyTemplateSms.getNotifyTemplateId()
                        : null;

                    if (smsTemplateId == null || smsTemplateId.isEmpty()) {
                        messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
                        updateMessageFlag(messagesDetail);
                        log.error(LOG_ERROR_MESSAGE_TEMPLATE_ID);
                        throw new IllegalStateException("smsTemplateId null or empty");
                    }
                    UUID notificationId = null;
                    Object response = null;
                    if (isEmail) {
                        SendEmailResponse emailResponse = notifyClient.sendEmail(
                            smsTemplateId, email, personalisation, reference);
                        response = emailResponse;
                        notificationId = emailResponse.getNotificationId();
                        emailSuccess++;
                    } else if (isPhone) {
                        SendSmsResponse smsResponse =
                            notifyClient.sendSms(smsTemplateId, phoneNumber, personalisation, reference);
                        response = smsResponse;
                        notificationId = smsResponse.getNotificationId();
                        smsSuccess++;
                    }

                    if (notificationId != null) {
                        messagesDetail.setMessageRead(MESSAGE_READ);
                        updateMessageFlag(messagesDetail);
                    }
                    log.trace("Court Comms  sms messaging  Service :  response {}", response);
                }
            } catch (NotificationClientException e) {
                log.info("Failed to send via Notify - {}", e.getMessage());
                messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
                updateMessageFlag(messagesDetail);
                if (NotifyUtil.isInvalidPhoneNumberError(e)) {
                    invalidPhoneCount++;
                } else if (NotifyUtil.isInvalidEmailAddressError(e)) {
                    invalidEmailCount++;
                } else {
                    log.error("Failed to send via Notify", e);
                    errorCount++;
                }
            } catch (Exception e) {
                log.error("Unexpected exception when sending details to notify", e);
                errorCount++;
            }
        }

        SchedulerServiceClient.Result.Status status = errorCount == 0
            ? SchedulerServiceClient.Result.Status.SUCCESS
            : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS;

        // log the results for Dynatrace
        log.info(
            "[JobKey: CRONBATCH_COURT_COMMS]\n[{}]\nresult={},\nmetadata={total_messages_to_send={},emails_sent={},sms_sent={},invalid_phone_count={},invalid_email_count={},error_count={},missing_api_key_count={},missing_email_and_phone={}}",
            DATE_TIME_FORMATTER.format(LocalDateTime.now()),
            status,
            messageDetailList.size(),
            emailSuccess,
            smsSuccess,
            invalidPhoneCount,
            invalidEmailCount,
            errorCount,
            missingApiKeyCount,
            missingEmailAndPhone
        );

        log.info("Court Comms Processing : Finished - {}", dateFormat.format(new Date()));
        return new SchedulerServiceClient.Result(
            status, null,
            Map.of(
                "TOTAL_MESSAGES_TO_SEND", String.valueOf(messageDetailList.size()),
                "ERROR_COUNT", String.valueOf(errorCount),
                "MISSING_API_KEY_COUNT", String.valueOf(missingApiKeyCount),
                "MISSING_EMAIL_AND_PHONE", String.valueOf(missingEmailAndPhone),
                "EMAIL_SUCCESS", String.valueOf(emailSuccess),
                "SMS_SUCCESS", String.valueOf(smsSuccess),
                "INVALID_PHONE_COUNT", String.valueOf(invalidPhoneCount),
                "INVALID_EMAIL_COUNT", String.valueOf(invalidEmailCount)
            ));
    }

    private TemplateDetails createTemplateDetails(Message messagesDetail) {
        TemplateDetails templateDetails = new TemplateDetails();
        CourtRegionMod courtRegionMod = messagesDetail.getLocationCode().getCourtRegion();

        templateDetails.setRegionNotifyTemplateListEmail(Lists.newLinkedList(
            regionNotifyTemplateRepositoryMod.findAll(
                RegionNotifyTemplateQueriesMod.regionNotifyTemplateByIdAndEmail(
                    courtRegionMod.getRegionId(),
                    messagesDetail.getMessageId()
                ))));
        templateDetails.setRegionNotifyTemplateListSms(Lists.newLinkedList(
            regionNotifyTemplateRepositoryMod.findAll(
                RegionNotifyTemplateQueriesMod.regionNotifyTemplateByIdAndSms(
                    courtRegionMod.getRegionId(),
                    messagesDetail.getMessageId()
                ))));
        templateDetails.setRegionNotifyTemplateListSmsWelsh(Lists.newLinkedList(
            regionNotifyTemplateRepositoryMod.findAll(
                RegionNotifyTemplateQueriesMod.regionNotifyTemplateByIdAndSmsWelsh(
                    courtRegionMod.getRegionId(),
                    messagesDetail.getMessageId()
                ))));
        templateDetails.setRegionNotifyTemplateListEmailWelsh(Lists.newLinkedList(
            regionNotifyTemplateRepositoryMod.findAll(
                RegionNotifyTemplateQueriesMod.regionNotifyTemplateByIdAndEmailWelsh(
                    courtRegionMod.getRegionId(),
                    messagesDetail.getMessageId()
                ))));
        return templateDetails;
    }


    private void templateCheck(Message messagesDetail, TemplateDetails templateDetails) {
        final int missingEmailTemplateCheck = templateDetails.getRegionNotifyTemplateListEmail().size();
        final int missingSmsTemplateCheck = templateDetails.getRegionNotifyTemplateListSms().size();
        final int missingWelshEmailTemplateCheck = templateDetails.getRegionNotifyTemplateListEmailWelsh().size();
        final int missingWelshSmsTemplateCheck = templateDetails.getRegionNotifyTemplateListSmsWelsh().size();

        if (missingEmailTemplateCheck < CHECK_NUM) {
            messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
            updateMessageFlag(messagesDetail);
        }
        if (missingSmsTemplateCheck < CHECK_NUM) {
            messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
            updateMessageFlag(messagesDetail);
        }

        if (missingWelshEmailTemplateCheck < CHECK_NUM) {
            messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
            updateMessageFlag(messagesDetail);
        }

        if (missingWelshSmsTemplateCheck < CHECK_NUM) {
            messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
            updateMessageFlag(messagesDetail);
        }

    }

    @SuppressWarnings("PMD.LinguisticNaming")
    public Proxy setUpConnection() {
        final NotifyConfigurationProperties.Proxy setUpProxy = notifyConfigurationProperties.getProxy();
        if (setUpProxy != null && setUpProxy.isEnabled()) {
            String setupHost = setUpProxy.getHost();
            int setupPort = Integer.parseInt(setUpProxy.getPort());
            Proxy.Type setupType = setUpProxy.getType();
            final InetSocketAddress socketAddress = new InetSocketAddress(setupHost, setupPort);
            proxy = new Proxy(setupType, socketAddress);
        }

        return proxy;
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    private void updateMessageFlag(Message messagesDetail) {
        messageRepository.save(messagesDetail);
    }

    public List<String> setUpNotifyRegionKeys() {
        List<String> notifyRegionKeys;
        notifyRegionKeys = notifyRegionsConfigurationProperties.getRegionKeys();
        return notifyRegionKeys;
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    public List<String> setUpRegionIds() {
        List<CourtRegionMod> courtRegions = courtRegionModRepository.findAllByOrderByRegionIdAsc();
        List<String> regionIds = new ArrayList<>();

        for (CourtRegionMod courtRegion : courtRegions) {
            regionIds.add(courtRegion.getRegionId());
            log.debug("CourtRegions {}", courtRegion.getRegionId());
        }

        return regionIds;
    }

    @Data
    static class TemplateDetails {
        private List<RegionNotifyTemplateMod> regionNotifyTemplateListEmail;
        private List<RegionNotifyTemplateMod> regionNotifyTemplateListSms;
        private List<RegionNotifyTemplateMod> regionNotifyTemplateListSmsWelsh;
        private List<RegionNotifyTemplateMod> regionNotifyTemplateListEmailWelsh;
    }
}



