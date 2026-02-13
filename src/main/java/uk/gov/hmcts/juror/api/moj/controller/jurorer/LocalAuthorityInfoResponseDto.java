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


    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Setter
    @Getter
    public static class ReminderHistoryInfo {

        private String sentBy;

        private LocalDateTime timeSent;
    }

}
