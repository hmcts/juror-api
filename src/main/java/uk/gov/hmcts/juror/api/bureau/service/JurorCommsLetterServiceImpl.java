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
import java.util.Objects;

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


                   Map<String, Map<String,String>> locCodeTemplateMap = Map.of(
                       "459", Map.of(
                           "CONFIRMATION OF SERVICE TAUNTON", "ea38af04-0631-4c7c-bfc8-0c491b7e98a2",
                           "TEMP_DEF_DENIED_ENG", "63d636d3-4ca2-452d-baa2-a940e4dcc48a",
                           "TEMP_DEF_GRANTED_ENG", "f5072da7-b250-4f02-b206-f176b1a0b80b",
                           "TEMP_EXC_DENIED_ENG","f5669ddd-4bb3-4092-b60b-45f410de74a7",
                           "TEMP_POSTPONE_JUROR_ENG","6504a964-0081-4b42-95da-9cccd26c1202"
                       ),
                       "468", Map.of(
                           "CONFIRMATION OF SERVICE HARROW", "bdcb84c2-49c1-435f-9821-262446c98a1c",
                           "TEMP_DEF_DENIED_ENG", "63d636d3-4ca2-452d-baa2-a940e4dcc48a",
                           "TEMP_DEF_GRANTED_ENG", "f5072da7-b250-4f02-b206-f176b1a0b80b",
                           "TEMP_EXC_DENIED_ENG","f5669ddd-4bb3-4092-b60b-45f410de74a7",
                           "TEMP_POSTPONE_JUROR_ENG","6504a964-0081-4b42-95da-9cccd26c1202"
                       )
                   );
                    updateTemplateIdForChangedCourt(printFile, juror, locCodeTemplateMap);



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
            "[JobKey: CRONBATCH_LETTER_COMMS]\n[{}]\nresult={},\nmetadata={messages_sent={},messages_failed={},invalid_email_count={}}",
            DATE_TIME_FORMATTER.format(LocalDateTime.now()),
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


    private void updateTemplateIdForChangedCourt(BulkPrintDataNotifyComms printFile, JurorPool juror, Map<String, Map<String, String>> locCodeTemplateMap) {
        String locCode = printFile.getLocCode();
        String templateName = printFile.getTemplateName();
        if (locCodeTemplateMap.containsKey(locCode) && locCodeTemplateMap.get(locCode).containsKey(templateName)) {
            String changedCourtTemplate = locCodeTemplateMap.get(locCode).get(templateName);
            printFile.setTemplateId(changedCourtTemplate);
            jurorCommsNotificationService.sendJurorComms(
                juror,
                JurorCommsNotifyTemplateType.LETTER_COMMS,
                printFile.getTemplateId(),
                printFile.getDetailRec(),
                false
            );
        }
    }


}
