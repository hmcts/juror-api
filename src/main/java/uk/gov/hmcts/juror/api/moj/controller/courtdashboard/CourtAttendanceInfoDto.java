package uk.gov.hmcts.juror.api.moj.controller.courtdashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Court attendance information DTO")
public class CourtAttendanceInfoDto {

    @JsonProperty("attendance_stats_today")
    @Schema(description = "Attendance statistics for jurors at the court today")
    private AttendanceStats attendanceStatsToday;

    @JsonProperty("attendance_stats_last_seven_days")
    @Schema(description = "Attendance statistics for jurors at the court for the last seven days")
    private AttendanceStats attendanceStatsLastSevenDays;

    @JsonProperty("total_due_to_attend")
    @Schema(description = "Total number of jurors due to attend at the court")
    private int totalDueToAttend;

    @JsonProperty("reasonable_adjustments")
    @Schema(description = "Number of jurors with reasonable adjustments at the court")
    private int reasonableAdjustments;

    @JsonProperty("unconfirmed_attendances")
    @Schema(description = "Number of jurors with unconfirmed attendances at the court")
    private int unconfirmedAttendances;

    @Builder
    public static class AttendanceStats {

        @JsonProperty("expected")
        @Schema(description = "Number of jurors expected to attend at the court")
        private int expected;

        @JsonProperty("checked_in")
        @Schema(description = "Number of jurors who have checked in at the court")
        private int checkedIn;

        @JsonProperty("on_trials")
        @Schema(description = "Number of jurors currently on trials at the court")
        private int onTrials;

        @JsonProperty("not_checked_in")
        @Schema(description = "Number of jurors who have not checked in at the court")
        private int notCheckedIn;

    }
}
