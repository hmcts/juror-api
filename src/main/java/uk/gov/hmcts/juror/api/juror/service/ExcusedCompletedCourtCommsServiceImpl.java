package uk.gov.hmcts.juror.api.juror.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import java.util.UUID;


@Slf4j
@Service
public class ExcusedCompletedCourtCommsServiceImpl implements BureauProcessService {

    private final JurorPoolRepository jurorRepository;
    private final CourtRegionModRepository courtRegionModRepository;
    private final RegionNotifyTemplateRepositoryMod regionNotifyTemplateRepositoryMod;
    private final NotifyConfigurationProperties notifyConfigurationProperties;
    private final NotifyRegionsConfigurationProperties notifyRegionsConfigurationProperties;
    private final String messagePlaceHolderJurorNumber = "JURORNUMBER";
    private final String messagePlaceHolderlocationCode = "lOCATIONCODE";
    private final String updateMessageStatusSent = "SENTNOTIFY";
    private final String updateMessageStatusNotSent = "NOTSENT";
    private Proxy proxy;

    @Autowired
    public ExcusedCompletedCourtCommsServiceImpl(
        final JurorPoolRepository jurorRepository,
        final CourtRegionModRepository courtRegionModRepository,
        final NotifyConfigurationProperties notifyConfigurationProperties,
        final NotifyRegionsConfigurationProperties notifyRegionsConfigurationProperties,
        final RegionNotifyTemplateRepositoryMod regionNotifyTemplateRepositoryMod) {
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

        List<String> regionIds = setUpRegionIds();
        List<String> notifyRegionIds = setUpNotifyRegionKeys();
        for (int i = 0; i < notifyRegionIds.size(); i++) {
            myRegionMap.put(regionIds.get(i), notifyRegionIds.get(i));
        }

        log.debug("Display myRegionMap {}", myRegionMap);
        final Metrics excusalMetrics = processExcusalList(gotProxy, myRegionMap);
        final Metrics completedMetrics = processCompleted(gotProxy, myRegionMap);

        log.info("Excused Completed Court Comms Processing : Finished - {}", dateFormat.format(new Date()));
        return new SchedulerServiceClient.Result(
            excusalMetrics.errorCount == 0 && completedMetrics.errorCount == 0
                ? SchedulerServiceClient.Result.Status.SUCCESS
                : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS, null,
            getMetaData(excusalMetrics, completedMetrics));
    }

    private static Map<String, String> getMetaData(Metrics excusalMetrics, Metrics completedMetrics) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put("EXCUSAL_IDENTIFIED", String.valueOf(excusalMetrics.jurorPoolsIdentified));
        metaData.put("EXCUSAL_ERROR_COUNT", String.valueOf(excusalMetrics.errorCount));
        metaData.put("EXCUSAL_INVALID_PHONE_COUNT", String.valueOf(excusalMetrics.invalidPhoneCount));
        metaData.put("EXCUSAL_INVALID_EMAIL_COUNT", String.valueOf(excusalMetrics.invalidEmailAddressCount));
        metaData.put("EXCUSAL_SUCCESS_COUNT", String.valueOf(excusalMetrics.successCount));
        metaData.put("EXCUSAL_MISSING_EMAIL_PHONE", String.valueOf(excusalMetrics.missingEmailAndPhone));

        metaData.put("COMPLETED_IDENTIFIED", String.valueOf(completedMetrics.jurorPoolsIdentified));
        metaData.put("COMPLETED_ERROR_COUNT", String.valueOf(completedMetrics.errorCount));
        metaData.put("COMPLETED_INVALID_PHONE_COUNT", String.valueOf(completedMetrics.invalidPhoneCount));
        metaData.put("COMPLETED_INVALID_EMAIL_COUNT", String.valueOf(completedMetrics.invalidEmailAddressCount));
        metaData.put("COMPLETED_SUCCESS_COUNT", String.valueOf(completedMetrics.successCount));
        metaData.put("COMPLETED_MISSING_EMAIL_PHONE", String.valueOf(completedMetrics.missingEmailAndPhone));
        return metaData;
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


    private void updateCommsStatusFlagCompleted(JurorPool poolCourtDetailCompletedList) {
        try {
            log.trace("Inside update....");
            jurorRepository.save(poolCourtDetailCompletedList);
            log.trace("Updating service_comp_comms_status ");
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (poolCourtDetailCompletedList.getJuror().getServiceCompCommsStatus() == null) {
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

    @SuppressWarnings("checkstyle:LineLength") // false positive
    public Metrics processExcusalList(Proxy gotProxy, Map<String, String> myRegionMap) {
        final List<JurorPool> jurorCourtDetailListExcusal = Lists.newLinkedList(jurorRepository.findAll(
            JurorPoolQueries.recordsForExcusalComms()));

        log.info(
            "JurorCourtDetailListExcusal Number of Excusal Records to process {}",
            jurorCourtDetailListExcusal.size()
        );

        int errorCount = 0;
        int invalidPhoneCount = 0;
        int invalidEmailCount = 0;
        int successCount = 0;
        int missingEmailAndPhone = 0;
        int missingApiKeyCount = 0;

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
                log.error("Missing Notify Api Account key Cannot send notify communication");

                jurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);
                missingApiKeyCount++;
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
                    missingEmailAndPhone++;
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
                        successCount++;
                    } else {
                        errorCount++;
                    }
                }
            } catch (NotificationClientException e) {
                log.info("Unable to send notify: {}", e.getMessage());
                jurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);

                updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);
                if (NotifyUtil.isInvalidPhoneNumberError(e)) {
                    invalidPhoneCount++;
                } else if (NotifyUtil.isInvalidEmailAddressError(e)) {
                    invalidEmailCount++;
                } else {
                    log.error("Failed to send via Notify: {}", e.getMessage(), e);
                    errorCount++;
                }
            } catch (Exception e) {
                log.error("Unexpected exception: {}", e.getMessage(), e);
                errorCount++;
            }
        }

        SchedulerServiceClient.Result.Status status =  errorCount == 0
            ? SchedulerServiceClient.Result.Status.SUCCESS
            : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS;

        // log the results for Dynatrace
        log.info(
            "[JobKey: CRONBATCH_EXCUSAL_SERVICE_COURT_COMMS]\n[{}]\nresult={},\nmetadata={number_of_jurors={},error_count={},success_count={},missing_email_and_phone_count={},missing_api_key_count={},invalid_phone_count={},invalid_email_count={}}",
            DATE_TIME_FORMATTER.format(LocalDateTime.now()),
            status,
            jurorCourtDetailListExcusal.size(),
            errorCount,
            successCount,
            missingEmailAndPhone,
            missingApiKeyCount,
            invalidPhoneCount,
            invalidEmailCount
        );

        return new Metrics(jurorCourtDetailListExcusal.size(), errorCount, successCount,
            missingEmailAndPhone, missingApiKeyCount,
            invalidPhoneCount, invalidEmailCount);
    }

    @SuppressWarnings("checkstyle:LineLength") // false positive
    private Metrics processCompleted(Proxy gotProxy, Map<String, String> myRegionMap) {

        final List<JurorPool> jurorCourtDetailListCompleted = Lists.newLinkedList(jurorRepository.findAll(
            JurorPoolQueries.recordsForServiceCompletedComms()));

        log.info(
            "jurorCourtDetailListCompleted Number of Completed Records to process {}",
            jurorCourtDetailListCompleted.size()
        );
        int errorCount = 0;
        int invalidPhoneCount = 0;
        int invalidEmailCount = 0;
        int successCount = 0;
        int missingEmailAndPhone = 0;
        int missingApiKeyCount = 0;
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

            final String regionId = jurorCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();
            final String regionApikey = myRegionMap.get(regionId);
            log.debug("regionApikey {} ", regionApikey);


            if (regionApikey == null || regionApikey.isEmpty()) {
                log.error("Missing Notify Api Account key Cannot send notify communication: ");
                jurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagCompleted(jurorCourtDetailCompletedList);
                missingApiKeyCount++;
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
                    log.info("No Email or phone");
                    missingEmailAndPhone++;
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
                        successCount++;
                    } else {
                        errorCount++;
                    }
                }
            } catch (NotificationClientException e) {
                log.info("Unable to send notify: {}", e.getMessage());
                jurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagCompleted(jurorCourtDetailCompletedList);
                errorCount++;
                if (NotifyUtil.isInvalidPhoneNumberError(e)) {
                    invalidPhoneCount++;
                } else if (NotifyUtil.isInvalidEmailAddressError(e)) {
                    invalidEmailCount++;
                } else {
                    log.error("Failed to send via Notify: {}", e.getMessage(), e);
                    errorCount++;
                }

            } catch (Exception e) {
                log.error("Unexpected exception:", e);
                errorCount++;
            }
        }

        SchedulerServiceClient.Result.Status status =  errorCount == 0
            ? SchedulerServiceClient.Result.Status.SUCCESS
            : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS;

        // log the results for Dynatrace
        log.info(
            "[JobKey: CRONBATCH_COMPLETED_SERVICE_COURT_COMMS]\n[{}]\nresult={},\nmetadata={number_of_jurors={},error_count={},success_count={},missing_email_and_phone_count={},missing_api_key_count={},invalid_phone_count={},invalid_email_count={}}",
            DATE_TIME_FORMATTER.format(LocalDateTime.now()),
            jurorCourtDetailListCompleted.size(),
            status,
            errorCount,
            successCount,
            missingEmailAndPhone,
            missingApiKeyCount,
            invalidPhoneCount,
            invalidEmailCount
        );

        return new Metrics(jurorCourtDetailListCompleted.size(), errorCount, successCount,
            missingEmailAndPhone, missingApiKeyCount,
            invalidPhoneCount, invalidEmailCount);
    }

    public record Metrics(int jurorPoolsIdentified, int errorCount, int successCount,
                          int missingEmailAndPhone, int missingApiKeyCount,
                          int invalidPhoneCount, int invalidEmailAddressCount) {

    }
}
