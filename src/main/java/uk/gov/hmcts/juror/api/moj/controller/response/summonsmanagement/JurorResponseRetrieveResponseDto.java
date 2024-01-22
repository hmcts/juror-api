package uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "A list of juror responses")
public class JurorResponseRetrieveResponseDto {
    @JsonProperty("record_count")
    @Schema(name = "Record count", description = "The number or responses retrieved matching the search criteria")
    private int recordCount;

    @JsonProperty("juror_response")
    @Schema(name = "Juror response", description = "List of juror response records retrieved based on search criteria")
    @Singular("jurorResponse")
    private List<JurorResponseDetails> records;

    @AllArgsConstructor
    @Schema(name = "Juror response details", description = "Details of the records retrieved based on the search "
        + "criteria")
    @Builder
    @Getter
    @Setter
    public static class JurorResponseDetails {
        @JsonProperty("juror_number")
        @Schema(description = "Juror number")
        private String jurorNumber;

        @JsonProperty("pool_number")
        @Schema(description = "Pool number")
        private String poolNumber;

        @JsonProperty("first_name")
        @Schema(description = "Juror's first name")
        private String firstName;

        @JsonProperty("last_name")
        @Schema(description = "Juror's last name")
        private String lastName;

        @JsonProperty("postcode")
        @Schema(description = "Juror's postcode")
        private String postcode;

        @JsonProperty("court_name")
        @Schema(description = "Name of court assigned to")
        private String courtName;

        @JsonProperty("officer_assigned")
        @Schema(description = "User name of officer assigned to")
        private String officerAssigned;

        @JsonProperty("reply_status")
        @Schema(description = "Reply status of response")
        private ProcessingStatus replyStatus;

        @JsonProperty("date_received")
        @Schema(description = "Date response received")
        private LocalDate dateReceived;

        @JsonProperty("reply_type")
        @Schema(description = "The reply type (Paper or Digital)")
        private String replyType;
    }
}
