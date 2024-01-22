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
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolQueries;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;

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
    private final PoolRepository poolRepository;
    private final AppSettingService appSetting;

    @Autowired
    public JurorCommsSentToCourtServiceImpl(
        final JurorCommsNotificationService jurorCommsNotificationService,
        final AppSettingService appSetting,
        final PoolRepository poolRepository) {
        Assert.notNull(jurorCommsNotificationService, "JurorCommsNotificationService cannot be null.");
        Assert.notNull(poolRepository, "PoolRepository cannot be null.");
        Assert.notNull(appSetting, "AppSettingService cannot be null.");
        this.jurorCommsNotificationService = jurorCommsNotificationService;
        this.appSetting = appSetting;
        this.poolRepository = poolRepository;
    }

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror.pool table and sends the appropriate email notifications to
     * the juror for juror where they have been transferred to court.
     */
    @Override
    @Transactional
    public void process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Sent To Court Comms Processing : Started - {}", dateFormat.format(new Date()));

        BooleanExpression sentToCourtFilter = PoolQueries.awaitingSentToCourtComms();

        final List<Pool> pooldetailList = Lists.newLinkedList(poolRepository.findAll(sentToCourtFilter));

        log.debug("pooldetailList {}", pooldetailList.size());

        Integer notificationsSent;
        for (Pool poolDetail : pooldetailList) {

            notificationsSent = poolDetail.getNotifications();
            log.trace("Sent To Court Comms Service :  jurorNumber {}", poolDetail.getJurorNumber());
            try {
                //Email
                if (poolDetail.getEmail() != null && !notificationsSent.equals(EMAIL_NOTIFICATION_SENT)) {
                    jurorCommsNotificationService.sendJurorComms(poolDetail, JurorCommsNotifyTemplateType.SENT_TO_COURT,
                        null, null, false
                    );
                    notificationsSent = EMAIL_NOTIFICATION_SENT;
                }

                //Send SMS only if there has not been an email sent
                if (poolDetail.getAltPhoneNumber() != null && !notificationsSent.equals(EMAIL_NOTIFICATION_SENT)
                    && appSetting.getSendEmailOrSms() == SEND_EMAIL_OR_SMS) {


                    jurorCommsNotificationService.sendJurorCommsSms(
                        poolDetail,
                        JurorCommsNotifyTemplateType.SENT_TO_COURT,
                        null,
                        null,
                        true
                    );
                }

                // Send SMS
                if (poolDetail.getAltPhoneNumber() != null && appSetting.getSendEmailOrSms() != SEND_EMAIL_OR_SMS) {


                    jurorCommsNotificationService.sendJurorCommsSms(
                        poolDetail,
                        JurorCommsNotifyTemplateType.SENT_TO_COURT,
                        null,
                        null,
                        true
                    );

                }

                //update regardless - stop processing next time.
                poolDetail.setNotifications(ALL_NOTIFICATION_SENT);
                notificationsSent = ALL_NOTIFICATION_SENT;
                update(poolDetail);

            } catch (JurorCommsNotificationServiceException e) {
                log.error(
                    "Unable to send sent to court comms for {} : {} {}",
                    poolDetail.getJurorNumber(),
                    e.getMessage(),
                    e.getCause().toString()
                );
                if (notificationsSent.equals(EMAIL_NOTIFICATION_SENT)) {
                    poolDetail.setNotifications(notificationsSent);
                    update(poolDetail);
                }
            } catch (Exception e) {
                log.error("Sent To Court Comms Processing : Juror Comms failed : {}", e.getMessage());
            }
        }
        log.info("Sent To Court Comms Processing : Finished - {}", dateFormat.format(new Date()));
    }

    /***
     * Updates pool notification.
     * @param poolDetails
     */
    private void update(Pool poolDetails) {
        try {
            log.trace("Inside update .....");
            poolRepository.save(poolDetails);
            log.trace("Updating pool notification as sent ({})... ", poolDetails.getNotifications());
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            if (poolDetails.getNotifications().equals(EMAIL_NOTIFICATION_SENT)) {
                log.trace("notifications is : {} - logging error", poolDetails.getNotifications());
                log.error("Failed to update db to {}. Manual update required. {}", poolDetails.getNotifications(),
                    cause.toString()
                );
            } else {
                log.trace("notifications is : {} - throwing excep", poolDetails.getNotifications());
                throw new JurorCommsNotificationServiceException(
                    "Failed to update db to "
                        + poolDetails.getNotifications() + ". Manual update required. ",
                    cause
                );
            }
        }
    }

}
