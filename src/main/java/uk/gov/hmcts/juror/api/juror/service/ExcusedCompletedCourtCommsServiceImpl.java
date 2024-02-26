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
import uk.gov.hmcts.juror.api.juror.domain.PoolCourt;
import uk.gov.hmcts.juror.api.juror.domain.PoolCourtQueries;
import uk.gov.hmcts.juror.api.juror.domain.PoolCourtRepository;
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


@Slf4j
@Service
public class ExcusedCompletedCourtCommsServiceImpl implements BureauProcessService {


    private final PoolCourtRepository poolCourtRepository;
    private final AppSettingService appSetting;
    private final CourtRegionRepository courtRegionRepository;
    private final RegionNotifyTemplateRepository regionNotifyTemplateRepository;
    private Proxy proxy;
    private final String messagePlaceHolderFirstName = "FIRSTNAME";
    private final String messagePlaceHolderLastName = "LASTNAME";
    private final String messagePlaceHolderJurorNumber = "JURORNUMBER";
    private final String messagePlaceHolderCourtAddress = "COURTADDRESS";
    private final String messagePlaceHolderCourtPhone = "COURTPHONE";
    private final String messagePlaceHolderlocationCode = "lOCATIONCODE";
    private final String messagePlaceHolderCourtName = "COURTNAME";
    private final NotifyConfigurationProperties notifyConfigurationProperties;
    private final NotifyRegionsConfigurationProperties notifyRegionsConfigurationProperties;
    private final String updateMessageStatusSent = "SENTNOTIFY";
    private final String updateMessageStatusNotSent = "NOTSENT";

    @Autowired
    public ExcusedCompletedCourtCommsServiceImpl(

        final AppSettingService appSetting,
        final PoolCourtRepository poolCourtRepository,
        final CourtRegionRepository courtRegionRepository,
        final NotifyConfigurationProperties notifyConfigurationProperties,
        final NotifyRegionsConfigurationProperties notifyRegionsConfigurationProperties,
        final RegionNotifyTemplateRepository regionNotifyTemplateRepository) {
        Assert.notNull(poolCourtRepository, "PoolCourtRepository cannot be null.");
        Assert.notNull(appSetting, "AppSettingService cannot be null.");
        Assert.notNull(courtRegionRepository, "CourtRegionRepository cannot be null.");
        Assert.notNull(notifyConfigurationProperties, "NotifyConfigurationProperties cannot be null.");
        Assert.notNull(notifyRegionsConfigurationProperties, "NotifyRegionsConfigurationProperties cannot be null.");
        Assert.notNull(regionNotifyTemplateRepository, "RegionNotifyTemplateRepository cannot be null.");
        this.appSetting = appSetting;
        this.poolCourtRepository = poolCourtRepository;
        this.courtRegionRepository = courtRegionRepository;
        this.notifyConfigurationProperties = notifyConfigurationProperties;
        this.notifyRegionsConfigurationProperties = notifyRegionsConfigurationProperties;
        this.regionNotifyTemplateRepository = regionNotifyTemplateRepository;
    }

    /**
     * Implements a specific job execution.
     * process Excusal criteria and Service Complete criteria for comms sent by Notify.
     */

    @Override
    @Transactional
    public void process() {


        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Excused Completed Court Comms Processing : Started - {}", dateFormat.format(new Date()));

        Proxy gotProxy = setUpConnection();

        log.debug("gotProxy {} ", gotProxy);

        Map<String, String> myRegionMap = new HashMap<>();


        for (int i = 0;
             i < setUpNotifyRegionKeys().size();
             i++) {
            myRegionMap.put(setUpRegionIds().get(i), setUpNotifyRegionKeys().get(i));
        }

        log.debug("Display myRegionMap {}", myRegionMap);


        BooleanExpression recordsForExcusalCommsFilter = PoolCourtQueries.recordsForExcusalComms();
        final List<PoolCourt> poolCourtDetailListExcusal = Lists.newLinkedList(poolCourtRepository.findAll(
            recordsForExcusalCommsFilter));

        log.info(
            "poolCourtDetailListExcusal Number of Excusal Records to process {}",
            poolCourtDetailListExcusal.size()
        );


        for (PoolCourt poolCourtDetailExcusalList : poolCourtDetailListExcusal) {

            log.info("poolCourtDetailExcusalList PART_NO : {}", poolCourtDetailExcusalList.getJurorNumber());
            log.info("Excusal Date: {}", poolCourtDetailExcusalList.getExcusalDate());


            String regionIdExcusalSms = poolCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();
            String regionIdExcusalEmail = poolCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();

            String phone = poolCourtDetailExcusalList.getAltPhoneNumber();

            String email = poolCourtDetailExcusalList.getEmail();

            int emailLength = (email != null
                ?
                email.length()
                :
                    0);
            if (emailLength == 1) {

                poolCourtDetailExcusalList.setEmail(null);
                updateCommsStatusFlagExcusal(poolCourtDetailExcusalList);
            }

            String locCode = poolCourtDetailExcusalList.getCourt().getLocCode();
            String firstName = poolCourtDetailExcusalList.getFirstName();
            String lastName = poolCourtDetailExcusalList.getLastName();
            String jurorNumber = poolCourtDetailExcusalList.getJurorNumber();
            String courtAddress = poolCourtDetailExcusalList.getCourt().getLocationAddress();
            String courtPhone = poolCourtDetailExcusalList.getCourt().getLocPhone();
            String courtName = poolCourtDetailExcusalList.getCourt().getLocCourtName();
            String reference = poolCourtDetailExcusalList.getJurorNumber();


            String regionId = poolCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();
            String regionApikey = myRegionMap.get(regionId);
            log.debug("regionApikey {} ", regionApikey);


            if (regionApikey == null || regionApikey.isEmpty()) {
                log.error("Missing Notify Api Account key Cannot send notify communication: ");
                log.info("Missing Notify Api Account key Cannot send notify communication: ");

                poolCourtDetailExcusalList.setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagExcusal(poolCourtDetailExcusalList);

                continue;
            }

            Map<String, String> personalisationEmail = new HashMap<>();
            personalisationEmail.put(messagePlaceHolderFirstName, firstName);
            personalisationEmail.put(messagePlaceHolderLastName, lastName);
            personalisationEmail.put(messagePlaceHolderJurorNumber, jurorNumber);
            personalisationEmail.put(messagePlaceHolderCourtAddress, courtAddress);
            personalisationEmail.put(messagePlaceHolderlocationCode, locCode);

            Map<String, String> personalisationText = new HashMap<>();
            personalisationText.put(messagePlaceHolderCourtName, courtName);
            personalisationText.put(messagePlaceHolderCourtPhone, courtPhone);
            personalisationText.put(messagePlaceHolderlocationCode, locCode);
            BooleanExpression regionNotifyTriggeredExcusalTemplateSmsFilter =
                RegionNotifyTemplateQueries.regionNotifyTriggeredExcusalTemplateSmsId(
                    regionIdExcusalSms);
            BooleanExpression regionNotifyTriggeredExcusalTemplateEmailFilter =
                RegionNotifyTemplateQueries.regionNotifyTriggeredExcusalTemplateEmailId(
                    regionIdExcusalEmail);


            final List<RegionNotifyTemplate> regionNotifyTriggeredExcusalTemplateListSms = Lists.newLinkedList(
                regionNotifyTemplateRepository.findAll(regionNotifyTriggeredExcusalTemplateSmsFilter));
            final List<RegionNotifyTemplate> regionNotifyTriggeredExcusalTemplateListEmail = Lists.newLinkedList(
                regionNotifyTemplateRepository.findAll(regionNotifyTriggeredExcusalTemplateEmailFilter));


            try {

                /**
                 *  send excusal email
                 */

                if (poolCourtDetailExcusalList.getEmail() != null) {

                    NotificationClient clientSendEmail = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplate regionNotifyTriggeredExcusalTemplateEmailList :
                        regionNotifyTriggeredExcusalTemplateListEmail) {

                        String emailExcusalTemplateId =
                            regionNotifyTriggeredExcusalTemplateEmailList.getNotifyTemplateId();
                        SendEmailResponse emailResponse = clientSendEmail.sendEmail(
                            emailExcusalTemplateId,
                            email,
                            personalisationEmail,
                            reference
                        );

                        if (emailResponse.getNotificationId() != null) {
                            poolCourtDetailExcusalList.setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagExcusal(poolCourtDetailExcusalList);
                        }

                    }

                } //send email  end of if statement

                /**
                 *  send excusal sms
                 */


                if (poolCourtDetailExcusalList.getAltPhoneNumber() != null
                    && poolCourtDetailExcusalList.getEmail() == null) {

                    NotificationClient clientSendSms = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplate regionNotifyTriggeredExcusalTemplateSmsList :
                        regionNotifyTriggeredExcusalTemplateListSms) {

                        String smsExcusalTemplateId = regionNotifyTriggeredExcusalTemplateSmsList.getNotifyTemplateId();


                        SendSmsResponse smsResponse = clientSendSms.sendSms(
                            smsExcusalTemplateId,
                            phone,
                            personalisationText,
                            reference
                        );
                        if (smsResponse.getNotificationId() != null) {
                            poolCourtDetailExcusalList.setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagExcusal(poolCourtDetailExcusalList);
                        }

                    }

                }  // send sms without no proxy end of if statement


                /**
                 *  end of 1st try catch
                 */

            } catch (NotificationClientException e) {

                log.error("Failed to send via Notify: {}", e);
                log.trace("Unable to send notify: {}", e.getHttpResult());
                log.info("Unable to send notify: {}", e.getHttpResult());
                poolCourtDetailExcusalList.setServiceCompCommsStatus(updateMessageStatusNotSent);

                updateCommsStatusFlagExcusal(poolCourtDetailExcusalList);
            } catch (Exception e) {
                log.info("Unexpected exception: {}", e);
            }


        }  /**
         ** end of excusal for loop
         */


        BooleanExpression recordsForServiceCompletedCommsFilter = PoolCourtQueries.recordsForServiceCompletedComms();
        final List<PoolCourt> poolCourtDetailListCompleted = Lists.newLinkedList(poolCourtRepository.findAll(
            recordsForServiceCompletedCommsFilter));

        log.info(
            "poolCourtDetailListCompleted Number of Completed Records to process {}",
            poolCourtDetailListCompleted.size()
        );

        for (PoolCourt poolCourtDetailCompletedList : poolCourtDetailListCompleted) {

            log.info("poolCourtDetailListCompleted PART_NO {}", poolCourtDetailCompletedList.getJurorNumber());

            String regionIdCompleteEmail = poolCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();
            String regionIdCompleteSms = poolCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();

            String phone = poolCourtDetailCompletedList.getAltPhoneNumber();

            String email = poolCourtDetailCompletedList.getEmail();

            int emailLength = (email != null
                ?
                email.length()
                :
                    0);
            if (emailLength == 1) {

                poolCourtDetailCompletedList.setEmail(null);

                updateCommsStatusFlagCompleted(poolCourtDetailCompletedList);

            }
            String locCode = poolCourtDetailCompletedList.getCourt().getLocCode();
            String jurorNumber = poolCourtDetailCompletedList.getJurorNumber();
            String reference = poolCourtDetailCompletedList.getJurorNumber();


            //  String apiKey = (poolCourtDetailCompletedList != null ? poolCourtDetailCompletedList.getCourt()
            //  .getCourtRegion().getNotifyAccountKey() : null);
            String regionId = poolCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();
            String regionApikey = myRegionMap.get(regionId);
            log.debug("regionApikey {} ", regionApikey);


            if (regionApikey == null || regionApikey.isEmpty()) {
                log.error("Missing Notify Api Account key Cannot send notify communication: ");
                log.info("Missing Notify Api Account key Cannot send notify communication: ");
                poolCourtDetailCompletedList.setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagCompleted(poolCourtDetailCompletedList);

                continue;
            }


            Map<String, String> personalisationEmail = new HashMap<>();
            personalisationEmail.put(messagePlaceHolderJurorNumber, jurorNumber);
            personalisationEmail.put(messagePlaceHolderlocationCode, locCode);

            Map<String, String> personalisationText = new HashMap<>();
            personalisationText.put(messagePlaceHolderlocationCode, locCode);

            BooleanExpression regionNotifyTriggeredCompleteTemplateEmailFilter =
                RegionNotifyTemplateQueries.regionNotifyTriggeredCompletedTemplateEmailId(
                    regionIdCompleteEmail);
            BooleanExpression regionNotifyTriggeredCompleteTemplateSmsFilter =
                RegionNotifyTemplateQueries.regionNotifyTriggeredCompletedTemplateSmsId(
                    regionIdCompleteSms);

            final List<RegionNotifyTemplate> regionNotifyTriggeredCompletedTemplateListEmail = Lists.newLinkedList(
                regionNotifyTemplateRepository.findAll(regionNotifyTriggeredCompleteTemplateEmailFilter));
            final List<RegionNotifyTemplate> regionNotifyTriggeredCompletedTemplateListSms = Lists.newLinkedList(
                regionNotifyTemplateRepository.findAll(regionNotifyTriggeredCompleteTemplateSmsFilter));


            try {
                /**
                 * send Service Completed email
                 */

                if (poolCourtDetailCompletedList.getEmail() != null) {


                    NotificationClient clientSendEmail = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplate regionNotifyTriggeredCompleteTemplateEmailList :
                        regionNotifyTriggeredCompletedTemplateListEmail) {

                        String emailCompletedTemplateId =
                            regionNotifyTriggeredCompleteTemplateEmailList.getNotifyTemplateId();


                        SendEmailResponse emailResponse = clientSendEmail.sendEmail(
                            emailCompletedTemplateId,
                            email,
                            personalisationEmail,
                            reference
                        );
                        if (emailResponse.getNotificationId() != null) {
                            poolCourtDetailCompletedList.setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagCompleted(poolCourtDetailCompletedList);
                        }

                    }


                } //send email

                /**
                 *  send sms
                 */

                if (poolCourtDetailCompletedList.getAltPhoneNumber() != null
                    && poolCourtDetailCompletedList.getEmail() == null) {


                    NotificationClient clientSendSms = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplate regionNotifyTriggeredCompletedTemplateSmsList :
                        regionNotifyTriggeredCompletedTemplateListSms) {

                        String smsCompletedTemplateId =
                            regionNotifyTriggeredCompletedTemplateSmsList.getNotifyTemplateId();


                        SendSmsResponse smsResponse = clientSendSms.sendSms(
                            smsCompletedTemplateId,
                            phone,
                            personalisationText,
                            reference
                        );
                        if (smsResponse.getNotificationId() != null) {
                            poolCourtDetailCompletedList.setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagCompleted(poolCourtDetailCompletedList);
                        }

                    }

                } // send sms with no proxy

                /**
                 * end of second try catch
                 */

            } catch (NotificationClientException e) {

                log.error("Failed to send via Notify: {}", e);
                log.trace("Unable to send notify: {}", e.getHttpResult());
                log.info("Unable to send notify: {}", e.getHttpResult());
                poolCourtDetailCompletedList.setServiceCompCommsStatus(updateMessageStatusNotSent);

                updateCommsStatusFlagCompleted(poolCourtDetailCompletedList);
            } catch (Exception e) {
                log.info("Unexpected exception: {}", e);
            }


        }  /**
         *
         * end of completed service for loop
         */


        BooleanExpression welshRecordsForExcusalCommsFilter = PoolCourtQueries.welshRecordsForExcusalComms();
        final List<PoolCourt> welshPoolCourtDetailListExcusal = Lists.newLinkedList(poolCourtRepository.findAll(
            welshRecordsForExcusalCommsFilter));


        log.info(
            "welshPoolCourtDetailListExcusal Number of Welsh Excusal records to process {}",
            welshPoolCourtDetailListExcusal.size()
        );


        for (PoolCourt welshPoolCourtDetailExcusalList : welshPoolCourtDetailListExcusal) {

            log.info("welshPoolCourtDetailListExcusal PART_NO {}", welshPoolCourtDetailExcusalList.getJurorNumber());

            String welshRegionIdExcusalSms = welshPoolCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();
            String welshRegionIdExcusalEmail =
                welshPoolCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();

            String phone = welshPoolCourtDetailExcusalList.getAltPhoneNumber();

            String email = welshPoolCourtDetailExcusalList.getEmail();

            int emailLength = (email != null
                ?
                email.length()
                :
                    0);
            if (emailLength == 1) {

                welshPoolCourtDetailExcusalList.setEmail(null);
                updateCommsStatusFlagExcusalWelsh(welshPoolCourtDetailExcusalList);
            }


            String locCode = welshPoolCourtDetailExcusalList.getCourt().getLocCode();
            String firstName = welshPoolCourtDetailExcusalList.getFirstName();
            String lastName = welshPoolCourtDetailExcusalList.getLastName();
            String jurorNumber = welshPoolCourtDetailExcusalList.getJurorNumber();
            String courtAddress = welshPoolCourtDetailExcusalList.getCourt().getLocationAddress();
            String courtPhone = welshPoolCourtDetailExcusalList.getCourt().getLocPhone();
            String courtName = welshPoolCourtDetailExcusalList.getCourt().getLocCourtName();
            String reference = welshPoolCourtDetailExcusalList.getJurorNumber();

            //    String apiKey = (welshPoolCourtDetailExcusalList != null ? welshPoolCourtDetailExcusalList.getCourt
            //    ().getCourtRegion().getNotifyAccountKey() : null);
            String regionId = welshPoolCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();
            String regionApikey = myRegionMap.get(regionId);
            log.info("regionApikey {} ", regionApikey);


            if (regionApikey == null || regionApikey.isEmpty()) {
                log.error("Missing Notify Api Account key Cannot send notify communication: ");
                log.info("Missing Notify Api Account key Cannot send notify communication: ");

                welshPoolCourtDetailExcusalList.setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagExcusalWelsh(welshPoolCourtDetailExcusalList);

                continue;
            }


            Map<String, String> personalisationEmail = new HashMap<>();
            personalisationEmail.put(messagePlaceHolderFirstName, firstName);
            personalisationEmail.put(messagePlaceHolderLastName, lastName);
            personalisationEmail.put(messagePlaceHolderJurorNumber, jurorNumber);
            personalisationEmail.put(messagePlaceHolderCourtAddress, courtAddress);
            personalisationEmail.put(messagePlaceHolderlocationCode, locCode);

            Map<String, String> personalisationText = new HashMap<>();
            personalisationText.put(messagePlaceHolderCourtName, courtName);
            personalisationText.put(messagePlaceHolderCourtPhone, courtPhone);
            personalisationText.put(messagePlaceHolderlocationCode, locCode);

            String isExcusalEmailWelsh = "Y";
            String isExcusalSmsWelsh = "Y";


            BooleanExpression welshRegionNotifyTriggeredExcusalTemplateSmsFilter =
                RegionNotifyTemplateQueries.welshRegionNotifyTriggeredExcusalTemplateSmsId(
                    welshRegionIdExcusalSms,
                    isExcusalSmsWelsh
                );
            BooleanExpression welshRegionNotifyTriggeredExcusalTemplateEmailFilter =
                RegionNotifyTemplateQueries.welshRegionNotifyTriggeredExcusalTemplateEmailId(
                    welshRegionIdExcusalEmail,
                    isExcusalEmailWelsh
                );


            final List<RegionNotifyTemplate> welshRegionNotifyTriggeredExcusalTemplateListSms = Lists.newLinkedList(
                regionNotifyTemplateRepository.findAll(welshRegionNotifyTriggeredExcusalTemplateSmsFilter));
            final List<RegionNotifyTemplate> welshRegionNotifyTriggeredExcusalTemplateListEmail = Lists.newLinkedList(
                regionNotifyTemplateRepository.findAll(welshRegionNotifyTriggeredExcusalTemplateEmailFilter));


            try {

                /**
                 *  send welsh excusal email
                 */

                if (welshPoolCourtDetailExcusalList.getEmail() != null) {


                    NotificationClient clientSendEmail = new NotificationClient(regionApikey, gotProxy);


                    for (RegionNotifyTemplate welshRegionNotifyTriggeredExcusalTemplateEmailList :
                        welshRegionNotifyTriggeredExcusalTemplateListEmail) {

                        String welshEmailExcusalTemplateId =
                            welshRegionNotifyTriggeredExcusalTemplateEmailList.getNotifyTemplateId();
                        SendEmailResponse emailResponse = clientSendEmail.sendEmail(
                            welshEmailExcusalTemplateId,
                            email,
                            personalisationEmail,
                            reference
                        );

                        if (emailResponse.getNotificationId() != null) {
                            welshPoolCourtDetailExcusalList.setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagExcusalWelsh(welshPoolCourtDetailExcusalList);
                        }

                    }

                } // send  email


                /**
                 *  send welsh sms
                 */


                if (welshPoolCourtDetailExcusalList.getAltPhoneNumber() != null
                    && welshPoolCourtDetailExcusalList.getEmail() == null) {


                    NotificationClient clientSendSms = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplate welshRegionNotifyTriggeredExcusalTemplateSmsList :
                        welshRegionNotifyTriggeredExcusalTemplateListSms) {

                        String welshSmsExcusalTemplateId =
                            welshRegionNotifyTriggeredExcusalTemplateSmsList.getNotifyTemplateId();


                        SendSmsResponse smsResponse = clientSendSms.sendSms(
                            welshSmsExcusalTemplateId,
                            phone,
                            personalisationText,
                            reference
                        );
                        if (smsResponse.getNotificationId() != null) {
                            welshPoolCourtDetailExcusalList.setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagExcusalWelsh(welshPoolCourtDetailExcusalList);
                        }

                    }

                } // send sms

                /**
                 *  end of welsh excusal try catch
                 */

            } catch (NotificationClientException e) {

                log.error("Failed to send via Notify: {}", e);
                log.trace("Unable to send notify: {}", e.getHttpResult());
                log.info("Unable to send notify: {}", e.getHttpResult());
                welshPoolCourtDetailExcusalList.setServiceCompCommsStatus(updateMessageStatusNotSent);

                poolCourtRepository.save(welshPoolCourtDetailExcusalList);
                updateCommsStatusFlagExcusalWelsh(welshPoolCourtDetailExcusalList);
            } catch (Exception e) {
                log.info("Unexpected exception: {}", e);
            }


        }  /**
         ** end of welsh excusal for loop
         */

        BooleanExpression welshRecordsForServiceCompletedCommsFilter =
            PoolCourtQueries.welshRecordsForServiceCompletedComms();
        final List<PoolCourt> welshPoolCourtDetailListCompleted = Lists.newLinkedList(poolCourtRepository.findAll(
            welshRecordsForServiceCompletedCommsFilter));

        log.info(
            "welshPoolCourtDetailListCompleted Number of Welsh Completed records to process {}",
            welshPoolCourtDetailListCompleted.size()
        );

        for (PoolCourt welshPoolCourtDetailCompletedList : welshPoolCourtDetailListCompleted) {

            log.info(
                "welshPoolCourtDetailListCompleted PART_NO {}",
                welshPoolCourtDetailCompletedList.getJurorNumber()
            );

            String welshRegionIdCompleteEmail =
                welshPoolCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();
            String welshRegionIdCompleteSms =
                welshPoolCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();

            String phone = welshPoolCourtDetailCompletedList.getAltPhoneNumber();


            String email = welshPoolCourtDetailCompletedList.getEmail();

            int emailLength = (email != null
                ?
                email.length()
                :
                    0);
            if (emailLength == 1) {

                welshPoolCourtDetailCompletedList.setEmail(null);
                updateCommsStatusFlagCompletedWelsh(welshPoolCourtDetailCompletedList);

            }
            String locCode = welshPoolCourtDetailCompletedList.getCourt().getLocCode();
            String jurorNumber = welshPoolCourtDetailCompletedList.getJurorNumber();
            String reference = welshPoolCourtDetailCompletedList.getJurorNumber();


            //  String apiKey = (welshPoolCourtDetailCompletedList != null ? welshPoolCourtDetailCompletedList
            //  .getCourt().getCourtRegion().getNotifyAccountKey() : null);

            String regionId = welshPoolCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();
            String regionApikey = myRegionMap.get(regionId);
            log.debug("regionApikey {} ", regionApikey);

            if (regionApikey == null || regionApikey.isEmpty()) {
                log.error("Missing Notify Api Account key Cannot send notify communication: ");
                log.info("Missing Notify Api Account key Cannot send notify communication: ");
                welshPoolCourtDetailCompletedList.setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagCompletedWelsh(welshPoolCourtDetailCompletedList);

                continue;
            }


            Map<String, String> personalisationEmail = new HashMap<>();
            personalisationEmail.put(messagePlaceHolderJurorNumber, jurorNumber);
            personalisationEmail.put(messagePlaceHolderlocationCode, locCode);

            Map<String, String> personalisationText = new HashMap<>();
            personalisationText.put(messagePlaceHolderlocationCode, locCode);

            String isCompletedEmailWelsh = "Y";
            String isCompletedSmsWelsh = "Y";

            BooleanExpression welshRegionNotifyTriggeredCompleteTemplateEmailFilter =
                RegionNotifyTemplateQueries.welshRegionNotifyTriggeredCompletedTemplateEmailId(
                    welshRegionIdCompleteEmail,
                    isCompletedEmailWelsh
                );
            BooleanExpression welshRegionNotifyTriggeredCompleteTemplateSmsFilter =
                RegionNotifyTemplateQueries.welshRegionNotifyTriggeredCompletedTemplateSmsId(
                    welshRegionIdCompleteSms,
                    isCompletedSmsWelsh
                );


            final List<RegionNotifyTemplate> welshRegionNotifyTriggeredCompletedTemplateListEmail = Lists.newLinkedList(
                regionNotifyTemplateRepository.findAll(welshRegionNotifyTriggeredCompleteTemplateEmailFilter));
            final List<RegionNotifyTemplate> welshRegionNotifyTriggeredCompletedTemplateListSms = Lists.newLinkedList(
                regionNotifyTemplateRepository.findAll(welshRegionNotifyTriggeredCompleteTemplateSmsFilter));


            try {
                /**
                 * send welsh Service Completed email
                 */

                if (welshPoolCourtDetailCompletedList.getEmail() != null) {

                    NotificationClient clientSendEmail = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplate welshRegionNotifyTriggeredCompleteTemplateEmailList :
                        welshRegionNotifyTriggeredCompletedTemplateListEmail) {

                        String welshEmailCompletedTemplateId =
                            welshRegionNotifyTriggeredCompleteTemplateEmailList.getNotifyTemplateId();


                        SendEmailResponse emailResponse = clientSendEmail.sendEmail(
                            welshEmailCompletedTemplateId,
                            email,
                            personalisationEmail,
                            reference
                        );
                        if (emailResponse.getNotificationId() != null) {
                            welshPoolCourtDetailCompletedList.setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagCompletedWelsh(welshPoolCourtDetailCompletedList);
                        }

                    }

                }


                /**
                 *  send welsh  sms
                 */

                if (welshPoolCourtDetailCompletedList.getAltPhoneNumber() != null
                    && welshPoolCourtDetailCompletedList.getEmail() == null) {


                    NotificationClient clientSendSms = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplate welshRegionNotifyTriggeredCompletedTemplateSmsList :
                        welshRegionNotifyTriggeredCompletedTemplateListSms) {

                        String welshSmsCompletedTemplateId =
                            welshRegionNotifyTriggeredCompletedTemplateSmsList.getNotifyTemplateId();


                        SendSmsResponse smsResponse = clientSendSms.sendSms(
                            welshSmsCompletedTemplateId,
                            phone,
                            personalisationText,
                            reference
                        );
                        if (smsResponse.getNotificationId() != null) {
                            welshPoolCourtDetailCompletedList.setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagCompletedWelsh(welshPoolCourtDetailCompletedList);
                        }

                    }


                }  // end welsh service completed sms statement

                /**
                 * end of  welsh second try catch
                 */

            } catch (NotificationClientException e) {

                log.error("Failed to send via Notify: {}", e);
                log.trace("Unable to send notify: {}", e.getHttpResult());
                log.info("Unable to send notify: {}", e.getHttpResult());
                welshPoolCourtDetailCompletedList.setServiceCompCommsStatus(updateMessageStatusNotSent);

                updateCommsStatusFlagCompletedWelsh(welshPoolCourtDetailCompletedList);

            } catch (Exception e) {
                log.info("Unexpected exception: {}", e);
            }


        }  /**
         *
         * end of welsh completed service for loop
         */

        log.info("Excused Completed Court Comms Processing : Finished - {}", dateFormat.format(new Date()));
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

    private void updateCommsStatusFlagExcusal(PoolCourt poolCourtDetailExcusalList) {
        try {
            log.trace("Inside update....");
            poolCourtRepository.save(poolCourtDetailExcusalList);
            log.trace("Updating service_comp_comms_status ");
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (poolCourtDetailExcusalList.getServiceCompCommsStatus() == null) {
                log.trace(
                    "ServiceCompCommsStatus is : {} - logging error",
                    poolCourtDetailExcusalList.getServiceCompCommsStatus()
                );
                log.info(
                    "ServiceCompCommsStatus is : {} - logging error",
                    poolCourtDetailExcusalList.getServiceCompCommsStatus()
                );
                log.error(
                    "Failed to update db to {}. Manual update required. {}",
                    poolCourtDetailExcusalList.getServiceCompCommsStatus(),
                    cause.toString()
                );
            } else {
                log.trace(
                    "notifications is : {} - throwing excep",
                    poolCourtDetailExcusalList.getServiceCompCommsStatus()
                );
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + poolCourtDetailExcusalList.getServiceCompCommsStatus()
                        + ". Manual update required. ",
                    cause
                );
            }
        }


    }

    private void updateCommsStatusFlagCompleted(PoolCourt poolCourtDetailCompletedList) {
        try {
            log.trace("Inside update....");
            poolCourtRepository.save(poolCourtDetailCompletedList);
            log.trace("Updating service_comp_comms_status ");
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (poolCourtDetailCompletedList.getServiceCompCommsStatus() == null) {
                log.trace(
                    "ServiceCompCommsStatus is : {} - logging error",
                    poolCourtDetailCompletedList.getServiceCompCommsStatus()
                );
                log.info(
                    "ServiceCompCommsStatus is : {} - logging error",
                    poolCourtDetailCompletedList.getServiceCompCommsStatus()
                );
                log.error(
                    "Failed to update db to {}. Manual update required. {}",
                    poolCourtDetailCompletedList.getServiceCompCommsStatus(),
                    cause.toString()
                );
            } else {
                log.trace(
                    "notifications is : {} - throwing excep",
                    poolCourtDetailCompletedList.getServiceCompCommsStatus()
                );
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + poolCourtDetailCompletedList.getServiceCompCommsStatus() + ". Manual update required. ",
                    cause
                );
            }
        }


    }

    private void updateCommsStatusFlagExcusalWelsh(PoolCourt welshPoolCourtDetailExcusalList) {
        try {
            log.trace("Inside update....");
            poolCourtRepository.save(welshPoolCourtDetailExcusalList);
            log.trace("Updating service_comp_comms_status ");
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (welshPoolCourtDetailExcusalList.getServiceCompCommsStatus() == null) {
                log.trace(
                    "ServiceCompCommsStatus is : {} - logging error",
                    welshPoolCourtDetailExcusalList.getServiceCompCommsStatus()
                );
                log.info(
                    "ServiceCompCommsStatus is : {} - logging error",
                    welshPoolCourtDetailExcusalList.getServiceCompCommsStatus()
                );
                log.error(
                    "Failed to update db to {}. Manual update required. {}",
                    welshPoolCourtDetailExcusalList.getServiceCompCommsStatus(),
                    cause.toString()
                );
            } else {
                log.trace(
                    "notifications is : {} - throwing excep",
                    welshPoolCourtDetailExcusalList.getServiceCompCommsStatus()
                );
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + welshPoolCourtDetailExcusalList.getServiceCompCommsStatus() + ". Manual update required. ",
                    cause
                );
            }
        }


    }

    private void updateCommsStatusFlagCompletedWelsh(PoolCourt welshPoolCourtDetailCompletedList) {
        try {
            log.trace("Inside update....");
            poolCourtRepository.save(welshPoolCourtDetailCompletedList);
            log.trace("Updating service_comp_comms_status ");
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (welshPoolCourtDetailCompletedList.getServiceCompCommsStatus() == null) {
                log.trace(
                    "ServiceCompCommsStatus is : {} - logging error",
                    welshPoolCourtDetailCompletedList.getServiceCompCommsStatus()
                );
                log.info(
                    "ServiceCompCommsStatus is : {} - logging error",
                    welshPoolCourtDetailCompletedList.getServiceCompCommsStatus()
                );
                log.error(
                    "Failed to update db to {}. Manual update required. {}",
                    welshPoolCourtDetailCompletedList.getServiceCompCommsStatus(),
                    cause.toString()
                );
            } else {
                log.trace(
                    "notifications is : {} - throwing excep",
                    welshPoolCourtDetailCompletedList.getServiceCompCommsStatus()
                );
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + welshPoolCourtDetailCompletedList.getServiceCompCommsStatus() + ". Manual update required. ",
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

            log.info("CourtRegions {}", courtRegion.getRegionId());

        }

        return regionIds;
    }


}







