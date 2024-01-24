package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewExpenseRequest {

    @JurorNumber
    @NotBlank
    @JsonProperty("juror_number")
    private String jurorNumber;

    @Pattern(regexp = "^F\\d+$|" + ValidationConstants.POOL_NUMBER)
    @NotBlank
    @JsonProperty("identifier")
    private String identifier;
}
