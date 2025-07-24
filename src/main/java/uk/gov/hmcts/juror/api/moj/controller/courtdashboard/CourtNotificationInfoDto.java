package uk.gov.hmcts.juror.api.moj.controller.courtdashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for returning Court Notification information.
 */
@Getter
@Setter
@Builder
@Schema(description = "Court notification information DTO")
public class CourtNotificationInfoDto {

    @JsonProperty("open_summons_replies")
    @Schema(description = "Number of open summons replies for jurors owned by the court")
    private int openSummonsReplies;

    @JsonProperty("pending_jurors")
    @Schema(description = "Number of pending jurors awaiting approval for the court, SJO users only")
    private int pendingJurors;

}
