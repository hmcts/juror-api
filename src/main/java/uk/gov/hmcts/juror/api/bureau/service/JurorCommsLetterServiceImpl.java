package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.domain.JurorCommsPrintFiles;
import uk.gov.hmcts.juror.api.bureau.domain.JurorCommsPrintFilesRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PrintFile;
import uk.gov.hmcts.juror.api.bureau.domain.PrintFileRepository;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link BureauProcessService}.
 */
@Slf4j
@Service
public class JurorCommsLetterServiceImpl implements BureauProcessService {


    private final JurorCommsNotificationService jurorCommsNotificationService;
    private final JurorCommsPrintFilesRepository jurorCommsPrintFilesRepository;
    private final PrintFileRepository printFileRepository;
    private final PoolRepository poolRepository;

    @Autowired
    public JurorCommsLetterServiceImpl(
        final JurorCommsNotificationService jurorCommsNotificationService,
        final JurorCommsPrintFilesRepository jurorCommsPrintFilesRepository,
        final PrintFileRepository printFileRepository,
        final PoolRepository poolRepository) {
        Assert.notNull(jurorCommsNotificationService, "JurorCommsNotificationService cannot be null.");
        Assert.notNull(jurorCommsPrintFilesRepository, "JurorCommsPrintFilesRepository cannot be null.");
        Assert.notNull(printFileRepository, "PrintFileRepository cannot be null.");
        Assert.notNull(poolRepository, "PoolRepository cannot be null.");
        this.jurorCommsNotificationService = jurorCommsNotificationService;
        this.jurorCommsPrintFilesRepository = jurorCommsPrintFilesRepository;
        this.printFileRepository = printFileRepository;
        this.poolRepository = poolRepository;
    }

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror.print_files table and sends the appropriate email notifications to
     * the juror.
     */
    @Override
    @Transactional
    public void process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Letter Comms Processing : Started - {}", dateFormat.format(new Date()));

        final List<JurorCommsPrintFiles> jurorCommsPrintFilesList =
            Lists.newLinkedList(jurorCommsPrintFilesRepository.findAll());

        log.debug("jurorCommsPrintFiles {}", jurorCommsPrintFilesList.size());

        if (!jurorCommsPrintFilesList.isEmpty()) {

            int commsSent = 0;
            int commsfailed = 0;
            for (JurorCommsPrintFiles printFile : jurorCommsPrintFilesList) {

                log.trace("LetterService :  jurorNumber {}", printFile.getJurorNumber());
                final Pool pool = poolRepository.findByJurorNumber(printFile.getJurorNumber());

                try {

                    jurorCommsNotificationService.sendJurorComms(
                        pool,
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
                        printFile.getJurorNumber(),
                        e.getMessage(),
                        e.getCause().toString()
                    );
                    commsfailed++;
                } catch (Exception e) {
                    log.error("Letter Comms Processing : Juror Comms failed : {}", e.getMessage());
                    throw new JurorCommsNotificationServiceException(
                        "Letter Comms Processing failed. " + e.getMessage(),
                        e.getCause()
                    );

                }

            }
            log.info("LetterService : Summary, identified:{}, sent:{}, failed:{},",
                jurorCommsPrintFilesList.size(), commsSent, commsfailed
            );
        } else {
            log.trace("Letter Comms Processing : No pending records found.");
        }
        log.info("Letter Comms Processing : Finished - {}", dateFormat.format(new Date()));
    }

    /***
     * Updates the digital_comms flag after comms has been sent to Notify.
     * @param jurorCommsPrintFiles
     */
    private void updatePrintFiles(JurorCommsPrintFiles jurorCommsPrintFiles) {

        log.trace("Inside updatePrintFiles .....");
        final List<PrintFile> printFileDetail = printFileRepository.findByPartNoAndPrintFileNameAndCreationDate(
            jurorCommsPrintFiles.getJurorNumber(),
            jurorCommsPrintFiles.getPrintFileName(),
            jurorCommsPrintFiles.getCreationDate()
        );
        if (printFileDetail.isEmpty() || printFileDetail.size() > 1) {
            throw new JurorCommsNotificationServiceException(
                "updatePrintFiles: Unable to update printFiles after Juror Comms sent.");
        }

        printFileDetail.get(0).setDigitalComms(true);
        printFileRepository.saveAll(printFileDetail);
        log.trace("Saving updated printFile.digital_comms - updatePrintFiles .....");
    }

}
