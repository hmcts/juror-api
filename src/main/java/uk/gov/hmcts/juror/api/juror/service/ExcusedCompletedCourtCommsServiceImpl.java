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
import uk.gov.hmcts.juror.api.bureau.service.BureauProcessService;
import uk.gov.hmcts.juror.api.config.NotifyConfigurationProperties;
import uk.gov.hmcts.juror.api.config.NotifyRegionsConfigurationProperties;
import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;
import uk.gov.hmcts.juror.api.moj.domain.CourtRegionMod;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.RegionNotifyTemplateMod;
import uk.gov.hmcts.juror.api.moj.repository.CourtRegionModRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolQueries;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
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
import java.util.UUID;


@Slf4j
@Service
public class ExcusedCompletedCourtCommsServiceImpl implements BureauProcessService {

    private final JurorPoolRepository jurorRepository;

    private final CourtRegionModRepository courtRegionModRepository;
    private final RegionNotifyTemplateRepositoryMod regionNotifyTemplateRepositoryMod;
    private Proxy proxy;
    private final String messagePlaceHolderJurorNumber = "JURORNUMBER";
    private final String messagePlaceHolderlocationCode = "lOCATIONCODE";
    private final String updateMessageStatusSent = "SENTNOTIFY";
    private final String updateMessageStatusNotSent = "NOTSENT";
    private final NotifyConfigurationProperties notifyConfigurationProperties;
    private final NotifyRegionsConfigurationProperties notifyRegionsConfigurationProperties;

    @Autowired
    public ExcusedCompletedCourtCommsServiceImpl(

        final AppSettingService appSetting,

        final JurorPoolRepository jurorRepository,
        final CourtRegionModRepository courtRegionModRepository,
        final NotifyConfigurationProperties notifyConfigurationProperties,
        final NotifyRegionsConfigurationProperties notifyRegionsConfigurationProperties,

        final RegionNotifyTemplateRepositoryMod regionNotifyTemplateRepositoryMod) {
        Assert.notNull(jurorRepository, "JurorPoolRepository cannot be null.");
        Assert.notNull(appSetting, "AppSettingService cannot be null.");
        Assert.notNull(courtRegionModRepository, "CourtRegionModRepository cannot be null.");
        Assert.notNull(notifyConfigurationProperties, "NotifyConfigurationProperties cannot be null.");
        Assert.notNull(notifyRegionsConfigurationProperties, "NotifyRegionsConfigurationProperties cannot be null.");

        Assert.notNull(regionNotifyTemplateRepositoryMod, "RegionNotifyTemplateRepositoryMod cannot be null.");
        this.jurorRepository = jurorRepository;
        this.courtRegionModRepository = courtRegionModRepository;
        this.notifyConfigurationProperties = notifyConfigurationProperties;
        this.notifyRegionsConfigurationProperties = notifyRegionsConfigurationProperties;

        this.regionNotifyTemplateRepositoryMod = regionNotifyTemplateRepositoryMod;
    }

    /**
     * Implements a specific job execution.
     * process Excusal criteria and Service Complete criteria for comms sent by Notify.
     */

    @Override
    @Transactional
    public SchedulerServiceClient.Result process() {


        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Excused Completed Court Comms Processing : Started - {}", dateFormat.format(new Date()));

        Proxy gotProxy = setUpConnection();

        log.debug("gotProxy {} ", gotProxy);

        Map<String, String> myRegionMap = new HashMap<>();

        @SuppressWarnings("PMD.VariableDeclarationUsageDistance")
        int errorCount = 0;
        List<String> regionIds = setUpRegionIds();
        List<String> notifyRegionIds = setUpNotifyRegionKeys();
        for (int i = 0; i < notifyRegionIds.size(); i++) {
            myRegionMap.put(regionIds.get(i), notifyRegionIds.get(i));
        }

        log.debug("Display myRegionMap {}", myRegionMap);
        errorCount += processExcusalList(gotProxy, myRegionMap);
        errorCount += processCompleted(gotProxy, myRegionMap);


        log.info("Excused Completed Court Comms Processing : Finished - {}", dateFormat.format(new Date()));
        return new SchedulerServiceClient.Result(
            errorCount == 0
                ? SchedulerServiceClient.Result.Status.SUCCESS
                : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS, null,
            Map.of("ERROR_COUNT", "" + errorCount));
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


    private void updateCommsStatusFlagCompleted(JurorPool poolCourtDetailCompletedList) {
        try {
            log.trace("Inside update....");
            jurorRepository.save(poolCourtDetailCompletedList);
            log.trace("Updating service_comp_comms_status ");
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (poolCourtDetailCompletedList.getJuror().getServiceCompCommsStatus() == null) {
                log.trace(
                    "ServiceCompCommsStatus is : {} - logging error",
                    poolCourtDetailCompletedList.getJuror().getServiceCompCommsStatus()
                );
                log.info(
                    "ServiceCompCommsStatus is : {} - logging error",
                    poolCourtDetailCompletedList.getJuror().getServiceCompCommsStatus()
                );
                log.error(
                    "Failed to update db to {}. Manual update required. {}",
                    poolCourtDetailCompletedList.getJuror().getServiceCompCommsStatus(),
                    cause.toString()
                );
            } else {
                log.trace(
                    "notifications is : {} - throwing excep",
                    poolCourtDetailCompletedList.getJuror().getServiceCompCommsStatus()
                );
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + poolCourtDetailCompletedList.getJuror().getServiceCompCommsStatus()
                        + ". Manual update required. ",
                    cause
                );
            }
        }
    }

    private void updateCommsStatusFlagExcusal(JurorPool poolCourtDetailExcusalList) {
        try {
            log.trace("Inside update....");
            jurorRepository.save(poolCourtDetailExcusalList);
            log.trace("Updating service_comp_comms_status ");
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (poolCourtDetailExcusalList.getJuror().getServiceCompCommsStatus() == null) {
                log.trace(
                    "ServiceCompCommsStatus is : {} - logging error",
                    poolCourtDetailExcusalList.getJuror().getServiceCompCommsStatus()
                );
                log.info(
                    "ServiceCompCommsStatus is : {} - logging error",
                    poolCourtDetailExcusalList.getJuror().getServiceCompCommsStatus()
                );
                log.error(
                    "Failed to update db to {}. Manual update required. {}",
                    poolCourtDetailExcusalList.getJuror().getServiceCompCommsStatus(),
                    cause.toString()
                );
            } else {
                log.trace(
                    "notifications is : {} - throwing excep",
                    poolCourtDetailExcusalList.getJuror().getServiceCompCommsStatus()
                );
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + poolCourtDetailExcusalList.getJuror().getServiceCompCommsStatus()
                        + ". Manual update required. ",
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
        List<CourtRegionMod> courtRegions = courtRegionModRepository.findAllByOrderByRegionIdAsc();
        List<String> regionIds = new ArrayList<>();

        for (CourtRegionMod courtRegion : courtRegions) {
            String courtregionIds = courtRegion.getRegionId();
            regionIds.add(courtregionIds);

            log.info("CourtRegions {}", courtRegion.getRegionId());

        }

        return regionIds;
    }

    public int processExcusalList(Proxy gotProxy, Map<String, String> myRegionMap) {
        final List<JurorPool> jurorCourtDetailListExcusal = Lists.newLinkedList(jurorRepository.findAll(
            JurorPoolQueries.recordsForExcusalComms()));

        log.info(
            "JurorCourtDetailListExcusal Number of Excusal Records to process {}",
            jurorCourtDetailListExcusal.size()
        );

        int errorCount = 0;
        for (JurorPool jurorCourtDetailExcusalList : jurorCourtDetailListExcusal) {
            log.info("poolCourtDetailExcusalList PART_NO : {}", jurorCourtDetailExcusalList.getJurorNumber());
            log.info("Excusal Date: {}", jurorCourtDetailExcusalList.getJuror().getExcusalDate());


            final String regionIdExcusalSms = jurorCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();
            final String regionIdExcusalEmail = jurorCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();

            final String phone = jurorCourtDetailExcusalList.getJuror().getAltPhoneNumber();

            final String email = jurorCourtDetailExcusalList.getJuror().getEmail();

            final int emailLength = (email != null ? email.length() : 0);
            if (emailLength == 1) {
                jurorCourtDetailExcusalList.getJuror().setEmail(null);
                updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);
            }

            final String locCode = jurorCourtDetailExcusalList.getCourt().getLocCode();
            final String firstName = jurorCourtDetailExcusalList.getJuror().getFirstName();
            final String lastName = jurorCourtDetailExcusalList.getJuror().getLastName();
            final String jurorNumber = jurorCourtDetailExcusalList.getJurorNumber();
            final String courtAddress = jurorCourtDetailExcusalList.getCourt().getLocationAddress();
            final String courtPhone = jurorCourtDetailExcusalList.getCourt().getLocPhone();
            final String courtName = jurorCourtDetailExcusalList.getCourt().getLocCourtName();
            final String reference = jurorCourtDetailExcusalList.getJurorNumber();


            final String regionId = jurorCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();
            final String regionApikey = myRegionMap.get(regionId);
            log.debug("regionApikey {} ", regionApikey);


            if (regionApikey == null || regionApikey.isEmpty()) {
                log.error("Missing Notify Api Account key Cannot send notify communication: ");
                log.info("Missing Notify Api Account key Cannot send notify communication: ");

                jurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);

                continue;
            }

            Map<String, String> personalisationEmail = new HashMap<>();
            String messagePlaceHolderFirstName = "FIRSTNAME";
            personalisationEmail.put(messagePlaceHolderFirstName, firstName);
            String messagePlaceHolderLastName = "LASTNAME";
            personalisationEmail.put(messagePlaceHolderLastName, lastName);
            personalisationEmail.put(messagePlaceHolderJurorNumber, jurorNumber);
            String messagePlaceHolderCourtAddress = "COURTADDRESS";
            personalisationEmail.put(messagePlaceHolderCourtAddress, courtAddress);
            personalisationEmail.put(messagePlaceHolderlocationCode, locCode);

            Map<String, String> personalisationText = new HashMap<>();
            String messagePlaceHolderCourtName = "COURTNAME";
            personalisationText.put(messagePlaceHolderCourtName, courtName);
            String messagePlaceHolderCourtPhone = "COURTPHONE";
            personalisationText.put(messagePlaceHolderCourtPhone, courtPhone);
            personalisationText.put(messagePlaceHolderlocationCode, locCode);

            BooleanExpression regionNotifyTriggeredExcusalTemplateSmsFilter =
                RegionNotifyTemplateQueriesMod.regionNotifyTriggeredExcusalTemplateSmsId(
                    regionIdExcusalSms);
            BooleanExpression regionNotifyTriggeredExcusalTemplateEmailFilter =
                RegionNotifyTemplateQueriesMod.regionNotifyTriggeredExcusalTemplateEmailId(
                    regionIdExcusalEmail);

            BooleanExpression welshRegionNotifyTriggeredExcusalTemplateSmsFilter =
                RegionNotifyTemplateQueriesMod.welshRegionNotifyTriggeredExcusalTemplateSmsId(
                    regionIdExcusalSms,
                    "Y"
                );
            BooleanExpression welshRegionNotifyTriggeredExcusalTemplateEmailFilter =
                RegionNotifyTemplateQueriesMod.welshRegionNotifyTriggeredExcusalTemplateEmailId(
                    regionIdExcusalEmail,
                    "Y"
                );

            NotificationClient notificationClient = new NotificationClient(regionApikey, gotProxy);


            boolean hasEmail = jurorCourtDetailExcusalList.getJuror().getEmail() != null;
            boolean hasPhone = jurorCourtDetailExcusalList.getJuror().getAltPhoneNumber() != null;

            try {
                List<RegionNotifyTemplateMod> regionNotifyTriggeredExcusalTemplateList;
                if (hasEmail) {
                    if (jurorCourtDetailExcusalList.getJuror().isWelsh()) {
                        regionNotifyTriggeredExcusalTemplateList =
                            Lists.newLinkedList(
                                regionNotifyTemplateRepositoryMod.findAll(
                                    welshRegionNotifyTriggeredExcusalTemplateEmailFilter));
                    } else {
                        regionNotifyTriggeredExcusalTemplateList = Lists.newLinkedList(
                            regionNotifyTemplateRepositoryMod.findAll(regionNotifyTriggeredExcusalTemplateEmailFilter));
                    }
                } else if (hasPhone) {
                    if (jurorCourtDetailExcusalList.getJuror().isWelsh()) {
                        regionNotifyTriggeredExcusalTemplateList = Lists.newLinkedList(
                            regionNotifyTemplateRepositoryMod.findAll(
                                welshRegionNotifyTriggeredExcusalTemplateSmsFilter));
                    } else {
                        regionNotifyTriggeredExcusalTemplateList = Lists.newLinkedList(
                            regionNotifyTemplateRepositoryMod.findAll(regionNotifyTriggeredExcusalTemplateSmsFilter));
                    }
                } else {
                    log.info("No Email or phone");
                    continue;
                }

                for (RegionNotifyTemplateMod regionNotifyTemplateMod : regionNotifyTriggeredExcusalTemplateList) {
                    UUID notificationId;
                    if (hasEmail) {

                        String emailExcusalTemplateId = regionNotifyTemplateMod.getNotifyTemplateId();
                        SendEmailResponse emailResponse = notificationClient.sendEmail(
                            emailExcusalTemplateId,
                            email,
                            personalisationEmail,
                            reference
                        );
                        notificationId = emailResponse.getNotificationId();
                    } else {
                        String smsExcusalTemplateId = regionNotifyTemplateMod.getNotifyTemplateId();

                        SendSmsResponse smsResponse = notificationClient.sendSms(
                            smsExcusalTemplateId,
                            phone,
                            personalisationText,
                            reference
                        );
                        notificationId = smsResponse.getNotificationId();
                    }
                    if (notificationId != null) {
                        jurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusSent);
                        updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);
                    }
                }
            } catch (NotificationClientException e) {
                log.error("Failed to send via Notify: {}", e);
                log.trace("Unable to send notify: {}", e.getHttpResult());
                log.info("Unable to send notify: {}", e.getHttpResult());
                jurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);

                updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);
                errorCount++;
            } catch (Exception e) {
                log.info("Unexpected exception: {}", e);
                errorCount++;
            }
        }
        return errorCount;
    }

    private int processCompleted(Proxy gotProxy, Map<String, String> myRegionMap) {
        int errorCount = 0;
        final List<JurorPool> jurorCourtDetailListCompleted = Lists.newLinkedList(jurorRepository.findAll(
            JurorPoolQueries.recordsForServiceCompletedComms()));

        log.info(
            "jurorCourtDetailListCompleted Number of Completed Records to process {}",
            jurorCourtDetailListCompleted.size()
        );

        for (JurorPool jurorCourtDetailCompletedList : jurorCourtDetailListCompleted) {

            log.info("jurorCourtDetailListCompleted PART_NO {}", jurorCourtDetailCompletedList.getJurorNumber());

            final String regionIdCompleteEmail = jurorCourtDetailCompletedList.getCourt().getCourtRegion()
                .getRegionId();
            final String regionIdCompleteSms = jurorCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();

            final String phone = jurorCourtDetailCompletedList.getJuror().getAltPhoneNumber();

            final String email = jurorCourtDetailCompletedList.getJuror().getEmail();

            final int emailLength = (email != null ? email.length() : 0);
            if (emailLength == 1) {

                jurorCourtDetailCompletedList.getJuror().setEmail(null);

                updateCommsStatusFlagCompleted(jurorCourtDetailCompletedList);

            }
            final String locCode = jurorCourtDetailCompletedList.getCourt().getLocCode();
            final String jurorNumber = jurorCourtDetailCompletedList.getJurorNumber();
            final String reference = jurorCourtDetailCompletedList.getJurorNumber();


            //  String apiKey = (poolCourtDetailCompletedList != null ? poolCourtDetailCompletedList.getCourt()
            //  .getCourtRegion().getNotifyAccountKey() : null);
            final String regionId = jurorCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();
            final String regionApikey = myRegionMap.get(regionId);
            log.debug("regionApikey {} ", regionApikey);


            if (regionApikey == null || regionApikey.isEmpty()) {
                log.error("Missing Notify Api Account key Cannot send notify communication: ");
                log.info("Missing Notify Api Account key Cannot send notify communication: ");
                jurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagCompleted(jurorCourtDetailCompletedList);

                continue;
            }


            Map<String, String> personalisationEmail = new HashMap<>();
            personalisationEmail.put(messagePlaceHolderJurorNumber, jurorNumber);
            personalisationEmail.put(messagePlaceHolderlocationCode, locCode);

            Map<String, String> personalisationText = new HashMap<>();
            personalisationText.put(messagePlaceHolderlocationCode, locCode);

            BooleanExpression regionNotifyTriggeredCompleteTemplateEmailFilter =
                RegionNotifyTemplateQueriesMod.regionNotifyTriggeredCompletedTemplateEmailId(
                    regionIdCompleteEmail);
            BooleanExpression regionNotifyTriggeredCompleteTemplateSmsFilter =
                RegionNotifyTemplateQueriesMod.regionNotifyTriggeredCompletedTemplateSmsId(
                    regionIdCompleteSms);


            String isCompletedEmailWelsh = "Y";
            String isCompletedSmsWelsh = "Y";

            BooleanExpression welshRegionNotifyTriggeredCompleteTemplateEmailFilter =
                RegionNotifyTemplateQueriesMod.welshRegionNotifyTriggeredCompletedTemplateEmailId(
                    regionIdCompleteEmail,
                    isCompletedEmailWelsh
                );
            BooleanExpression welshRegionNotifyTriggeredCompleteTemplateSmsFilter =
                RegionNotifyTemplateQueriesMod.welshRegionNotifyTriggeredCompletedTemplateSmsId(
                    regionIdCompleteSms,
                    isCompletedSmsWelsh
                );

            try {
                NotificationClient notificationClient = new NotificationClient(regionApikey, gotProxy);
                boolean hasEmail = jurorCourtDetailCompletedList.getJuror().getEmail() != null;
                boolean hasPhone = jurorCourtDetailCompletedList.getJuror().getAltPhoneNumber() != null;

                List<RegionNotifyTemplateMod> regionNotifyTriggeredCompletedTemplateList;
                if (hasEmail) {
                    if (jurorCourtDetailCompletedList.getJuror().isWelsh()) {
                        regionNotifyTriggeredCompletedTemplateList =
                            Lists.newLinkedList(
                                regionNotifyTemplateRepositoryMod.findAll(
                                    welshRegionNotifyTriggeredCompleteTemplateEmailFilter));
                    } else {
                        regionNotifyTriggeredCompletedTemplateList = Lists.newLinkedList(
                            regionNotifyTemplateRepositoryMod.findAll(
                                regionNotifyTriggeredCompleteTemplateEmailFilter));
                    }
                } else if (hasPhone) {
                    if (jurorCourtDetailCompletedList.getJuror().isWelsh()) {
                        regionNotifyTriggeredCompletedTemplateList =
                            Lists.newLinkedList(
                                regionNotifyTemplateRepositoryMod.findAll(
                                    welshRegionNotifyTriggeredCompleteTemplateSmsFilter));
                    } else {
                        regionNotifyTriggeredCompletedTemplateList = Lists.newLinkedList(
                            regionNotifyTemplateRepositoryMod.findAll(regionNotifyTriggeredCompleteTemplateSmsFilter));
                    }
                } else {
                    continue;
                }

                for (RegionNotifyTemplateMod regionNotifyTriggeredCompleteTemplateList :
                    regionNotifyTriggeredCompletedTemplateList) {
                    UUID notificationId;
                    if (hasEmail) {
                        String emailCompletedTemplateId =
                            regionNotifyTriggeredCompleteTemplateList.getNotifyTemplateId();

                        SendEmailResponse emailResponse = notificationClient.sendEmail(
                            emailCompletedTemplateId,
                            email,
                            personalisationEmail,
                            reference
                        );
                        notificationId = emailResponse.getNotificationId();
                    } else {
                        String smsCompletedTemplateId =
                            regionNotifyTriggeredCompleteTemplateList.getNotifyTemplateId();


                        SendSmsResponse smsResponse = notificationClient.sendSms(
                            smsCompletedTemplateId,
                            phone,
                            personalisationText,
                            reference
                        );
                        notificationId = smsResponse.getNotificationId();
                    }
                    if (notificationId != null) {
                        jurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusSent);
                        updateCommsStatusFlagCompleted(jurorCourtDetailCompletedList);
                    }
                }
            } catch (NotificationClientException e) {

                log.error("Failed to send via Notify: {}", e);
                log.trace("Unable to send notify: {}", e.getHttpResult());
                log.info("Unable to send notify: {}", e.getHttpResult());
                jurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);

                updateCommsStatusFlagCompleted(jurorCourtDetailCompletedList);
                errorCount++;
            } catch (Exception e) {
                log.info("Unexpected exception: {}", e);
                errorCount++;
            }
        }
        return errorCount;
    }
}