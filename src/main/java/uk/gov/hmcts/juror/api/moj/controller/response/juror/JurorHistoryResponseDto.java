package uk.gov.hmcts.juror.api.moj.controller.response.juror;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public class JurorHistoryResponseDto {
    @JsonProperty("data")
    @Schema(description = "History entries")
    List<JurorHistoryEntryDto> data;

    @Builder
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

        @JsonProperty("other_info")
        @Schema(description = "Other related information")
        String otherInfo;

        @JsonProperty("other_info_date")
        @Schema(description = "Other related date")
        @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
        LocalDate otherInfoDate;

        @JsonProperty("other_info_ref")
        @Schema(description = "Other related reference")
        String otherInfoRef;
    }

}
