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
import java.util.HashMap;
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
    private final JurorPoolRepository jurorPoolRepository;

    private static final String LOC_CODE_HARROW = "468";
    private static final String LOC_CODE_TAUNTON = "459";

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

        // remove any unwanted records from the bulk_print_data table based on the business rules
        bulkPrintDataRepository.deletePrintfiles();

        final List<BulkPrintDataNotifyComms> bulkPrintDataNotifyCommsList =
            Lists.newLinkedList(bulkPrintDataNotifyCommsRepository.findAll());

        log.debug("jurorCommsPrintFiles {}", bulkPrintDataNotifyCommsList.size());
        int commsSent = 0;
        int commsfailed = 0;
        int invalidEmailAddress = 0;
        if (!bulkPrintDataNotifyCommsList.isEmpty()) {
            Map<String, String> locCodeTemplateMap = getLocCodeTemplateMap();
            Map<String, String> changeTemplateMap = getChangeTemplateMap();

            for (BulkPrintDataNotifyComms printFile : bulkPrintDataNotifyCommsList) {
                try {
                    log.trace("LetterService :  jurorNumber {}", printFile.getJurorNo());
                    final JurorPool juror =
                        jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(printFile.getJurorNo(), true,
                            SecurityUtil.BUREAU_OWNER);
                    String locCode = printFile.getLocCode();

                    if (Objects.equals(locCode, LOC_CODE_TAUNTON) || Objects.equals(locCode, LOC_CODE_HARROW)) {

                        String templateName = printFile.getTemplateName();

                        if (templateName.equals("CONFRIM_JUROR_ENG")
                            ||
                            templateName.equals("DEF_DENIED_ENG")
                            ||
                            templateName.equals("DEF_GRANTED_ENG")
                            ||
                            templateName.equals("EXC_DENIED_ENG")
                            ||
                            templateName.equals("POSTPONE_JUROR_ENG")) {


                            String currentTemplate = printFile.getTemplateName();
                            String newTemplate = changeTemplateMap.get(currentTemplate);
                            String changedCourtTemplate = locCodeTemplateMap.get(newTemplate);
                            printFile.setTemplateId(changedCourtTemplate);
                        }

                    }


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

    private Map<String, String> getLocCodeTemplateMap() {
        Map<String, String> locCodeTemplateMap = new HashMap<>();
        locCodeTemplateMap.put("CONFIRMATION OF SERVICE TAUNTON", "ea38af04-0631-4c7c-bfc8-0c491b7e98a2");
        locCodeTemplateMap.put("TEMP_DEF_DENIED_ENG", "63d636d3-4ca2-452d-baa2-a940e4dcc48a");
        locCodeTemplateMap.put("TEMP_DEF_GRANTED_ENG", "f5072da7-b250-4f02-b206-f176b1a0b80b");
        locCodeTemplateMap.put("TEMP_EXC_DENIED_ENG", "f5669ddd-4bb3-4092-b60b-45f410de74a7");
        locCodeTemplateMap.put("TEMP_POSTPONE_JUROR_ENG", "6504a964-0081-4b42-95da-9cccd26c1202");
        locCodeTemplateMap.put("CONFIRMATION OF SERVICE HARROW", "bdcb84c2-49c1-435f-9821-262446c98a1c");
        locCodeTemplateMap.put("CONFRIM_JUROR_ENG", "00afe3f3-28cb-4ae0-9776-9b78556ae8e7");
        locCodeTemplateMap.put("DEF_DENIED_ENG", "7e6f2099-6fb7-4179-b968-e9c867e73c64");
        locCodeTemplateMap.put("DEF_GRANTED_ENG", "399c27ff-9651-4a49-9398-99c990db1a34");
        locCodeTemplateMap.put("EXC_DENIED_ENG", "26d3232e-09cd-47a8-afaa-8d0d0d0dd2a2");
        locCodeTemplateMap.put("POSTPONE_JUROR_ENG", "7857b20c-3582-4de2-9f1a-c906096d3c73");
        return locCodeTemplateMap;
    }

    private Map<String, String> getChangeTemplateMap() {
        Map<String, String> changeTemplateMap = new HashMap<>();
        changeTemplateMap.put("CONFRIM_JUROR_ENG", "CONFIRMATION OF SERVICE TAUNTON");
        changeTemplateMap.put("CONFRIM_JUROR_ENG", "CONFIRMATION OF SERVICE HARROW");
        changeTemplateMap.put("DEF_DENIED_ENG", "TEMP_DEF_DENIED_ENG");
        changeTemplateMap.put("DEF_GRANTED_ENG", "TEMP_DEF_GRANTED_ENG");
        changeTemplateMap.put("EXC_DENIED_ENG", "TEMP_EXC_DENIED_ENG");
        changeTemplateMap.put("POSTPONE_JUROR_ENG", "TEMP_POSTPONE_JUROR_ENG");
        return changeTemplateMap;
    }


}
