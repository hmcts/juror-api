package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@Builder
@Schema(description = "Details of a juror disqualified due to age")
public class AgeDisqualifiedJurorDto {

    @Schema(description = "9-digit juror number")
    private String jurorNumber;

    @Schema(description = "Juror date of birth")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dob;

    @Schema(description = "The service start date of the pool the juror is currently assigned to")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate currentServiceStartDate;

    @Schema(description = "The service start date of the pool the juror was being reassigned to")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate newDate;
}
