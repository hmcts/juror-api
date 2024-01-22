package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Response DTO for listing pool history relating to a specific pool request.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of pool history relating to a specific pool request")
public class PoolHistoryListDto {

    @JsonProperty("poolHistoryEvents")
    @Schema(description = "List of pool history events on selected pool request")
    private List<PoolHistoryListDto.PoolHistoryDataDto> data;


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "Pool History data")
    @ToString
    public static class PoolHistoryDataDto {

        @JsonProperty("owner")
        @Schema(description = "owner")
        private String owner;

        @JsonProperty("poolNumber")
        @Schema(name = "Pool number", description = "The unique number for a pool request")
        private String poolNumber;

        @JsonProperty("datePart")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)
        @Schema(name = "History Date", description = "The history date recorded")
        private LocalDateTime historyDate;

        @JsonProperty("historyCode")
        @Schema(name = "History Code", description = "The ID for a type of history event on a pool request")
        @Enumerated(EnumType.STRING)
        private HistoryCode historyCode;

        @JsonProperty("userId")
        @Schema(name = "User Id", description = "The ID of the current user")
        private String userId;

        @JsonProperty("otherInformation")
        @Schema(name = "Other Information", description = "Additional information relating to the history event")
        private String otherInformation;

        @JsonProperty("historyDescription")
        @Schema(name = "history description", description = "The description of the history code")
        private String historyDescription;

        /**
         * Initialise an instance of this DTO class using a PoolHistory.PoolHistoryId object to populate its properties
         *
         * @param poolHistory an object representation of a ContactLog record from the database
         */
        public PoolHistoryDataDto(PoolHistory poolHistory, String historyDescription) {
            this.poolNumber = poolHistory.getPoolNumber();
            this.historyCode = poolHistory.getHistoryCode();
            this.historyDate = poolHistory.getHistoryDate();
            this.userId = poolHistory.getUserId();
            this.otherInformation = poolHistory.getOtherInformation();
            this.historyDescription = historyDescription;
        }
    }
}
