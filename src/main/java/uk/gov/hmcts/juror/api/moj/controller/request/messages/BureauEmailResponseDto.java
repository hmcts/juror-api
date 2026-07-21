package uk.gov.hmcts.juror.api.moj.controller.response.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Response from sending bureau emails to jurors, with details of any failures")
public class BureauEmailResponseDto {

    @JsonProperty("total_jurors_requested")
    @Schema(example = "5")
    private int totalJurorsRequested;

    @JsonProperty("successful_emails_sent")
    @Schema(example = "4")
    private int successfulEmailsSent;

    @JsonProperty("failed_notifications")
    private List<FailedNotification> failedNotifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class FailedNotification {

        @JsonProperty("juror_number")
        private String jurorNumber;

        @JsonProperty("email_address")
        private String emailAddress;

        @JsonProperty("failure_reason")
        private FailureReason failureReason;

        @JsonProperty("failure_message")
        private String failureMessage;
    }

    public enum FailureReason {
        JUROR_NOT_FOUND,
        TEMPLATE_NOT_CONFIGURED,
        NOTIFY_API_ERROR,
        UNEXPECTED_ERROR
    }
}
