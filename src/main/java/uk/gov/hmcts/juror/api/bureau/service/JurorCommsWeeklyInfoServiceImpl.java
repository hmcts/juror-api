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
public class JurorCommsWeeklyInfoServiceImpl implements BureauProcessService {


    private static final Integer NOTIFICATION_SENT = 1;
    private final JurorCommsNotificationService jurorCommsNotificationService;
    private final PoolRepository poolRepository;

    @Autowired
    public JurorCommsWeeklyInfoServiceImpl(
        final JurorCommsNotificationService jurorCommsNotificationService,
        final PoolRepository poolRepository) {
        Assert.notNull(jurorCommsNotificationService, "JurorCommsNotificationService cannot be null.");
        Assert.notNull(poolRepository, "PoolRepository cannot be null.");
        this.jurorCommsNotificationService = jurorCommsNotificationService;
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
        log.info("Informational Comms Processing : Started - {}", dateFormat.format(new Date()));

        BooleanExpression infomationalCommsFilter = PoolQueries.awaitingInfoComms();

        final List<Pool> pooldetailList = Lists.newLinkedList(poolRepository.findAll(infomationalCommsFilter));

        log.debug("pooldetailList {}", pooldetailList.size());

        int infoCommsSent = 0;
        int noEmailAddress = 0;
        int infoCommsfailed = 0;
        for (Pool poolDetail : pooldetailList) {

            log.trace("Informational Comms Service :  jurorNumber {}", poolDetail.getJurorNumber());
            try {
                //Email
                if (poolDetail.getEmail() != null) {
                    jurorCommsNotificationService.sendJurorComms(poolDetail, JurorCommsNotifyTemplateType.COMMS,
                        null, null, false
                    );
                    infoCommsSent++;
                } else {
                    log.debug(
                        "Informational Comms Service :  Email Address not found for {}",
                        poolDetail.getJurorNumber()
                    );
                    noEmailAddress++;
                }
                update(poolDetail);

            } catch (JurorCommsNotificationServiceException e) {
                log.error(
                    "Unable to send Informational comms for {} : {} {}",
                    poolDetail.getJurorNumber(),
                    e.getMessage(),
                    e.getCause().toString()
                );
                infoCommsfailed++;
            } catch (Exception e) {
                log.error("Informational Comms Processing : Juror Comms failed : {}", e.getMessage());
                infoCommsfailed++;
            }
        }
        log.info("Informational Comms Service : Summary, identified:{}, sent:{}, failed:{}, no email address: {}",
            pooldetailList.size(), infoCommsSent, infoCommsfailed, noEmailAddress
        );
        log.info("Informational Comms Processing : Finished - {}", dateFormat.format(new Date()));
    }

    /***
     * Updates pool notifciation.
     * @param poolDetails
     */
    private void update(Pool poolDetails) {

        try {
            log.trace("Informational Comms : Inside update .....");
            poolDetails.setNotifications(NOTIFICATION_SENT);
            poolRepository.save(poolDetails);
            log.trace("Informational Comms : Updating pool notification as sent ({})... ", NOTIFICATION_SENT);
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            throw new JurorCommsNotificationServiceException(
                "Failed to update db to " + NOTIFICATION_SENT + ". Manual update required. ",
                cause
            );
        }
    }
}
