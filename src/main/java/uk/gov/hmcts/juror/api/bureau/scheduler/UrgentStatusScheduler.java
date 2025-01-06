package uk.gov.hmcts.juror.api.bureau.scheduler;


import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.bureau.service.ScheduledService;
import uk.gov.hmcts.juror.api.bureau.service.UrgencyService;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UrgentStatusScheduler implements ScheduledService {

    private final UrgencyService urgencyService;

    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepositoryMod;
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepositoryMod;
    private final JurorPoolRepository jurorRepository;


    @SuppressWarnings("checkstyle:LineLength") // false positive
    @Override
    public SchedulerServiceClient.Result process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Scheduler Starting time, is now {}", dateFormat.format(new Date()));

        final List<ProcessingStatus> pendingStatuses = List.of(ProcessingStatus.CLOSED);

        Iterable<DigitalResponse> digitalResponseList = jurorDigitalResponseRepositoryMod.findAll(
            JurorResponseQueries.byStatusNotClosed(pendingStatuses)
                .and(JurorResponseQueries.jurorIsNotTransferred())
                .and(JurorResponseQueries.isDigital()));

        Iterable<PaperResponse> paperResponseList = jurorPaperResponseRepositoryMod.findAll(
            JurorResponseQueries.byStatusNotClosedPaper(pendingStatuses)
                .and(JurorResponseQueries.jurorIsNotTransferredPaper())
                .and(JurorResponseQueries.isPaper()));

        final List<AbstractJurorResponse> jurorResponsesNotClosed = Lists.newLinkedList(digitalResponseList);
        jurorResponsesNotClosed.addAll(Lists.newLinkedList(paperResponseList));

        log.trace("jurorResponsesNotClosed {}", jurorResponsesNotClosed.size());


        int failedToFindJurorCount = 0;
        int totalResponsesProcessed = 0;
        int totalUrgentResponses = 0;
        if (!jurorResponsesNotClosed.isEmpty()) {

            for (AbstractJurorResponse backlogItem : jurorResponsesNotClosed) {
                JurorPool jurorDetails = jurorRepository.findByJurorJurorNumberAndIsActiveAndOwner(
                    backlogItem.getJurorNumber(),
                    true,
                    SecurityUtil.BUREAU_OWNER);

                if (jurorDetails == null) {
                    log.error("Can not find active bureau owned juror pool for juror: {}",
                        backlogItem.getJurorNumber());
                    failedToFindJurorCount++;
                    continue;
                }
                totalResponsesProcessed++;

                log.trace("processing  pool number {} ", jurorDetails.getJurorNumber());

                if ((!backlogItem.isUrgent() && urgencyService.isUrgent(backlogItem, jurorDetails))) {

                    totalUrgentResponses++;
                    urgencyService.setUrgencyFlags(backlogItem, jurorDetails);
                    log.trace("saving response back");
                    if (backlogItem instanceof DigitalResponse digitalResponse) {
                        jurorDigitalResponseRepositoryMod.save(digitalResponse);
                    } else if (backlogItem instanceof PaperResponse paperResponse) {
                        jurorPaperResponseRepositoryMod.save(paperResponse);
                    }
                    log.trace("responses saved juror {}", backlogItem.getJurorNumber());
                }
            }
            log.debug("Scheduler : Processing complete.");
        } else {
            log.trace("Scheduler: No pending Juror responses found.");
        }

        SchedulerServiceClient.Result.Status status = failedToFindJurorCount == 0
            ? SchedulerServiceClient.Result.Status.SUCCESS
            : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS;

        log.info(
            "[JobKey: CRONBATCH_URGENT_SUPER_URGENT_STATUS]\n[{}]\nresult={},\nmetadata={total_processed={},total_marked_urgent={},total_failed_to_find={}}",
            LocalDateTime.now(),
            status,
            totalResponsesProcessed,
            totalUrgentResponses,
            failedToFindJurorCount
        );

        log.info("Scheduler: Finished, time is now {}", dateFormat.format(new Date()));
        return new SchedulerServiceClient.Result(
            status, null,
            Map.of(
                "TOTAL_PROCESSED", String.valueOf(totalResponsesProcessed),
                "TOTAL_MARKED_URGENT", String.valueOf(totalUrgentResponses),
                "TOTAL_FAILED_TO_FIND", String.valueOf(failedToFindJurorCount)
            ));

    }
}
