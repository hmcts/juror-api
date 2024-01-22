package uk.gov.hmcts.juror.api.bureau.scheduler;


import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.bureau.service.ScheduledService;
import uk.gov.hmcts.juror.api.bureau.service.UrgencyService;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UrgentSuperUrgentStatusScheduler implements ScheduledService {

    private final UrgencyService urgencyService;
    private final JurorResponseRepository jurorResponseRepository;
    private final PoolRepository poolRepository;


    @Override
    public void process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Scheduler Starting time, is now {}", dateFormat.format(new Date()));

        final List<ProcessingStatus> pendingStatuses = List.of(ProcessingStatus.CLOSED);

        BooleanExpression pendingFilter = JurorResponseQueries.byStatusNotClosed(pendingStatuses);

        Iterable<JurorResponse> jurorResponseList = jurorResponseRepository.findAll(pendingFilter);

        final List<JurorResponse> jurorResponsesNotClosed = Lists.newLinkedList(jurorResponseList);

        log.trace("jurorResponsesNotClosed {}", jurorResponsesNotClosed.size());


        if (!jurorResponsesNotClosed.isEmpty()) {

            for (JurorResponse backlogItem : jurorResponsesNotClosed) {
                Pool poolDetails = poolRepository.findByJurorNumber(backlogItem.getJurorNumber());
                log.trace("processing  pool number {} ", poolDetails.getJurorNumber());

                if (urgencyService.isUrgent(backlogItem, poolDetails)
                    || urgencyService.isSuperUrgent(backlogItem, poolDetails)) {
                    urgencyService.setUrgencyFlags(backlogItem, poolDetails);
                    log.trace("saving response back");
                    jurorResponseRepository.save(backlogItem);
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
