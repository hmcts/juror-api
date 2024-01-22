package uk.gov.hmcts.juror.api.bureau.scheduler;


import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetail;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetailQueries;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetailRepository;
import uk.gov.hmcts.juror.api.bureau.service.JurorResponsesSummonedService;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


@Component
@Slf4j
public class CloseJurorResponsesScheduler implements JurorResponsesSummonedService {

    private final JurorResponseRepository jurorResponseRepository;

    private final BureauJurorDetailRepository bureauJurorDetailRepository;

    @Autowired
    public CloseJurorResponsesScheduler(
        final JurorResponseRepository jurorResponseRepository,
        final BureauJurorDetailRepository bureauJurorDetailRepository
    ) {

        Assert.notNull(bureauJurorDetailRepository, "BureauJurorDetailRepository cannot be null");
        org.springframework.util.Assert.notNull(jurorResponseRepository, "JurorResponseRepository cannot be null");
        this.jurorResponseRepository = jurorResponseRepository;
        this.bureauJurorDetailRepository = bureauJurorDetailRepository;

    }

    @Override
    public void process() {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Close Response Scheduler Starting time, is now {}", dateFormat.format(new Date()));


        BooleanExpression JurorResponseCloseFilter = BureauJurorDetailQueries.JurorResponsesForClosing();
        final List<BureauJurorDetail> JurorResponsesDetail = Lists.newLinkedList(bureauJurorDetailRepository.findAll(
            JurorResponseCloseFilter));


        int numberRecordsClosed = 0;

        for (BureauJurorDetail responseRecord : JurorResponsesDetail) {
            log.info("jurorResponsesSummonedToClosed JUROR_NO: {}", responseRecord.getJurorNumber());

            String jurorNumber = responseRecord.getJurorNumber();

            log.info("Juror Numbers {}", jurorNumber);

            JurorResponse jurorResponseRecord = jurorResponseRepository.findByJurorNumber(jurorNumber);
            log.info(" Juror numbers to be processed {} ", jurorResponseRecord.getJurorNumber());


            jurorResponseRecord.setProcessingStatus(ProcessingStatus.CLOSED);
            jurorResponseRecord.setProcessingComplete(Boolean.TRUE);
            jurorResponseRecord.setCompletedAt(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
            updateJurorResponse(jurorResponseRecord);
            numberRecordsClosed++;

        }
        log.info("Number of Juror response records found  : {}", JurorResponsesDetail.size());
        log.info("The Number of Juror response records closed {}", numberRecordsClosed);
        log.info("Close Response Scheduler: Finished, time is now {}", dateFormat.format(new Date()));
    }

    private void updateJurorResponse(JurorResponse jurorResponseRecord) {
        try {
            log.trace("Inside update ....");
            jurorResponseRepository.save(jurorResponseRecord);
            log.trace("Updating processing status,processing complete ");
        } catch (TransactionSystemException e) {
            log.error("Failed to update db to {}. Manual update required. {}", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
