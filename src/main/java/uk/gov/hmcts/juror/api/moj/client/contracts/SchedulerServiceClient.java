package uk.gov.hmcts.juror.api.moj.client.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import java.util.Map;

public interface SchedulerServiceClient extends Client {

    void updateStatus(String jobKey, Long taskId, Result result);

    @Getter
    @AllArgsConstructor
    class Result {
        private Status status;
        private String message;
        @JsonProperty("meta_data")
        private Map<
            @Length(min = 1, max = 2500) String,
            @Length(min = 1, max = 2500) String> metaData;

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
