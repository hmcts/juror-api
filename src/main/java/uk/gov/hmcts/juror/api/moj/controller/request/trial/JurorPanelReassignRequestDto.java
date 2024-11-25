package uk.gov.hmcts.juror.api.moj.controller.request.trial;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "The details containing the panel jurors to be moved")
public class JurorPanelReassignRequestDto {

    @NotNull
    @Schema(description = "A list of panel jurors to be moved")
    private List<String> jurors;

    @NotBlank
    private String sourceTrialNumber;

    @NotBlank
    private String sourceTrialLocCode;

    @NotBlank
    private String targetTrialNumber;

    @NotBlank
    private String targetTrialLocCode;

}
