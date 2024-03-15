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
import uk.gov.hmcts.juror.api.moj.domain.*;
import uk.gov.hmcts.juror.api.moj.repository.*;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;
import uk.gov.hmcts.juror.api.bureau.service.BureauProcessService;
import uk.gov.hmcts.juror.api.config.NotifyConfigurationProperties;
import uk.gov.hmcts.juror.api.config.NotifyRegionsConfigurationProperties;
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



    private final JurorPoolRepository jurorRepository;
    private final AppSettingService appSetting;

    private final CourtRegionModRepository courtRegionModRepository;
    private final RegionNotifyTemplateRepositoryMod regionNotifyTemplateRepositoryMod;
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
        this.appSetting = appSetting;
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


        BooleanExpression recordsForExcusalCommsFilter = JurorPoolQueries.recordsForExcusalComms();
        final List<JurorPool> jurorCourtDetailListExcusal = Lists.newLinkedList(jurorRepository.findAll(
            recordsForExcusalCommsFilter));

        log.info(
            "JurorCourtDetailListExcusal Number of Excusal Records to process {}",
            jurorCourtDetailListExcusal.size()
        );


        for (JurorPool jurorCourtDetailExcusalList : jurorCourtDetailListExcusal) {

            log.info("poolCourtDetailExcusalList PART_NO : {}", jurorCourtDetailExcusalList.getJurorNumber());
            log.info("Excusal Date: {}", jurorCourtDetailExcusalList.getJuror().getExcusalDate());


            String regionIdExcusalSms = jurorCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();
            String regionIdExcusalEmail = jurorCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();

            String phone = jurorCourtDetailExcusalList.getJuror().getAltPhoneNumber();

            String email = jurorCourtDetailExcusalList.getJuror().getEmail();

            int emailLength = (email != null
                ?
                email.length()
                :
                    0);
            if (emailLength == 1) {

                jurorCourtDetailExcusalList.getJuror().setEmail(null);
                updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);
            }

            String locCode = jurorCourtDetailExcusalList.getCourt().getLocCode();
            String firstName = jurorCourtDetailExcusalList.getJuror().getFirstName();
            String lastName = jurorCourtDetailExcusalList.getJuror().getLastName();
            String jurorNumber = jurorCourtDetailExcusalList.getJurorNumber();
            String courtAddress = jurorCourtDetailExcusalList.getCourt().getLocationAddress();
            String courtPhone = jurorCourtDetailExcusalList.getCourt().getLocPhone();
            String courtName = jurorCourtDetailExcusalList.getCourt().getLocCourtName();
            String reference = jurorCourtDetailExcusalList.getJurorNumber();


            String regionId = jurorCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();
            String regionApikey = myRegionMap.get(regionId);
            log.debug("regionApikey {} ", regionApikey);


            if (regionApikey == null || regionApikey.isEmpty()) {
                log.error("Missing Notify Api Account key Cannot send notify communication: ");
                log.info("Missing Notify Api Account key Cannot send notify communication: ");

                jurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);

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
                RegionNotifyTemplateQueriesMod.regionNotifyTriggeredExcusalTemplateSmsId(
                    regionIdExcusalSms);
            BooleanExpression regionNotifyTriggeredExcusalTemplateEmailFilter =
                RegionNotifyTemplateQueriesMod.regionNotifyTriggeredExcusalTemplateEmailId(
                    regionIdExcusalEmail);


            final List<RegionNotifyTemplateMod> regionNotifyTriggeredExcusalTemplateListSms = Lists.newLinkedList(
                regionNotifyTemplateRepositoryMod.findAll(regionNotifyTriggeredExcusalTemplateSmsFilter));
            final List<RegionNotifyTemplateMod> regionNotifyTriggeredExcusalTemplateListEmail = Lists.newLinkedList(
                regionNotifyTemplateRepositoryMod.findAll(regionNotifyTriggeredExcusalTemplateEmailFilter));


            try {

                /**
                 *  send excusal email
                 */

                if (jurorCourtDetailExcusalList.getJuror().getEmail() != null) {

                    NotificationClient clientSendEmail = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplateMod regionNotifyTriggeredExcusalTemplateEmailList :
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
                            jurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);
                        }

                    }

                } //send email  end of if statement

                /**
                 *  send excusal sms
                 */


                if (jurorCourtDetailExcusalList.getJuror().getAltPhoneNumber() != null
                    && jurorCourtDetailExcusalList.getJuror().getEmail() == null) {

                    NotificationClient clientSendSms = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplateMod regionNotifyTriggeredExcusalTemplateSmsList :
                        regionNotifyTriggeredExcusalTemplateListSms) {

                        String smsExcusalTemplateId = regionNotifyTriggeredExcusalTemplateSmsList.getNotifyTemplateId();


                        SendSmsResponse smsResponse = clientSendSms.sendSms(
                            smsExcusalTemplateId,
                            phone,
                            personalisationText,
                            reference
                        );
                        if (smsResponse.getNotificationId() != null) {
                            jurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);
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
                jurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);

                updateCommsStatusFlagExcusal(jurorCourtDetailExcusalList);
            } catch (Exception e) {
                log.info("Unexpected exception: {}", e);
            }


        }  /**
         ** end of excusal for loop
         */


        BooleanExpression recordsForServiceCompletedCommsFilter = JurorPoolQueries.recordsForServiceCompletedComms();
        final List<JurorPool> jurorCourtDetailListCompleted = Lists.newLinkedList(jurorRepository.findAll(
            recordsForServiceCompletedCommsFilter));

        log.info(
            "jurorCourtDetailListCompleted Number of Completed Records to process {}",
            jurorCourtDetailListCompleted.size()
        );

        for (JurorPool jurorCourtDetailCompletedList : jurorCourtDetailListCompleted) {

            log.info("jurorCourtDetailListCompleted PART_NO {}", jurorCourtDetailCompletedList.getJurorNumber());

            String regionIdCompleteEmail = jurorCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();
            String regionIdCompleteSms = jurorCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();

            String phone = jurorCourtDetailCompletedList.getJuror().getAltPhoneNumber();

            String email = jurorCourtDetailCompletedList.getJuror().getEmail();

            int emailLength = (email != null
                ?
                email.length()
                :
                    0);
            if (emailLength == 1) {

                jurorCourtDetailCompletedList.getJuror().setEmail(null);

                updateCommsStatusFlagCompleted(jurorCourtDetailCompletedList);

            }
            String locCode = jurorCourtDetailCompletedList.getCourt().getLocCode();
            String jurorNumber = jurorCourtDetailCompletedList.getJurorNumber();
            String reference = jurorCourtDetailCompletedList.getJurorNumber();


            //  String apiKey = (poolCourtDetailCompletedList != null ? poolCourtDetailCompletedList.getCourt()
            //  .getCourtRegion().getNotifyAccountKey() : null);
            String regionId = jurorCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();
            String regionApikey = myRegionMap.get(regionId);
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

            final List<RegionNotifyTemplateMod> regionNotifyTriggeredCompletedTemplateListEmail = Lists.newLinkedList(
                regionNotifyTemplateRepositoryMod.findAll(regionNotifyTriggeredCompleteTemplateEmailFilter));
            final List<RegionNotifyTemplateMod> regionNotifyTriggeredCompletedTemplateListSms = Lists.newLinkedList(
                regionNotifyTemplateRepositoryMod.findAll(regionNotifyTriggeredCompleteTemplateSmsFilter));


            try {
                /**
                 * send Service Completed email
                 */

                if (jurorCourtDetailCompletedList.getJuror().getEmail() != null) {


                    NotificationClient clientSendEmail = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplateMod regionNotifyTriggeredCompleteTemplateEmailList :
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
                            jurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagCompleted(jurorCourtDetailCompletedList);
                        }

                    }


                } //send email

                /**
                 *  send sms
                 */

                if (jurorCourtDetailCompletedList.getJuror().getAltPhoneNumber() != null
                    && jurorCourtDetailCompletedList.getJuror().getEmail() == null) {


                    NotificationClient clientSendSms = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplateMod regionNotifyTriggeredCompletedTemplateSmsList :
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
                            jurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagCompleted(jurorCourtDetailCompletedList);
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
                jurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);

                updateCommsStatusFlagCompleted(jurorCourtDetailCompletedList);
            } catch (Exception e) {
                log.info("Unexpected exception: {}", e);
            }


        }  /**
         *
         * end of completed service for loop
         */


        BooleanExpression welshRecordsForExcusalCommsFilter = JurorPoolQueries.welshRecordsForExcusalComms();
        final List<JurorPool> welshJurorCourtDetailListExcusal = Lists.newLinkedList(jurorRepository.findAll(
            welshRecordsForExcusalCommsFilter));


        log.info(
            "welshJurorCourtDetailListExcusal Number of Welsh Excusal records to process {}",
            welshJurorCourtDetailListExcusal.size()
        );


        for (JurorPool welshJurorCourtDetailExcusalList : welshJurorCourtDetailListExcusal) {

            log.info("welshJurorCourtDetailListExcusal PART_NO {}", welshJurorCourtDetailExcusalList.getJurorNumber());

            String welshRegionIdExcusalSms = welshJurorCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();
            String welshRegionIdExcusalEmail =
                welshJurorCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();

            String phone = welshJurorCourtDetailExcusalList.getJuror().getAltPhoneNumber();

            String email = welshJurorCourtDetailExcusalList.getJuror().getEmail();

            int emailLength = (email != null
                ?
                email.length()
                :
                    0);
            if (emailLength == 1) {

                welshJurorCourtDetailExcusalList.getJuror().setEmail(null);
                updateCommsStatusFlagExcusalWelsh(welshJurorCourtDetailExcusalList);
            }


            String locCode = welshJurorCourtDetailExcusalList.getCourt().getLocCode();
            String firstName = welshJurorCourtDetailExcusalList.getJuror().getFirstName();
            String lastName = welshJurorCourtDetailExcusalList.getJuror().getLastName();
            String jurorNumber = welshJurorCourtDetailExcusalList.getJurorNumber();
            String courtAddress = welshJurorCourtDetailExcusalList.getCourt().getLocationAddress();
            String courtPhone = welshJurorCourtDetailExcusalList.getCourt().getLocPhone();
            String courtName = welshJurorCourtDetailExcusalList.getCourt().getLocCourtName();
            String reference = welshJurorCourtDetailExcusalList.getJurorNumber();

            //    String apiKey = (welshJurorCourtDetailExcusalList != null ? welshJurorCourtDetailExcusalList.getCourt
            //    ().getCourtRegion().getNotifyAccountKey() : null);
            String regionId = welshJurorCourtDetailExcusalList.getCourt().getCourtRegion().getRegionId();
            String regionApikey = myRegionMap.get(regionId);
            log.info("regionApikey {} ", regionApikey);


            if (regionApikey == null || regionApikey.isEmpty()) {
                log.error("Missing Notify Api Account key Cannot send notify communication: ");
                log.info("Missing Notify Api Account key Cannot send notify communication: ");

                welshJurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagExcusalWelsh(welshJurorCourtDetailExcusalList);

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
                RegionNotifyTemplateQueriesMod.welshRegionNotifyTriggeredExcusalTemplateSmsId(
                    welshRegionIdExcusalSms,
                    isExcusalSmsWelsh
                );
            BooleanExpression welshRegionNotifyTriggeredExcusalTemplateEmailFilter =
                RegionNotifyTemplateQueriesMod.welshRegionNotifyTriggeredExcusalTemplateEmailId(
                    welshRegionIdExcusalEmail,
                    isExcusalEmailWelsh
                );


            final List<RegionNotifyTemplateMod> welshRegionNotifyTriggeredExcusalTemplateListSms = Lists.newLinkedList(
                regionNotifyTemplateRepositoryMod.findAll(welshRegionNotifyTriggeredExcusalTemplateSmsFilter));
            final List<RegionNotifyTemplateMod> welshRegionNotifyTriggeredExcusalTemplateListEmail = Lists.newLinkedList(
                regionNotifyTemplateRepositoryMod.findAll(welshRegionNotifyTriggeredExcusalTemplateEmailFilter));


            try {

                /**
                 *  send welsh excusal email
                 */

                if (welshJurorCourtDetailExcusalList.getJuror().getEmail() != null) {


                    NotificationClient clientSendEmail = new NotificationClient(regionApikey, gotProxy);


                    for (RegionNotifyTemplateMod welshRegionNotifyTriggeredExcusalTemplateEmailList :
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
                            welshJurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagExcusalWelsh(welshJurorCourtDetailExcusalList);
                        }

                    }

                } // send  email


                /**
                 *  send welsh sms
                 */


                if (welshJurorCourtDetailExcusalList.getJuror().getAltPhoneNumber() != null
                    && welshJurorCourtDetailExcusalList.getJuror().getEmail() == null) {


                    NotificationClient clientSendSms = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplateMod welshRegionNotifyTriggeredExcusalTemplateSmsList :
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
                            welshJurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagExcusalWelsh(welshJurorCourtDetailExcusalList);
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
                welshJurorCourtDetailExcusalList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);

                jurorRepository.save(welshJurorCourtDetailExcusalList);
                updateCommsStatusFlagExcusalWelsh(welshJurorCourtDetailExcusalList);
            } catch (Exception e) {
                log.info("Unexpected exception: {}", e);
            }


        }  /**
         ** end of welsh excusal for loop
         */

        BooleanExpression welshRecordsForServiceCompletedCommsFilter =
            JurorPoolQueries.welshRecordsForServiceCompletedComms();
        final List<JurorPool> welshJurorCourtDetailListCompleted = Lists.newLinkedList(jurorRepository.findAll(
            welshRecordsForServiceCompletedCommsFilter));

        log.info(
            "welshJurorCourtDetailListCompleted Number of Welsh Completed records to process {}",
            welshJurorCourtDetailListCompleted.size()
        );

        for (JurorPool welshJurorCourtDetailCompletedList : welshJurorCourtDetailListCompleted) {

            log.info(
                "welshJurorCourtDetailListCompleted PART_NO {}",
                welshJurorCourtDetailCompletedList.getJurorNumber()
            );

            String welshRegionIdCompleteEmail =
                welshJurorCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();
            String welshRegionIdCompleteSms =
                welshJurorCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();

            String phone = welshJurorCourtDetailCompletedList.getJuror().getAltPhoneNumber();


            String email = welshJurorCourtDetailCompletedList.getJuror().getEmail();

            int emailLength = (email != null
                ?
                email.length()
                :
                    0);
            if (emailLength == 1) {

                welshJurorCourtDetailCompletedList.getJuror().setEmail(null);
                updateCommsStatusFlagCompletedWelsh(welshJurorCourtDetailCompletedList);

            }
            String locCode = welshJurorCourtDetailCompletedList.getCourt().getLocCode();
            String jurorNumber = welshJurorCourtDetailCompletedList.getJurorNumber();
            String reference = welshJurorCourtDetailCompletedList.getJurorNumber();


            //  String apiKey = (welshJurorCourtDetailCompletedList != null ? welshJurorCourtDetailCompletedList
            //  .getCourt().getCourtRegion().getNotifyAccountKey() : null);

            String regionId = welshJurorCourtDetailCompletedList.getCourt().getCourtRegion().getRegionId();
            String regionApikey = myRegionMap.get(regionId);
            log.debug("regionApikey {} ", regionApikey);

            if (regionApikey == null || regionApikey.isEmpty()) {
                log.error("Missing Notify Api Account key Cannot send notify communication: ");
                log.info("Missing Notify Api Account key Cannot send notify communication: ");
                welshJurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);
                updateCommsStatusFlagCompletedWelsh(welshJurorCourtDetailCompletedList);

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
                RegionNotifyTemplateQueriesMod.welshRegionNotifyTriggeredCompletedTemplateEmailId(
                    welshRegionIdCompleteEmail,
                    isCompletedEmailWelsh
                );
            BooleanExpression welshRegionNotifyTriggeredCompleteTemplateSmsFilter =
                RegionNotifyTemplateQueriesMod.welshRegionNotifyTriggeredCompletedTemplateSmsId(
                    welshRegionIdCompleteSms,
                    isCompletedSmsWelsh
                );


            final List<RegionNotifyTemplateMod> welshRegionNotifyTriggeredCompletedTemplateListEmail = Lists.newLinkedList(
                regionNotifyTemplateRepositoryMod.findAll(welshRegionNotifyTriggeredCompleteTemplateEmailFilter));
            final List<RegionNotifyTemplateMod> welshRegionNotifyTriggeredCompletedTemplateListSms = Lists.newLinkedList(
                regionNotifyTemplateRepositoryMod.findAll(welshRegionNotifyTriggeredCompleteTemplateSmsFilter));


            try {
                /**
                 * send welsh Service Completed email
                 */

                if (welshJurorCourtDetailCompletedList.getJuror().getEmail() != null) {

                    NotificationClient clientSendEmail = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplateMod welshRegionNotifyTriggeredCompleteTemplateEmailList :
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
                            welshJurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagCompletedWelsh(welshJurorCourtDetailCompletedList);
                        }

                    }

                }


                /**
                 *  send welsh  sms
                 */

                if (welshJurorCourtDetailCompletedList.getJuror().getAltPhoneNumber() != null
                    && welshJurorCourtDetailCompletedList.getJuror().getEmail() == null) {


                    NotificationClient clientSendSms = new NotificationClient(regionApikey, gotProxy);

                    for (RegionNotifyTemplateMod welshRegionNotifyTriggeredCompletedTemplateSmsList :
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
                            welshJurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusSent);
                            updateCommsStatusFlagCompletedWelsh(welshJurorCourtDetailCompletedList);
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
                welshJurorCourtDetailCompletedList.getJuror().setServiceCompCommsStatus(updateMessageStatusNotSent);

                updateCommsStatusFlagCompletedWelsh(welshJurorCourtDetailCompletedList);

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
                        + poolCourtDetailCompletedList.getJuror().getServiceCompCommsStatus() + ". Manual update required. ",
                    cause
                );
            }
        }


    }

    private void updateCommsStatusFlagExcusalWelsh(JurorPool welshJurorCourtDetailExcusalList) {
        try {
            log.trace("Inside update....");
            jurorRepository.save(welshJurorCourtDetailExcusalList);
            log.trace("Updating service_comp_comms_status ");
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (welshJurorCourtDetailExcusalList.getJuror().getServiceCompCommsStatus() == null) {
                log.trace(
                    "ServiceCompCommsStatus is : {} - logging error",
                    welshJurorCourtDetailExcusalList.getJuror().getServiceCompCommsStatus()
                );
                log.info(
                    "ServiceCompCommsStatus is : {} - logging error",
                    welshJurorCourtDetailExcusalList.getJuror().getServiceCompCommsStatus()
                );
                log.error(
                    "Failed to update db to {}. Manual update required. {}",
                    welshJurorCourtDetailExcusalList.getJuror().getServiceCompCommsStatus(),
                    cause.toString()
                );
            } else {
                log.trace(
                    "notifications is : {} - throwing excep",
                    welshJurorCourtDetailExcusalList.getJuror().getServiceCompCommsStatus()
                );
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + welshJurorCourtDetailExcusalList.getJuror().getServiceCompCommsStatus() + ". Manual update required. ",
                    cause
                );
            }
        }


    }

    private void updateCommsStatusFlagCompletedWelsh(JurorPool welshJurorCourtDetailCompletedList) {
        try {
            log.trace("Inside update....");
            jurorRepository.save(welshJurorCourtDetailCompletedList);
            log.trace("Updating service_comp_comms_status ");
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (welshJurorCourtDetailCompletedList.getJuror().getServiceCompCommsStatus() == null) {
                log.trace(
                    "ServiceCompCommsStatus is : {} - logging error",
                    welshJurorCourtDetailCompletedList.getJuror().getServiceCompCommsStatus()
                );
                log.info(
                    "ServiceCompCommsStatus is : {} - logging error",
                    welshJurorCourtDetailCompletedList.getJuror().getServiceCompCommsStatus()
                );
                log.error(
                    "Failed to update db to {}. Manual update required. {}",
                    welshJurorCourtDetailCompletedList.getJuror().getServiceCompCommsStatus(),
                    cause.toString()
                );
            } else {
                log.trace(
                    "notifications is : {} - throwing excep",
                    welshJurorCourtDetailCompletedList.getJuror().getServiceCompCommsStatus()
                );
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + welshJurorCourtDetailCompletedList.getJuror().getServiceCompCommsStatus() + ". Manual update required. ",
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
        List<CourtRegionMod> courtRegions = Lists.newLinkedList(courtRegionModRepository.findAll());
        List<String> regionIds = new ArrayList<>();

        for (CourtRegionMod courtRegion : courtRegions) {
            String courtregionIds = courtRegion.getRegionId();
            regionIds.add(courtregionIds);

            log.info("CourtRegions {}", courtRegion.getRegionId());

        }

        return regionIds;
    }


}







