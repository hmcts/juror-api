package uk.gov.hmcts.juror.api.bureau.scheduler;


import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.bureau.service.ScheduledService;
import uk.gov.hmcts.juror.api.bureau.service.UrgencyService;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UrgentSuperUrgentStatusScheduler implements ScheduledService {

    private final UrgencyService urgencyService;

    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepositoryMod;


    private final JurorPoolRepository jurorRepository;



    @Override
    public void process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Scheduler Starting time, is now {}", dateFormat.format(new Date()));

        final List<ProcessingStatus> pendingStatuses = List.of(ProcessingStatus.CLOSED);

        BooleanExpression pendingFilter = JurorResponseQueries.byStatusNotClosed(pendingStatuses);

        Iterable<DigitalResponse> digitalResponseList = jurorDigitalResponseRepositoryMod.findAll(pendingFilter);

        final List<DigitalResponse> jurorResponsesNotClosed = Lists.newLinkedList(digitalResponseList);

        log.trace("jurorResponsesNotClosed {}", jurorResponsesNotClosed.size());


        if (!jurorResponsesNotClosed.isEmpty()) {

            for (DigitalResponse backlogItem : jurorResponsesNotClosed) {
                JurorPool jurorDetails = jurorRepository.findByJurorJurorNumber(backlogItem.getJurorNumber());
                log.trace("processing  pool number {} ", jurorDetails.getJurorNumber());

                if (urgencyService.isUrgent(backlogItem, jurorDetails)
                    || urgencyService.isSuperUrgent(backlogItem, jurorDetails)) {
                    urgencyService.setUrgencyFlags(backlogItem, jurorDetails);
                    log.trace("saving response back");
                    jurorDigitalResponseRepositoryMod.save(backlogItem);
                    log.trace("responses saved juror {}", backlogItem.getJurorNumber());
                }

            }
            log.debug("Scheduler : Processing complete.");
        } else {
            log.trace("Scheduler: No pending Juror responses found.");
        }
        log.info("Scheduler: Finished, time is now {}", dateFormat.format(new Date()));
    }
}
