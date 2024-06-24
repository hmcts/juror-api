package uk.gov.hmcts.juror.api.juror.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
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
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.SimpleDateFormat;
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
    private static final String MESSAGE_NOT_READ = "NR";
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
    public SchedulerServiceClient.Result process() {
        // Process court comms
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Court Comms Processing : STARTED- {}", dateFormat.format(new Date()));

        Proxy gotProxy = setUpConnection();

        log.debug("gotProxy {} ", gotProxy);

        // Hash map of regionID as key and notify region
        // keys as values

        Map<String, String> myRegionMap = new HashMap<>();
        @SuppressWarnings("PMD.VariableDeclarationUsageDistance")
        int errorCount = 0;

        for (int i = 0;
             i < setUpNotifyRegionKeys().size();
             i++) {
            myRegionMap.put(setUpRegionIds().get(i), setUpNotifyRegionKeys().get(i));
        }

        log.debug("Display myRegionMap {}", myRegionMap);

        String welshTempComparisonText = appSetting.getWelshTranslation();

        BooleanExpression unreadMessagesFilter = MessageQueries.messageReadStatus();

        final List<Message> messageDetailList = Lists.newLinkedList(messageRepository.findAll(unreadMessagesFilter));

        log.info("messageDetailList Number of records to process : {}", messageDetailList.size());


        Map<String, TemplateDetails> templateDetailsMap = new HashMap<>();


        for (Message messagesDetail : messageDetailList) {
            log.info("messagesDetail  PART_NO : {}", messagesDetail.getJurorNumber());

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


                //     String apiKey = (messagesDetail != null ? messagesDetail.getLocationCode().getCourtRegion()
                //     .getNotifyAccountKey() : null);
                final String regionId = messagesDetail.getLocationCode().getCourtRegion().getRegionId();
                final String regionApikey = myRegionMap.get(regionId);
                log.debug("regionApikey {} ", regionApikey);


                if (regionApikey == null || regionApikey.isEmpty()) {
                    log.error("Missing Notify Api Account key Cannot send notify communication: ");
                    log.info("Missing Notify Api Account key Cannot send notify communication: ");
                    messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
                    updateMessageFlag(messagesDetail);

                    continue;
                }

                Map<String, String> personalisation = new HashMap<>();
                personalisation.put(MESSAGE_PLACEHOLDER_MESSAGE, textMessage);
                personalisation.put(MESSAGE_PLACEHOLDER_JUROR, jurorNumber);


                final boolean isEmail = messagesDetail.getEmail() != null;
                final boolean isPhone = messagesDetail.getPhone() != null;

                if (!isEmail && !isPhone) {
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
                    } else if (isPhone) {
                        SendSmsResponse smsResponse =
                            notifyClient.sendSms(smsTemplateId, phoneNumber, personalisation, reference);
                        response = smsResponse;
                        notificationId = smsResponse.getNotificationId();
                    }

                    if (notificationId != null) {
                        messagesDetail.setMessageRead(MESSAGE_READ);
                        updateMessageFlag(messagesDetail);
                    }
                    log.trace("Court Comms  sms messaging  Service :  response {}", response);
                }
            } catch (NotificationClientException e) {
                log.error("Failed to send via Notify", e);
                messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
                updateMessageFlag(messagesDetail);
                errorCount++;
            } catch (Exception e) {
                log.error("Unexpected exception when sending details to notify", e);
                errorCount++;
            }
        }

        log.info("Court Comms Processing : Finished - {}", dateFormat.format(new Date()));
        return new SchedulerServiceClient.Result(
            errorCount == 0
                ? SchedulerServiceClient.Result.Status.SUCCESS
                : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS, null,
            Map.of("ERROR_COUNT", "" + errorCount));
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

    private void updateMessageFlag(Message messagesDetail) {
        messageRepository.save(messagesDetail);
    }

    public List<String> setUpNotifyRegionKeys() {
        List<String> notifyRegionKeys;
        notifyRegionKeys = notifyRegionsConfigurationProperties.getRegionKeys();
        return notifyRegionKeys;
    }

    public List<String> setUpRegionIds() {
        List<CourtRegionMod> courtRegions = Lists.newLinkedList(courtRegionModRepository.findAll());
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



