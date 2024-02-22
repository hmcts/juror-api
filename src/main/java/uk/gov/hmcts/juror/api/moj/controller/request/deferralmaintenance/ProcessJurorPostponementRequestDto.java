package uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralReasonRequestDto;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Payload for processing a postponement request.  Note: A postponement is a type of deferral")
public class ProcessJurorPostponementRequestDto extends DeferralReasonRequestDto {
    @JsonProperty("juror_numbers")
    @NotEmpty(message = "Must have at lease one juror to postpone")
    @Schema(description = "List of juror numbers to be postponed", requiredMode = Schema.RequiredMode.REQUIRED)
    public List<String> jurorNumbers;
}
