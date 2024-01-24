package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Edit Pool Request DTO")
public class PoolEditRequestDto implements Serializable {

    @Schema(name = "Pool number", description = "Pool Request number")
    @JsonProperty("poolNumber")
    @NotBlank
    @Size(max = 9)
    private String poolNumber;

    @Schema(name = "Number Requested", description = "Number Requested from Bureau")
    @JsonProperty("noRequested")
    private Integer noRequested;

    @Schema(name = "Total Required", description = "Total Number Required by Court")
    @JsonProperty("totalRequired")
    private Integer totalRequired;

    @Schema(name = "Reason for Change", description = "Reason for change")
    @JsonProperty("reasonForChange")
    @NotBlank
    @Size(max = 80)
    private String reasonForChange;

}