package uk.gov.hmcts.juror.api.moj.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.config.RemoteConfig;
import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.JwtService;

@Slf4j
@Component
public class SchedulerServiceClientImpl extends AbstractRemoteRestClient implements SchedulerServiceClient {

    private final String updateStatusUrl;

    public SchedulerServiceClientImpl(
        final JwtService jwtService,
        final RemoteConfig config) {
        super(config.schedulerServiceRestTemplateBuilder(jwtService));
        this.updateStatusUrl = config.getSchedulerService().getUrl();
        log.info("******** SchedulerService updateStatusUrl: " + this.updateStatusUrl);
    }

    @Override
    public void updateStatus(String jobKey, Long taskId, Result payload) {
        try {
            log.debug("JobKey: " + jobKey + ".\n"
                + "TaskId: " + taskId + ".\n"
                + "Status: " + payload.getStatus() + ".\n"
                + "Message: " + payload.getMessage());
            if (jobKey == null || taskId == null) {
                return;//No need to continue if jobKey/taskId are not provided as these are required for reporting back
            }

            HttpEntity<Result> requestUpdate = new HttpEntity<>(payload);
            ResponseEntity<Void> response =
                restTemplate.exchange(updateStatusUrl, HttpMethod.PUT, requestUpdate, Void.class, jobKey, taskId);

            final HttpStatusCode statusCode = response.getStatusCode();
            if (!statusCode.equals(HttpStatus.ACCEPTED)) {
                throw new MojException.RemoteGatewayException(
                    "Call to SchedulerServiceClient.updateStatus(jobKey, taskId, result, duration) failed status code"
                        + " was: "
                        + statusCode, null);
            }
        } catch (Throwable throwable) {
            String message = "Failed to upload Job result";
            log.error(message, throwable);
            throw new MojException.InternalServerError(message, throwable);
        }
    }
}
