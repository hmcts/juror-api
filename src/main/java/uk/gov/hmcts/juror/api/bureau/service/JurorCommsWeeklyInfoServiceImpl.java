package uk.gov.hmcts.juror.api.bureau.service;


import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolQueries;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.utils.NotifyUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link BureauProcessService}.
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class JurorCommsWeeklyInfoServiceImpl implements BureauProcessService {


    private static final Integer NOTIFICATION_SENT = 1;
    private final JurorCommsNotificationService jurorCommsNotificationService;
    private final JurorPoolRepository jurorRepository;

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror table and sends the appropriate email notifications to
     * the juror for juror where they have been transferred to court.
     */
    @Override
    @Transactional
    public SchedulerServiceClient.Result process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Informational Comms Processing : Started - {}", dateFormat.format(new Date()));

        BooleanExpression infomationalCommsFilter = JurorPoolQueries.awaitingInfoComms();

        final List<JurorPool> jurordetailList = Lists.newLinkedList(jurorRepository.findAll(infomationalCommsFilter));

        log.debug("jurordetailList {}", jurordetailList.size());

        int infoCommsSent = 0;
        int noEmailAddress = 0;
        int invalidEmailAddress = 0;
        int infoCommsfailed = 0;
        for (JurorPool jurorDetail : jurordetailList) {

            log.trace("Informational Comms Service :  jurorNumber {}", jurorDetail.getJurorNumber());
            try {
                //Email
                if (jurorDetail.getJuror().getEmail() != null) {
                    jurorCommsNotificationService.sendJurorComms(jurorDetail, JurorCommsNotifyTemplateType.COMMS,
                        null, null, false
                    );
                    infoCommsSent++;
                } else {
                    log.debug(
                        "Informational Comms Service :  Email Address not found for {}",
                        jurorDetail.getJurorNumber()
                    );
                    noEmailAddress++;
                }
                update(jurorDetail);

            } catch (JurorCommsNotificationServiceException e) {
                if (NotifyUtil.isInvalidEmailAddressError(e.getCause())) {
                    invalidEmailAddress++;
                } else {
                    log.error(
                        "Unable to send Informational comms for {}",
                        jurorDetail.getJurorNumber(), e
                    );
                    infoCommsfailed++;
                }
            } catch (Exception e) {
                log.error("Informational Comms Processing : Juror Comms failed", e);
                infoCommsfailed++;
            }
        }
        log.info("Informational Comms Service : Summary, identified:{}, sent:{}, failed:{}, no email address: {}",
            jurordetailList.size(), infoCommsSent, infoCommsfailed, noEmailAddress
        );
        log.info("Informational Comms Processing : Finished - {}", dateFormat.format(new Date()));
        return new SchedulerServiceClient.Result(
            infoCommsfailed == 0
                ? SchedulerServiceClient.Result.Status.SUCCESS
                : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS, null,
            Map.of(
                "INFO_COMMS_SENT", String.valueOf(infoCommsSent),
                "INFO_COMMS_FAILED", String.valueOf(infoCommsfailed),
                "NO_EMAIL_ADDRESS", String.valueOf(noEmailAddress),
                "INVALID_EMAIL_ADDRESS", String.valueOf(invalidEmailAddress)
            ));
    }

    /**
     * Updates pool notifciation.
     */
    private void update(JurorPool jurorDetails) {

        try {
            log.trace("Informational Comms : Inside update .....");
            jurorDetails.getJuror().setNotifications(NOTIFICATION_SENT);
            jurorRepository.save(jurorDetails);
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
