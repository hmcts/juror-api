package uk.gov.hmcts.juror.api.moj.controller.response.juror;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class JurorHistoryResponseDto {
    @JsonProperty("data")
    @Schema(description = "History entries")
    public List<JurorHistoryEntryDto> data;

    @Builder
    @ToString
    public static class JurorHistoryEntryDto {
        @JsonProperty("description")
        @Schema(description = "Entry description")
        String description;

        @JsonProperty("username")
        @Schema(description = "User who took the action")
        String username;

        @JsonProperty("date_created")
        @Schema(description = "Time of event")
        @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
        LocalDateTime dateCreated;

        @JsonProperty("pool_number")
        @Schema(description = "Relevant pool number for this event")
        String poolNumber;

        @JsonProperty("history_details")
        @Schema(description = "History details")
        List<String> details;
    }

}
