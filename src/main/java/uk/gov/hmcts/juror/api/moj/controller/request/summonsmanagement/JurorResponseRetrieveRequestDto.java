package uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

@NoArgsConstructor
@Data
public class JurorResponseRetrieveRequestDto {
    // basic search (all bureau officers)
    @JsonProperty("juror_number")
    @Pattern(regexp = JUROR_NUMBER)
    @Schema(description = "Juror number")
    private String jurorNumber;

    @JsonProperty("pool_number")
    @PoolNumber
    @Schema(description = "Pool number")
    private String poolNumber;

    @JsonProperty("last_name")
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror last name")
    private String lastName;

    // advanced search (team leaders only)
    @JsonProperty("officer_assigned")
    @Schema(description = "Name of the officer assigned to the response")
    private String officerAssigned;

    @JsonProperty("is_urgent")
    @Schema(description = "Urgency of the response")
    private Boolean isUrgent;

    @JsonProperty("processing_status")
    @Schema(description = "Processing status of the response", example = "TODO")
    private Status processingStatus;

    public enum Status {
        TODO,
        AWAITING_COURT_REPLY,
        AWAITING_CONTACT,
        AWAITING_TRANSLATION,
        COMPLETED
    }
}