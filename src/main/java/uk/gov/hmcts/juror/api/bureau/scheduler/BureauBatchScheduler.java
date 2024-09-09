package uk.gov.hmcts.juror.api.bureau.scheduler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.service.ScheduledService;
import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


@Component
@Slf4j
public class BureauBatchScheduler {

    private final BureauBatchProcessFactory bureauBatchProcessFactory;
    private final SchedulerServiceClient schedulerServiceClient;

    @Autowired
    public BureauBatchScheduler(
        final BureauBatchProcessFactory bureauBatchProcessFactory,
        final SchedulerServiceClient schedulerServiceClient) {
        Assert.notNull(bureauBatchProcessFactory, "BureauBatchProcessFactory cannot be null.");
        this.bureauBatchProcessFactory = bureauBatchProcessFactory;
        this.schedulerServiceClient = schedulerServiceClient;

    }

    /**
     * General Entry point for externally hosted cron jobs. ( server crontab and not via springboot).
     */
    @Async("threadPoolTaskExecutor")
    public void processBatchJobServices(final String[] types, String jobKey, Long taskId) {
        try {
            SchedulerServiceClient.Result result = null;

            SimpleDateFormat dateFormat = new SimpleDateFormat();
            log.info("BureauBatchScheduler: Starting - {}", dateFormat.format(new Date()));
            for (String batchJob : types) {

                final ScheduledService scheduledService = bureauBatchProcessFactory.getBatchProcessService(batchJob);

                if (scheduledService != null) {
                    result = scheduledService.process();
                } else {
                    result = new SchedulerServiceClient.Result(SchedulerServiceClient.Result.Status.FAILED,
                        "Unknown job type: '" + batchJob + "'", null);
                    log.info("BureauBatchScheduler: {} job does not exist.", batchJob);
                }
            }
            if (result == null) {
                result = new SchedulerServiceClient.Result(SchedulerServiceClient.Result.Status.SUCCESS, null, null);
            }
            this.schedulerServiceClient.updateStatus(jobKey, taskId, result);
            log.info("BureauBatchScheduler: Finished - {}", dateFormat.format(new Date()));
        } catch (Exception e) {
            log.error(
                "Call BureauBatchScheduler Processing : CronBureauBatchScheduler failed : {}",
                e.getMessage());
            this.schedulerServiceClient.updateStatus(jobKey, taskId, new SchedulerServiceClient.Result(
                SchedulerServiceClient.Result.Status.FAILED_UNEXPECTED_EXCEPTION,
                "An unexpected exception happened when running: " + Arrays.toString(types), null));
            throw e;
        }
    }

}
