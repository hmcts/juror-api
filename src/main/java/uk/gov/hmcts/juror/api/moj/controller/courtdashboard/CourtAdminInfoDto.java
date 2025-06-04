package uk.gov.hmcts.juror.api.moj.controller.courtdashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Court administration information DTO")
public class CourtAdminInfoDto {

    @JsonProperty("unpaid_attendances")
    @Schema(description = "Number of unpaid attendances for jurors at the court")
    private int unpaidAttendances;

    @JsonProperty("oldest_unpaid_attendance_date")
    @Schema(description = "Oldest unpaid attendance date for jurors at the court")
    private LocalDate oldestUnpaidAttendanceDate;

    @JsonProperty("oldest_unpaid_attendance_days")
    @Schema(description = "How many days old the Oldest unpaid attendance for jurors at the court")
    private long oldestUnpaidAttendanceDays;

    @JsonProperty("oldest_unpaid_juror")
    @Schema(description = "Juror number of the oldest unpaid attendance for jurors at the court")
    private String oldestUnpaidJurorNumber;

    @JsonProperty("utilisation_report_date")
    @Schema(description = "Date when the utilisation report was last generated")
    private LocalDateTime utilisationReportDate;

}
