package uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Request DTO to bulk disqualify jurors due to age")
public class BulkDisqualifyRequestDto {

    @JsonProperty("jurorNumbers")
    @Schema(description = "List of juror numbers to disqualify for age")
    @NotEmpty
    private List<String> jurorNumbers;
}
