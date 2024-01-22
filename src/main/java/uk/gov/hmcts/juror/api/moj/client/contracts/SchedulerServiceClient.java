package uk.gov.hmcts.juror.api.moj.client.contracts;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface SchedulerServiceClient extends Client {

    void updateStatus(String jobKey, Long taskId, Result result);

    @Getter
    @AllArgsConstructor
    class Result {
        private Status status;
        private String message;

        public enum Status {
            VALIDATION_PASSED,
            VALIDATION_FAILED,
            SUCCESS,
            PARTIAL_SUCCESS,
            PENDING,
            INDETERMINATE,
            FAILED,
            FAILED_UNEXPECTED_EXCEPTION
        }
    }
}
