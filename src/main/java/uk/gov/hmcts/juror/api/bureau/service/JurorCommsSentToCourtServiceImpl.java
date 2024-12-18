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
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;
import uk.gov.hmcts.juror.api.moj.utils.NotifyUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link BureauProcessService}.
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class JurorCommsSentToCourtServiceImpl implements BureauProcessService {


    private static final Integer ALL_NOTIFICATION_SENT = 9;
    private static final Integer EMAIL_NOTIFICATION_SENT = 8;
    private static final Integer SEND_EMAIL_OR_SMS = 1;
    private final JurorCommsNotificationService jurorCommsNotificationService;
    private final JurorPoolRepository jurorRepository;
    private final AppSettingService appSetting;

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror table and sends the appropriate email notifications to
     * the juror for juror where they have been transferred to court.
     */
    @SuppressWarnings("checkstyle:LineLength") // false positive
    @Override
    @Transactional
    public SchedulerServiceClient.Result process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Sent To Court Comms Processing : Started - {}", dateFormat.format(new Date()));

        BooleanExpression sentToCourtFilter = JurorPoolQueries.awaitingSentToCourtComms();

        final List<JurorPool> jurordetailList = Lists.newLinkedList(jurorRepository.findAll(sentToCourtFilter));

        log.debug("pooldetailList {}", jurordetailList.size());

        Integer notificationsSent;
        int errorCount = 0;
        int errorInvalidEmailCount = 0;
        int errorInvalidPhoneCount = 0;
        int successCountEmail = 0;
        int successCountSms = 0;
        int errorCountEmail = 0;
        int errorCountSms = 0;
        int successCount = 0;
        for (JurorPool jurorDetail : jurordetailList) {

            notificationsSent = jurorDetail.getJuror().getNotifications();
            log.trace("Sent To Court Comms Service :  jurorNumber {}", jurorDetail.getJurorNumber());
            boolean isEmail = false;
            boolean isSms = false;
            try {
                //Email
                if (jurorDetail.getJuror().getEmail() != null && !notificationsSent.equals(EMAIL_NOTIFICATION_SENT)) {
                    isEmail = true;
                    jurorCommsNotificationService.sendJurorComms(jurorDetail,
                        JurorCommsNotifyTemplateType.SENT_TO_COURT,
                        null, null, false
                    );
                    notificationsSent = EMAIL_NOTIFICATION_SENT;
                    successCountEmail++;
                }

                //Send SMS only if there has not been an email sent
                if (jurorDetail.getJuror().getAltPhoneNumber() != null
                    && !notificationsSent.equals(EMAIL_NOTIFICATION_SENT)
                    && Objects.equals(appSetting.getSendEmailOrSms(), SEND_EMAIL_OR_SMS)) {
                    isSms = true;
                    jurorCommsNotificationService.sendJurorCommsSms(
                        jurorDetail,
                        JurorCommsNotifyTemplateType.SENT_TO_COURT,
                        null,
                        null,
                        true
                    );
                    successCountSms++;
                }

                // Send SMS
                if (jurorDetail.getJuror().getAltPhoneNumber() != null
                    && !Objects.equals(appSetting.getSendEmailOrSms(), SEND_EMAIL_OR_SMS)) {
                    isSms = true;
                    jurorCommsNotificationService.sendJurorCommsSms(
                        jurorDetail,
                        JurorCommsNotifyTemplateType.SENT_TO_COURT,
                        null,
                        null,
                        true
                    );
                    successCountSms++;
                }

                //update regardless - stop processing next time.
                jurorDetail.getJuror().setNotifications(ALL_NOTIFICATION_SENT);
                notificationsSent = ALL_NOTIFICATION_SENT;
                update(jurorDetail);
                successCount++;

            } catch (JurorCommsNotificationServiceException e) {
                boolean isError = false;
                if (isEmail) {
                    if (NotifyUtil.isInvalidEmailAddressError(e.getCause())) {
                        errorInvalidEmailCount++;
                    } else {
                        isError = true;
                        errorCountEmail++;
                    }
                }
                if (isSms) {
                    if (NotifyUtil.isInvalidPhoneNumberError(e.getCause())) {
                        errorInvalidPhoneCount++;
                    } else {
                        isError = true;
                        errorCountSms++;
                    }
                }
                if (isError) {
                    errorCount++;
                    log.error(
                        "Unable to send sent to court comms for {}",
                        jurorDetail.getJurorNumber(), e
                    );
                }
                if (notificationsSent.equals(EMAIL_NOTIFICATION_SENT)) {
                    jurorDetail.getJuror().setNotifications(notificationsSent);
                    update(jurorDetail);
                }
            } catch (Exception e) {
                log.error("Sent To Court Comms Processing : Juror Comms failed : {}", e.getMessage(), e);
                errorCount++;
                if (isEmail) {
                    errorCountEmail++;
                }
                if (isSms) {
                    errorCountSms++;
                }
            }
        }

        SchedulerServiceClient.Result.Status status = errorCount == 0
            ? SchedulerServiceClient.Result.Status.SUCCESS
            : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS;

        // log the results for Dynatrace
        log.info(
            "[JobKey: CRONBATCH_SEND_TO_COURT_COMMS]\n[{}]\nresult={},\nemail_sent={},\nsms_sent={},\nemail_failed={},\nsms_failed={},\ninvalid_email_count={},\ninvalid_phone_count={},\nsuccess_count={},\nerror_count={},\ntotal_jurors={}",
            LocalDateTime.now(),
            status,
            successCountEmail,
            successCountSms,
            errorCountEmail,
            errorCountSms,
            errorInvalidEmailCount,
            errorInvalidPhoneCount,
            successCount,
            errorCount,
            jurordetailList.size()
        );

        log.info("Sent To Court Comms Processing : Finished - {}", dateFormat.format(new Date()));
        return new SchedulerServiceClient.Result(
            status, null,
            Map.of(
                "SUCCESS_COUNT_EMAIL", String.valueOf(successCountEmail),
                "SUCCESS_COUNT_SMS", String.valueOf(successCountSms),
                "ERROR_COUNT_EMAIL", String.valueOf(errorCountEmail),
                "ERROR_COUNT_SMS", String.valueOf(errorCountSms),
                "COUNT_INVALID_EMAIL", String.valueOf(errorInvalidEmailCount),
                "COUNT_INVALID_PHONE", String.valueOf(errorInvalidPhoneCount),
                "SUCCESS_COUNT", String.valueOf(successCount),
                "ERROR_COUNT", String.valueOf(errorCount),
                "TOTAL_JURORS", String.valueOf(jurordetailList.size())
            ));
    }

    /**
     * Updates juror notification.
     */
    private void update(JurorPool jurorDetails) {
        try {
            jurorRepository.save(jurorDetails);
            log.trace("Updating Juror notification as sent ({})... ", jurorDetails.getJuror().getNotifications());
        } catch (TransactionSystemException e) {
            Throwable cause = e.getRootCause();
            throw new JurorCommsNotificationServiceException(
                "Failed to update db to "
                    + jurorDetails.getJuror().getNotifications() + ". Manual update required. ",
                cause
            );
        }
    }

}
