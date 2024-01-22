package uk.gov.hmcts.juror.api.juror.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.bureau.service.AppSettingService;
import uk.gov.hmcts.juror.api.bureau.service.BureauProcessService;
import uk.gov.hmcts.juror.api.config.NotifyConfigurationProperties;
import uk.gov.hmcts.juror.api.config.NotifyRegionsConfigurationProperties;
import uk.gov.hmcts.juror.api.juror.domain.CourtRegion;
import uk.gov.hmcts.juror.api.juror.domain.CourtRegionRepository;
import uk.gov.hmcts.juror.api.juror.domain.MessageStagingRepository;
import uk.gov.hmcts.juror.api.juror.domain.Messages;
import uk.gov.hmcts.juror.api.juror.domain.MessagesQueries;
import uk.gov.hmcts.juror.api.juror.domain.MessagesRepository;
import uk.gov.hmcts.juror.api.juror.domain.RegionNotifyTemplate;
import uk.gov.hmcts.juror.api.juror.domain.RegionNotifyTemplateQueries;
import uk.gov.hmcts.juror.api.juror.domain.RegionNotifyTemplateRepository;
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

@Service
@Slf4j
public class MessagesServiceImpl implements BureauProcessService {
    private final static String MESSAGE_PLACEHOLDER_MESSAGE = "MESSAGETEXT";
    private final static String MESSAGE_PLACEHOLDER_JUROR = "JURORNUMBER";
    private final static String MESSAGE_READ = "SN";
    private final static String MESSAGE_READ_APP_ERROR = "NS";
    private final static String MESSAGE_NOT_READ = "NR";
    private final static int CHECK_NUM = 1;

    private final static String LOG_ERROR_MESSAGE_TEMPLATE_ID = " Missing templateId. Cannot send notify "
        + "communication:";
    private final AppSettingService appSetting;
    private final MessageStagingRepository messageStagingRepository;
    private final MessagesRepository messagesRepository;
    private final CourtRegionRepository courtRegionRepository;
    private final RegionNotifyTemplateRepository regionNotifyTemplateRepository;
    private final NotifyConfigurationProperties notifyConfigurationProperties;
    private final NotifyRegionsConfigurationProperties notifyRegionsConfigurationProperties;
    private Proxy proxy;


    @Autowired
    public MessagesServiceImpl(

        final AppSettingService appSetting,
        final MessageStagingRepository messageStagingRepository,
        final MessagesRepository messagesRepository,
        final CourtRegionRepository courtRegionRepository,
        final NotifyConfigurationProperties notifyConfigurationProperties,
        final NotifyRegionsConfigurationProperties notifyRegionsConfigurationProperties,
        final RegionNotifyTemplateRepository regionNotifyTemplateRepository) {
        Assert.notNull(appSetting, "AppSettingService cannot be null.");
        Assert.notNull(messageStagingRepository, "messageStagingRepository can not be null.");
        Assert.notNull(messagesRepository, "MessagesRepository can not be null.");
        Assert.notNull(courtRegionRepository, "CourtRegionRepository can not be null.");
        Assert.notNull(notifyConfigurationProperties, "NotifyConfigurationProperties can not be null.");
        Assert.notNull(notifyRegionsConfigurationProperties, "NotifyRegionsConfigurationProperties can not be null.");
        Assert.notNull(regionNotifyTemplateRepository, "RegionNotifyTemplateRepository can not be null.");
        this.appSetting = appSetting;
        this.messageStagingRepository = messageStagingRepository;
        this.messagesRepository = messagesRepository;
        this.courtRegionRepository = courtRegionRepository;
        this.notifyConfigurationProperties = notifyConfigurationProperties;
        this.notifyRegionsConfigurationProperties = notifyRegionsConfigurationProperties;
        this.regionNotifyTemplateRepository = regionNotifyTemplateRepository;
    }

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror.messages table and sends the appropriate email notifications to
     * the juror
     */
    @Override
    @Transactional
    public void process() {
        // Process court comms
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Court Comms Processing : STARTED- {}", dateFormat.format(new Date()));

        Proxy gotProxy = setUpConnection();

        log.debug("gotProxy {} ", gotProxy);

        // Hash map of regionID as key and notify region
        // keys as values

        Map<String, String> myRegionMap = new HashMap<>();


        for (int i = 0;
             i < setUpNotifyRegionKeys().size();
             i++) {
            myRegionMap.put(setUpRegionIds().get(i), setUpNotifyRegionKeys().get(i));
        }

        log.debug("Display myRegionMap {}", myRegionMap);

        String welshTempComparisonText = appSetting.getWelshTranslation();

        BooleanExpression unreadMessagesFilter = MessagesQueries.messageReadStatus();

        final List<Messages> messageDetailList = Lists.newLinkedList(messagesRepository.findAll(unreadMessagesFilter));

        log.info("messageDetailList Number of records to process : {}", messageDetailList.size());

        for (Messages messagesDetail : messageDetailList) {
            log.info("messagesDetail  PART_NO : {}", messagesDetail.getJurorNumber());

            try {
                String welshSubjectText = messagesDetail.getSubject();
                String regionIdSms = messagesDetail.getLocationCode().getCourtRegion().getRegionId();
                Integer legacyTemplateIdSms = messagesDetail.getMessageId();
                String regionIdEmail = messagesDetail.getLocationCode().getCourtRegion().getRegionId();
                Integer legacyTemplateIdEmail = messagesDetail.getMessageId();
                String regionIdSmsWelsh = messagesDetail.getLocationCode().getCourtRegion().getRegionId();
                Integer legacyTemplateIdSmsWelsh = messagesDetail.getMessageId();
                String regionIdEmailWelsh = messagesDetail.getLocationCode().getCourtRegion().getRegionId();
                Integer legacyTemplateIdEmailWelsh = messagesDetail.getMessageId();


                //Queries to filter on Region_id ,Legacy_Template_id,Message_Format and Welsh_Language
                BooleanExpression regionNotifyTemplateSmsFilter =
                    RegionNotifyTemplateQueries.regionNotifyTemplateByIdAndSms(
                        regionIdSms,
                        legacyTemplateIdSms
                    );
                BooleanExpression regionNotifyTemplateEmailFilter =
                    RegionNotifyTemplateQueries.regionNotifyTemplateByIdAndEmail(
                        regionIdEmail,
                        legacyTemplateIdEmail
                    );
                BooleanExpression regionNotifyTemplateSmsFilterWelsh =
                    RegionNotifyTemplateQueries.regionNotifyTemplateByIdAndSmsWelsh(
                        regionIdSmsWelsh,
                        legacyTemplateIdSmsWelsh
                    );
                BooleanExpression regionNotifyTemplateEmailFilterWelsh =
                    RegionNotifyTemplateQueries.regionNotifyTemplateByIdAndEmailWelsh(
                        regionIdEmailWelsh,
                        legacyTemplateIdEmailWelsh
                    );

                //Queries to filter on Region_id ,Legacy_Template_id,Message_Format and Welsh_Language
                final List<RegionNotifyTemplate> regionNotifyTemplateListEmail = Lists.newLinkedList(
                    regionNotifyTemplateRepository.findAll(regionNotifyTemplateEmailFilter));
                final List<RegionNotifyTemplate> regionNotifyTemplateListSms = Lists.newLinkedList(
                    regionNotifyTemplateRepository.findAll(regionNotifyTemplateSmsFilter));
                final List<RegionNotifyTemplate> regionNotifyTemplateListSmsWelsh = Lists.newLinkedList(
                    regionNotifyTemplateRepository.findAll(regionNotifyTemplateSmsFilterWelsh));
                final List<RegionNotifyTemplate> regionNotifyTemplateListEmailWelsh = Lists.newLinkedList(
                    regionNotifyTemplateRepository.findAll(regionNotifyTemplateEmailFilterWelsh));


                templateCheck(
                    messagesDetail,
                    regionNotifyTemplateListEmail,
                    regionNotifyTemplateListSms,
                    regionNotifyTemplateListSmsWelsh,
                    regionNotifyTemplateListEmailWelsh
                );


                String jurorNumber = messagesDetail.getJurorNumber();
                String phoneNumber = messagesDetail.getPhone();
                String email = messagesDetail.getEmail();
                String textMessage = messagesDetail.getMessageText();
                String reference = (messagesDetail.getJurorNumber());


                //     String apiKey = (messagesDetail != null ? messagesDetail.getLocationCode().getCourtRegion()
                //     .getNotifyAccountKey() : null);
                String regionId = messagesDetail.getLocationCode().getCourtRegion().getRegionId();
                String regionApikey = myRegionMap.get(regionId);
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


                //  Send sms to notify
                if (messagesDetail.getEmail() == null) {

                    // for Welsh Region Notify Template Sms
                    if (Objects.equals(welshTempComparisonText, welshSubjectText)) {

                        NotificationClient clientSendSms = new NotificationClient(regionApikey, gotProxy);
                        for (RegionNotifyTemplate regionNotifyTemplateSmsListWelsh : regionNotifyTemplateListSmsWelsh) {
                            String smsTemplateIdWelsh = (regionNotifyTemplateSmsListWelsh != null
                                ? regionNotifyTemplateSmsListWelsh.getNotifyTemplateId()
                                : null);

                            if (smsTemplateIdWelsh == null || smsTemplateIdWelsh.isEmpty()) {
                                messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
                                updateMessageFlag(messagesDetail);


                                log.error(LOG_ERROR_MESSAGE_TEMPLATE_ID);
                                throw new IllegalStateException("smsTemplateId null or empty");

                            }
                            SendSmsResponse smsResponse = clientSendSms.sendSms(
                                smsTemplateIdWelsh, phoneNumber, personalisation, reference);
                            if (smsResponse.getNotificationId() != null) {
                                messagesDetail.setMessageRead(MESSAGE_READ);
                                updateMessageFlag(messagesDetail);
                            }

                            log.trace("Court Comms  sms messaging  Service :  response {}", smsResponse);
                        }


                    } // if Welsh end if

                    NotificationClient clientSendSms = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplate regionNotifyTemplateSmsList : regionNotifyTemplateListSms) {

                        String smsTemplateId = (regionNotifyTemplateSmsList != null
                            ? regionNotifyTemplateSmsList.getNotifyTemplateId()
                            : null);

                        if (smsTemplateId == null || smsTemplateId.isEmpty()) {

                            messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);

                            updateMessageFlag(messagesDetail);

                            log.error(LOG_ERROR_MESSAGE_TEMPLATE_ID);
                            throw new IllegalStateException("smsTemplateId null or empty");

                        }


                        SendSmsResponse smsResponse = clientSendSms.sendSms(
                            smsTemplateId, phoneNumber, personalisation, reference);
                        if (smsResponse.getNotificationId() != null) {
                            messagesDetail.setMessageRead(MESSAGE_READ);
                            updateMessageFlag(messagesDetail);
                        }

                        log.trace("Court Comms  sms messaging  Service :  response {}", smsResponse);
                    }

                }  // send sms

                // Send email to notify

                if (messagesDetail.getPhone() == null) {

                    // for Welsh Region Notify Template Email
                    if (Objects.equals(welshTempComparisonText, welshSubjectText)) {

                        NotificationClient clientSendEmail = new NotificationClient(regionApikey, gotProxy);

                        for (RegionNotifyTemplate regionNotifyTemplateEmailListWelsh :
                            regionNotifyTemplateListEmailWelsh) {
                            String emailTemplateIdWelsh = (regionNotifyTemplateEmailListWelsh != null
                                ? regionNotifyTemplateEmailListWelsh.getNotifyTemplateId()
                                : null);

                            if (emailTemplateIdWelsh == null || emailTemplateIdWelsh.isEmpty()) {
                                messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
                                updateMessageFlag(messagesDetail);

                                log.error(LOG_ERROR_MESSAGE_TEMPLATE_ID);
                                throw new IllegalStateException("smsTemplateId null or empty");

                            }
                            SendEmailResponse emailResponse = clientSendEmail.sendEmail(
                                emailTemplateIdWelsh, email, personalisation, reference);
                            if (emailResponse.getNotificationId() != null) {
                                messagesDetail.setMessageRead(MESSAGE_READ);
                                updateMessageFlag(messagesDetail);
                            }

                            log.trace("Court Comms  email messaging  Service :  response {}", emailResponse);
                        }

                    } // send Welsh email end if statement

                    NotificationClient clientSendEmail = new NotificationClient(regionApikey, gotProxy);


                    for (RegionNotifyTemplate regionNotifyTemplateEmailList : regionNotifyTemplateListEmail) {
                        String emailTemplateId = (regionNotifyTemplateEmailList != null
                            ? regionNotifyTemplateEmailList.getNotifyTemplateId()
                            : null);

                        if (emailTemplateId == null || emailTemplateId.isEmpty()) {
                            messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
                            updateMessageFlag(messagesDetail);
                            log.error(LOG_ERROR_MESSAGE_TEMPLATE_ID);
                            throw new IllegalStateException("emailTemplateId null or empty");
                        }

                        SendEmailResponse emailResponse = clientSendEmail.sendEmail(
                            emailTemplateId, email, personalisation, reference);
                        if (emailResponse.getNotificationId() != null) {
                            messagesDetail.setMessageRead(MESSAGE_READ);
                            updateMessageFlag(messagesDetail);
                        }
                        log.trace("Court Comms  email messaging  Service :  response {}", emailResponse);

                    }  // send email end of if statement

                }


            } catch (NotificationClientException e) {

                log.error("Failed to send via Notify: {}", e);
                log.trace("Unable to send notify: {}", e.getHttpResult());
                log.info("Unable to send notify: {}", e.getHttpResult());
                messagesDetail.setMessageRead(MESSAGE_READ_APP_ERROR);
                updateMessageFlag(messagesDetail);
            } catch (Exception e) {
                log.info("Unexpected exception: {}", e);
            }

        }

        log.info("Court Comms Processing : Finished - {}", dateFormat.format(new Date()));

    }


    private void templateCheck(Messages messagesDetail, List<RegionNotifyTemplate> regionNotifyTemplateListEmail,
                               List<RegionNotifyTemplate> regionNotifyTemplateListSms,
                               List<RegionNotifyTemplate> regionNotifyTemplateListEmailWelsh,
                               List<RegionNotifyTemplate> regionNotifyTemplateListSmsWelsh) {
        int missingEmailTemplateCheck = regionNotifyTemplateListEmail.size();
        int missingSmsTemplateCheck = regionNotifyTemplateListSms.size();
        int missingWelshEmailTemplateCheck = regionNotifyTemplateListEmailWelsh.size();
        int missingWelshSmsTemplateCheck = regionNotifyTemplateListSmsWelsh.size();

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
            Integer setupPort = Integer.valueOf(setUpProxy.getPort());
            Proxy.Type setupType = setUpProxy.getType();
            final InetSocketAddress socketAddress = new InetSocketAddress(setupHost, setupPort);
            proxy = new Proxy(setupType, socketAddress);
        }

        return proxy;
    }

    private void updateMessageFlag(Messages messagesDetail) {
        try {
            log.trace("Inside update....");
            messagesRepository.save(messagesDetail);
            log.trace("Updating messages_read ");
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (messagesDetail.getMessageRead().equals(MESSAGE_NOT_READ)) {
                log.trace("notifications is : {} - logging error", messagesDetail.getMessageRead());
                log.info("notifications is : {} - logging error", messagesDetail.getMessageRead());
                log.error("Failed to update db to {}. Manual update required. {}", messagesDetail.getMessageRead(),
                    cause.toString()
                );
            } else {
                log.trace("notifications is : {} - throwing excep", messagesDetail.getMessageRead());
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + messagesDetail.getMessageRead() + ". Manual update required. ",
                    cause
                );
            }
        }


    }

    public List<String> setUpNotifyRegionKeys() {
        List<String> notifyRegionKeys;
        notifyRegionKeys = notifyRegionsConfigurationProperties.getRegionKeys();
        return notifyRegionKeys;
    }

    public List<String> setUpRegionIds() {
        List<CourtRegion> courtRegions = Lists.newLinkedList(courtRegionRepository.findAll());
        List<String> regionIds = new ArrayList<>();

        for (CourtRegion courtRegion : courtRegions) {
            String courtregionIds = courtRegion.getRegionId();
            regionIds.add(courtregionIds);

            log.debug("CourtRegions {}", courtRegion.getRegionId());

        }

        return regionIds;
    }

}



