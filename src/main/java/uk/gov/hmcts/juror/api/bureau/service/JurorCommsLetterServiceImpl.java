package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintDataNotifyComms;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintDataNotifyCommsRepository;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
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
public class JurorCommsLetterServiceImpl implements BureauProcessService {


    private final JurorCommsNotificationService jurorCommsNotificationService;
    private final BulkPrintDataNotifyCommsRepository bulkPrintDataNotifyCommsRepository;
    private final BulkPrintDataRepository bulkPrintDataRepository;
    private final JurorPoolRepository jurorRepository;

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror.print_files table and sends the appropriate email notifications to
     * the juror.
     */
    @SuppressWarnings("checkstyle:LineLength") // false positive
    @Override
    @Transactional
    public SchedulerServiceClient.Result process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Letter Comms Processing : Started - {}", dateFormat.format(new Date()));

        final List<BulkPrintDataNotifyComms> bulkPrintDataNotifyCommsList =
            Lists.newLinkedList(bulkPrintDataNotifyCommsRepository.findAll());

        log.debug("jurorCommsPrintFiles {}", bulkPrintDataNotifyCommsList.size());
        int commsSent = 0;
        int commsfailed = 0;
        int invalidEmailAddress = 0;
        if (!bulkPrintDataNotifyCommsList.isEmpty()) {
            for (BulkPrintDataNotifyComms printFile : bulkPrintDataNotifyCommsList) {
                try {
                    log.trace("LetterService :  jurorNumber {}", printFile.getJurorNo());
                    final JurorPool juror =
                        jurorRepository.findByJurorJurorNumberAndIsActiveAndOwner(printFile.getJurorNo(), true,
                            SecurityUtil.BUREAU_OWNER);


                    jurorCommsNotificationService.sendJurorComms(
                        juror,
                        JurorCommsNotifyTemplateType.LETTER_COMMS,
                        printFile.getTemplateId(),
                        printFile.getDetailRec(),
                        false
                    );

                    updatePrintFiles(printFile);
                    commsSent++;
                } catch (JurorCommsNotificationServiceException e) {
                    if (NotifyUtil.isInvalidEmailAddressError(e.getCause())) {
                        invalidEmailAddress++;
                    } else {
                        log.error(
                            "Unable to send Letter comms for {}",
                            printFile.getJurorNo(), e
                        );
                        commsfailed++;
                    }
                } catch (Exception e) {
                    commsfailed++;
                    log.error("Letter Comms Processing : Juror Comms failed : {}", e.getMessage());
                }
            }
            log.info("LetterService : Summary, identified:{}, sent:{}, failed:{},",
                bulkPrintDataNotifyCommsList.size(), commsSent, commsfailed
            );
        } else {
            log.trace("Letter Comms Processing : No pending records found.");
        }

        SchedulerServiceClient.Result.Status status = commsfailed == 0
            ? SchedulerServiceClient.Result.Status.SUCCESS
            : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS;

        // log the results for Dynatrace
        log.info(
            "[JobKey: CRONBATCH_LETTER_COMMS]\n[{}]\nresult={},\nmessages_sent={},\nmessages_failed={},\ninvalid_email_count={}",
            LocalDateTime.now(),
            status,
            commsSent,
            commsfailed,
            invalidEmailAddress
        );

        log.info("Letter Comms Processing : Finished - {}", dateFormat.format(new Date()));

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
    private void updatePrintFiles(BulkPrintDataNotifyComms bulkPrintDataNotifyComms) {

        log.trace("Inside updatePrintFiles .....");
        final List<BulkPrintData> bulkPrintDataDetail = bulkPrintDataRepository.findByJurorNoAndIdAndCreationDate(
            bulkPrintDataNotifyComms.getJurorNo(),
            bulkPrintDataNotifyComms.getId(),
            bulkPrintDataNotifyComms.getCreationDate()
        );
        if (bulkPrintDataDetail.size() != 1) {
            throw new JurorCommsNotificationServiceException(
                "updatePrintFiles: Unable to update printFiles after Juror Comms sent.");
        }

        bulkPrintDataDetail.get(0).setDigitalComms(true);
        bulkPrintDataRepository.saveAll(bulkPrintDataDetail);
        log.trace("Saving updated printFile.digital_comms - updatePrintFiles .....");
    }

}
