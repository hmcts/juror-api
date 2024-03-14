package uk.gov.hmcts.juror.api.bureau.service;


import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolQueries;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link BureauProcessService}.
 */
@Slf4j
@Service
public class JurorCommsSentToCourtServiceImpl implements BureauProcessService {


    private static final Integer ALL_NOTIFICATION_SENT = 9;
    private static final Integer EMAIL_NOTIFICATION_SENT = 8;
    private static final Integer SEND_EMAIL_OR_SMS = 1;
    private final JurorCommsNotificationService jurorCommsNotificationService;
    private final JurorPoolRepository jurorRepository;
    private final AppSettingService appSetting;

    @Autowired
    public JurorCommsSentToCourtServiceImpl(
        final JurorCommsNotificationService jurorCommsNotificationService,
        final AppSettingService appSetting,
        final JurorPoolRepository jurorRepository) {
        Assert.notNull(jurorCommsNotificationService, "JurorCommsNotificationService cannot be null.");
        Assert.notNull(jurorRepository, "JurorRepository cannot be null.");
        Assert.notNull(appSetting, "AppSettingService cannot be null.");
        this.jurorCommsNotificationService = jurorCommsNotificationService;
        this.appSetting = appSetting;
        this.jurorRepository = jurorRepository;
    }

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror table and sends the appropriate email notifications to
     * the juror for juror where they have been transferred to court.
     */
    @Override
    @Transactional
    public void process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Sent To Court Comms Processing : Started - {}", dateFormat.format(new Date()));

        BooleanExpression sentToCourtFilter = JurorPoolQueries.awaitingSentToCourtComms();

        final List<JurorPool> jurordetailList = Lists.newLinkedList(jurorRepository.findAll(sentToCourtFilter));

        log.debug("pooldetailList {}", jurordetailList.size());

        Integer notificationsSent;
        for (JurorPool jurorDetail : jurordetailList) {

            notificationsSent = jurorDetail.getJuror().getNotifications();
            log.trace("Sent To Court Comms Service :  jurorNumber {}", jurorDetail.getJurorNumber());
            try {
                //Email
                if (jurorDetail.getJuror().getEmail() != null && !notificationsSent.equals(EMAIL_NOTIFICATION_SENT)) {
                    jurorCommsNotificationService.sendJurorComms(jurorDetail, JurorCommsNotifyTemplateType.SENT_TO_COURT,
                        null, null, false
                    );
                    notificationsSent = EMAIL_NOTIFICATION_SENT;
                }

                //Send SMS only if there has not been an email sent
                if (jurorDetail.getJuror().getAltPhoneNumber() != null && !notificationsSent.equals(EMAIL_NOTIFICATION_SENT)
                    && appSetting.getSendEmailOrSms() == SEND_EMAIL_OR_SMS) {


                    jurorCommsNotificationService.sendJurorCommsSms(
                        jurorDetail,
                        JurorCommsNotifyTemplateType.SENT_TO_COURT,
                        null,
                        null,
                        true
                    );
                }

                // Send SMS
                if (jurorDetail.getJuror().getAltPhoneNumber() != null && appSetting.getSendEmailOrSms() != SEND_EMAIL_OR_SMS) {


                    jurorCommsNotificationService.sendJurorCommsSms(
                        jurorDetail,
                        JurorCommsNotifyTemplateType.SENT_TO_COURT,
                        null,
                        null,
                        true
                    );

                }

                //update regardless - stop processing next time.
                jurorDetail.getJuror().setNotifications(ALL_NOTIFICATION_SENT);
                notificationsSent = ALL_NOTIFICATION_SENT;
                update(jurorDetail);

            } catch (JurorCommsNotificationServiceException e) {
                log.error(
                    "Unable to send sent to court comms for {} : {} {}",
                    jurorDetail.getJurorNumber(),
                    e.getMessage(),
                    e.getCause().toString()
                );
                if (notificationsSent.equals(EMAIL_NOTIFICATION_SENT)) {
                    jurorDetail.getJuror().setNotifications(notificationsSent);
                    update(jurorDetail);
                }
            } catch (Exception e) {
                log.error("Sent To Court Comms Processing : Juror Comms failed : {}", e.getMessage());
            }
        }
        log.info("Sent To Court Comms Processing : Finished - {}", dateFormat.format(new Date()));
    }

    /***
     * Updates juror notification.
     * @param jurorDetails
     */
    private void update(JurorPool jurorDetails) {
        try {
            log.trace("Inside update .....");
            jurorRepository.save(jurorDetails);
            log.trace("Updating Juror notification as sent ({})... ", jurorDetails.getJuror().getNotifications());
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
          // if (poolDetails.getNotifications().equals(EMAIL_NOTIFICATION_SENT)) {
            if (jurorDetails.getJuror().getNotifications()==(EMAIL_NOTIFICATION_SENT)) {
                log.trace("notifications is : {} - logging error", jurorDetails.getJuror().getNotifications());
                log.error("Failed to update db to {}. Manual update required. {}", jurorDetails.getJuror().getNotifications(),
                    cause.toString()
                );
            } else {
                log.trace("notifications is : {} - throwing excep", jurorDetails.getJuror().getNotifications());
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + jurorDetails.getJuror().getNotifications() + ". Manual update required. ",
                    cause
                );
            }
        }
    }

}
