package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "the details of the juror to be empanelled")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EmpanelDetailsDto {

    @JurorNumber
    @Schema(name = "Juror number", description = "9 digit numeric string to uniquely identify a juror")
    private String jurorNumber;

    @NotEmpty
    @Length(max = 20)
    @Schema(description = "Juror first name")
    private String firstName;

    @NotEmpty
    @Length(max = 25)
    @Schema(description = "Juror last name")
    private String lastName;

    @Schema(name = "Juror status", description = "A status representing the juror e.g. Panelled")
    private String status;

}
