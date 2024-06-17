package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing juror details for failed to attend list")
public class FailedToAttendListResponse {

    @JsonProperty("juror_number")
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jurorNumber;

    @JsonProperty("pool_number")
    @Schema(description = "Pool number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String poolNumber;

    @JsonProperty("first_name")
    @Schema(description = "First name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @JsonProperty("last_name")
    @Schema(description = "Last name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @JsonProperty("postcode")
    @Schema(description = "Postcode", requiredMode = Schema.RequiredMode.REQUIRED)
    private String postcode;

}
