package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "A List of trials for issuing exemptions")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JurorForExemptionListDto {

    private String jurorNumber;

    private String firstName;

    private String lastName;

    private LocalDate dateEmpanelled;
}
