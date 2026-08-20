package uk.gov.hmcts.juror.api.bureau.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.CommunicationChannel;
import uk.gov.hmcts.juror.api.moj.enumeration.EmailStatus;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.utils.NotifyUtil;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link BureauProcessService}.
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.CognitiveComplexity")
public class JurorCommsEmailServiceImpl implements BureauProcessService {

    private final JurorCommsNotificationService jurorCommsNotificationService;
    private final BulkPrintDataRepository bulkPrintDataRepository;
    private final JurorPoolRepository jurorPoolRepository;

    /**
     * Implements a specific job execution.
     * Processes entries in the juror_mod.bulk_print_data table and sends the appropriate email notifications to
     * the juror.
     */
    @SuppressWarnings("checkstyle:LineLength") // false positive
    @Override
    @Transactional
    public SchedulerServiceClient.Result process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Email Comms Processing : Started - {}", dateFormat.format(new Date()));

        // remove any unwanted records from the bulk_print_data table based on the business rules
        bulkPrintDataRepository.deletePrintfiles();

        // find the emails that need to be sent out from bulk_print_data table
        List<BulkPrintData> bulkPrintDataList = bulkPrintDataRepository.findByCommunicationChannelAndEmailStatus(
            CommunicationChannel.EMAIL, EmailStatus.PENDING);

        log.debug("Juror Emails Comms size {}", bulkPrintDataList.size());
        int commsSent = 0;
        int commsfailed = 0;
        int invalidEmailAddress = 0;
        if (bulkPrintDataList.isEmpty()) {
            log.trace("Email Comms Processing : No pending records found.");
        } else {

            for (BulkPrintData pendingEmail : bulkPrintDataList) {
                try {
                    log.trace("EmailService :  jurorNumber {}", pendingEmail.getJurorNo());
                    final JurorPool juror =
                        jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(pendingEmail.getJurorNo(), true,
                            SecurityUtil.BUREAU_OWNER);

                    jurorCommsNotificationService.sendJurorEmailComms(
                        juror,
                        pendingEmail.getNotifyTemplateName()
                    );

                    updateEmailStatus(pendingEmail);
                    commsSent++;
                } catch (JurorCommsNotificationServiceException e) {
                    if (NotifyUtil.isInvalidEmailAddressError(e.getCause())) {
                        invalidEmailAddress++;
                    } else {
                        log.error(
                            "Unable to send Email comms for {}",
                            pendingEmail.getJurorNo(), e
                        );
                        commsfailed++;
                    }
                } catch (Exception e) {
                    commsfailed++;
                    log.error("Email Comms Processing : Juror Comms failed : {}", e.getMessage());
                }
            }
            log.info("LetterService : Summary, identified:{}, sent:{}, failed:{},",
                     bulkPrintDataList.size(), commsSent, commsfailed
            );
        }

        SchedulerServiceClient.Result.Status status = commsfailed == 0
            ? SchedulerServiceClient.Result.Status.SUCCESS
            : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS;

        // log the results for Dynatrace
        log.info(
            "[JobKey: CRONBATCH_EMAIL_COMMS]\n[{}]\nresult={},\nmetadata={messages_sent={},messages_failed={},"
                + "invalid_email_count={}}",
            DATE_TIME_FORMATTER.format(LocalDateTime.now()),
            status,
            commsSent,
            commsfailed,
            invalidEmailAddress
        );

        log.info("Email Comms Processing : Finished - {}", dateFormat.format(new Date()));

        return new SchedulerServiceClient.Result(
            status, null,
            Map.of(
                "COMMS_FAILED", String.valueOf(commsfailed),
                "COMMNS_SENT", String.valueOf(commsSent),
                "INVALID_EMAIL_ADDRESS", String.valueOf(invalidEmailAddress)
            ));
    }

    /**
     * Updates the digital_comms flag after comms has been sent to Notify.
     */
    private void updateEmailStatus(BulkPrintData bulkPrintData) {

        log.trace("Inside updateEmailStatus .....");
        final List<BulkPrintData> bulkPrintDataDetail = bulkPrintDataRepository.findByJurorNoAndIdAndCreationDate(
            bulkPrintData.getJurorNo(),
            bulkPrintData.getId(),
            bulkPrintData.getCreationDate()
        );
        if (bulkPrintDataDetail.size() != 1) {
            throw new JurorCommsNotificationServiceException(
                "Unable to update bulk print data after Juror Comms sent.");
        }

        bulkPrintDataDetail.get(0).setEmailStatus(EmailStatus.SENT);
        bulkPrintDataRepository.saveAll(bulkPrintDataDetail);
        log.trace("Saving updated bulk_print_data - updated status to SENT .....");

    }

}
