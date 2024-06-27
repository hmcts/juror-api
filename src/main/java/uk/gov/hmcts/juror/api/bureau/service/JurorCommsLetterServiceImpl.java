package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import io.jsonwebtoken.lang.Assert;
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
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link BureauProcessService}.
 */
@Slf4j
@Service
public class JurorCommsLetterServiceImpl implements BureauProcessService {


    private final JurorCommsNotificationService jurorCommsNotificationService;
    private final BulkPrintDataNotifyCommsRepository bulkPrintDataNotifyCommsRepository;
    private final BulkPrintDataRepository bulkPrintDataRepository;
    private final JurorPoolRepository jurorRepository;

    @Autowired
    public JurorCommsLetterServiceImpl(
        final JurorCommsNotificationService jurorCommsNotificationService,
        final BulkPrintDataNotifyCommsRepository bulkPrintDataNotifyCommsRepository,
        final BulkPrintDataRepository bulkPrintDataRepository,
        final JurorPoolRepository jurorRepository) {
        Assert.notNull(jurorCommsNotificationService, "JurorCommsNotificationService cannot be null.");
        Assert.notNull(bulkPrintDataRepository, "BulkPrintDataRepository cannot be null.");
        Assert.notNull(bulkPrintDataNotifyCommsRepository, "BulkPrintDataNotifyCommsRepository cannot be null.");
        Assert.notNull(jurorRepository, "JurorRepository cannot be null.");

        this.jurorCommsNotificationService = jurorCommsNotificationService;
        this.bulkPrintDataRepository = bulkPrintDataRepository;
        this.bulkPrintDataNotifyCommsRepository = bulkPrintDataNotifyCommsRepository;
        this.jurorRepository = jurorRepository;

    }

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror.print_files table and sends the appropriate email notifications to
     * the juror.
     */
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
                    log.error(
                        "Unable to send Letter comms for {} : {} {}",
                        printFile.getJurorNo(),
                        e.getMessage(),
                        e.getCause().toString()
                    );
                    commsfailed++;
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
        log.info("Letter Comms Processing : Finished - {}", dateFormat.format(new Date()));

        return new SchedulerServiceClient.Result(
            commsfailed == 0
                ? SchedulerServiceClient.Result.Status.SUCCESS
                : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS, null,
            Map.of("COMMS_FAILED", "" + commsfailed,
                "COMMNS_SENT", "" + commsSent));
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
