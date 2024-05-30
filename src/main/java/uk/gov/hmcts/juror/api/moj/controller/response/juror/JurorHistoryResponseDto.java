package uk.gov.hmcts.juror.api.moj.controller.response.juror;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class JurorHistoryResponseDto {
    @JsonProperty("title")
    @Schema(description = "Entry title")
    String title;

    @JsonProperty("username")
    @Schema(description = "User who took the action")
    String username;

    @JsonProperty("date_created")
    @Schema(description = "Time of event")
    LocalDateTime dateCreated;


}
