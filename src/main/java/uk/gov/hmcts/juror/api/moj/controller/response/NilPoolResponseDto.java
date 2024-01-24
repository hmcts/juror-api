package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.NumericString;

@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Nil Pool check response Dto")
public class NilPoolResponseDto {

    @JsonProperty("deferrals")
    @Schema(name = "Deferrals", description = "The total number deferrals for the court on that date")
    private int deferrals;

    @JsonProperty("poolNumber")
    @Schema(name = "Pool number", description = "The unique number for a pool request")
    private String poolNumber;

    @JsonProperty("courtCode")
    @Size(min = 3, max = 3)
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locationCode;

    @JsonProperty("locationName")
    @Schema(name = "Location name", description = "The court name")
    private String locationName;

}
