package uk.gov.hmcts.juror.api.bureau.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.NullOrNonBlank;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.util.List;

/**
 * DTO for the JDB-1971 search functionality
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Request body for search")
public class JurorResponseSearchRequest {

    @NumericString
    @Schema(description = "Juror number - must be a full and exact match. Case insensitive.", example = "644892530")
    private String jurorNumber;

    /**
     * Changed for JDB-3192: Exact Match.
     */
    @NullOrNonBlank
    @Schema(description = "Juror last name -  must be a full and exact match . Case insensitive.", example = "smit")
    private String lastName;

    /**
     * Changed for JDB-3192: Exact Match.
     */
    @NullOrNonBlank
    @Schema(description = "Juror postcode -  must be a full and exact match. Case insensitive.", example = "LL23")
    private String postCode;

    @NumericString
    @Schema(description = "Juror pool number - must be a full and exact match", example = "644")
    private String poolNumber;

    @Schema(description = "Only return urgent and super-urgent responses (team leaders only)")
    private Boolean urgentsOnly;

    @NullOrNonBlank
    @Schema(description = "Login of the staff member assigned to the response (team leaders only)", example = "jpowers")
    private String staffAssigned;

    @NullOrNonBlank
    @Schema(description = "Processing status of the response (team leaders only)", example = "CLOSED")
    private List<String> status;

    /**
     * Added for JDB-3192: Exact Match.
     */

    @NumericString
    @Schema(description = "Court code - must be a full and exact match.", example = "555")
    private String courtCode;

}
