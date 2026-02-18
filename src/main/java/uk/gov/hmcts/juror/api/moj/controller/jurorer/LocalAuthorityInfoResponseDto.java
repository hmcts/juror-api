package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.jurorer.domain.EmailRequestStatus;
import uk.gov.hmcts.juror.api.jurorer.domain.UploadStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "Local authority information response DTO")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LocalAuthorityInfoResponseDto {

    @Schema(description = "Local authority code", example = "002")
    private String localAuthorityCode;

    @Schema(description = "Local authority name", example = "Birmingham")
    private String localAuthorityName;

    @Schema(description = "Whether the local authority is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Upload status of the local authority", example = "NOT_UPLOADED")
    private UploadStatus uploadStatus;

    @Schema(description = "Last upload date of the local authority", example = "2024-06-30")
    private LocalDate lastUploadDate;

    @Schema(description = "Last logged in date of the local authority", example = "2024-06-30")
    private LocalDate lastLoggedInDate;

    @Schema(description = "Email request status of the local authority", example = "SENT")
    private EmailRequestStatus emailRequestStatus;

    @Schema(description = "Date the email request was sent to local authority", example = "2024-06-30")
    private LocalDate dateEmailRequestSent;

    @Schema(description = "Email addresses of the local authority contacts")
    private List<String> emailAddresses;

    @Schema(description = "Notes related to the local authority")
    private String notes;

    @Schema(description = "History of reminder emails sent to the local authority")
    private List<ReminderHistoryInfo> reminderHistory;

    @Schema(description = "Inactive information - only populated if local authority is inactive")
    private InactiveInfo inactiveInfo;


    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Setter
    @Getter
    public static class ReminderHistoryInfo {

        private String sentBy;

        private String sentTo;

        private LocalDateTime timeSent;
    }
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Setter
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Information about why and when a local authority was made inactive")
    public static class InactiveInfo {

        @Schema(description = "Reason the local authority was made inactive")
        private String inactiveReason;      // maps to LocalAuthority.inactiveReason

        @Schema(description = "Username of who made the local authority inactive")
        private String madeInactiveBy;      // maps to LocalAuthority.updatedBy

        @Schema(description = "Date and time when the local authority was made inactive")
        private LocalDateTime madeInactiveAt; // maps to LocalAuthority.lastUpdated
    }

}
