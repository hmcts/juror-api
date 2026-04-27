package uk.gov.hmcts.juror.api.moj.controller.courtdashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning Court Admin information.
 */
@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Court administration information DTO")
public class CourtAdminInfoDto {

    @Schema(description = "Number of unpaid attendances for jurors at the court")
    private int unpaidAttendances;

    @Schema(description = "Oldest unpaid attendance date for jurors at the court")
    private LocalDate oldestUnpaidAttendanceDate;

    @Schema(description = "How many days old the Oldest unpaid attendance for jurors at the court")
    private long oldestUnpaidAttendanceDays;

    @Schema(description = "Juror number of the oldest unpaid attendance for jurors at the court")
    private String oldestUnpaidJurorNumber;

    @Schema(description = "Date when the utilisation report was last generated")
    private LocalDateTime utilisationReportDate;

    @Schema(description = "Utilisation percentage for the court when the utilisation report was last generated")
    private double utilisationPercentage;

}
