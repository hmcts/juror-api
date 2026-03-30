package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Valid
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JurorStatusValidationResponseDto extends JurorValidationResponseDto {
    @NotNull
    @Min(0)
    @Max(11)
    @JsonProperty("status")
    private Integer status;
}
