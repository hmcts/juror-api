package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.NumericString;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Juror to dismiss request DTO")
public class JurorsToDismissRequestDto {

    @NotEmpty
    @JsonProperty("pool_numbers")
    private List<@PoolNumber String> poolNumbers;

    @JsonProperty("location_code")
    @NotBlank
    @Size(min = 3, max = 3)
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locationCode;

    @JsonProperty("number_of_jurors_to_dismiss")
    @Schema(description = "The number of jurors to be selected for dismissal", example = "10",
        required = true)
    private int numberOfJurorsToDismiss;

    @JsonProperty("include_jurors_on_call")
    @Schema(description = "Flag indicating if jurors on call should be included")
    private Boolean includeOnCall;

    @JsonProperty("include_jurors_not_in_attendance")
    @Schema(description = "Flag indicating if jurors not in attendance should be included")
    private Boolean includeNotInAttendance;

}
