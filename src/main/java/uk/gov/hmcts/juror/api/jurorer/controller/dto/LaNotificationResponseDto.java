package uk.gov.hmcts.juror.api.jurorer.controller.dto;

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
@Schema(description = "Response from sending LA notifications with details of any failures")
public class LaNotificationResponseDto {

    @JsonProperty("total_la_codes_requested")
    @Schema(description = "Total number of LA codes in the request", example = "5")
    private int totalLaCodesRequested;

    @JsonProperty("successful_notifications_sent")
    @Schema(description = "Total number of successful email notifications sent", example = "8")
    private int successfulNotificationsSent;

    @JsonProperty("failed_notifications")
    @Schema(description = "List of failed notifications with reasons")
    private List<FailedNotification> failedNotifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Details of a failed notification")
    public static class FailedNotification {

        @JsonProperty("la_code")
        @Schema(description = "Local Authority code", example = "001")
        private String laCode;

        @JsonProperty("la_name")
        @Schema(description = "Local Authority name", example = "Birmingham")
        private String laName;

        @JsonProperty("email_address")
        @Schema(description = "Email address that failed (null if no users exist)", example = "user@example.com")
        private String emailAddress;

        @JsonProperty("failure_reason")
        @Schema(description = "Reason for failure", example = "LA_NOT_FOUND")
        private FailureReason failureReason;

        @JsonProperty("failure_message")
        @Schema(description = "Detailed failure message", example = "Local Authority not found")
        private String failureMessage;
    }

    @Schema(description = "Reason codes for notification failures")
    public enum FailureReason {
        @Schema(description = "Local Authority code does not exist in database")
        LA_NOT_FOUND,

        @Schema(description = "Local Authority exists but has no users")
        NO_USERS_FOR_LA,

        @Schema(description = "User exists but email address is blank or null")
        EMAIL_ADDRESS_BLANK,

        @Schema(description = "User exists but is marked as inactive")
        USER_INACTIVE,

        @Schema(description = "Email failed to send via GOV.UK Notify")
        NOTIFY_API_ERROR,

        @Schema(description = "Unexpected error occurred while processing")
        UNEXPECTED_ERROR
    }
}
