package uk.gov.hmcts.juror.api.moj.controller.courtdashboard;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for returning Court attendance information.
 */
@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Court attendance information DTO")
public class CourtAttendanceInfoDto {

    @Schema(description = "Attendance statistics for jurors at the court today")
    private AttendanceStatsToday attendanceStatsToday;

    @Schema(description = "Attendance statistics for jurors at the court for the last seven days")
    private AttendanceStatsLastSevenDays attendanceStatsLastSevenDays;

    @Schema(description = "Total number of jurors due to attend at the court")
    private int totalDueToAttend;

    @Schema(description = "Number of jurors with reasonable adjustments at the court")
    private int reasonableAdjustments;

    @Schema(description = "Number of jurors with unconfirmed attendances at the court")
    private int unconfirmedAttendances;

    @Schema(description = "Number of jurors on call at the court")
    private int jurorsOnCall;

    @Builder
    @Setter
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AttendanceStatsToday {

        @Schema(description = "Number of jurors expected to attend at the court")
        private int expected;

        @Schema(description = "Number of jurors who have checked in at the court today")
        private int checkedIn;

        @Schema(description = "Number of jurors who have checked out at the court today")
        private int checkedOut;

        @Schema(description = "Number of jurors on trials at the court")
        private int onTrials;

        @Schema(description = "Number of jurors who have not checked in at the court")
        private int notCheckedIn;

    }

    @Builder
    @Setter
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AttendanceStatsLastSevenDays {

        @Schema(description = "Number of jurors expected to attend at the court")
        private int expected;

        @Schema(description = "Number of jurors who have attended the court")
        private int attended;

        @Schema(description = "Number of jurors on trials at the court")
        private int onTrials;

        @Schema(description = "Number of jurors who were absent from the court")
        private int absent;

    }
}
