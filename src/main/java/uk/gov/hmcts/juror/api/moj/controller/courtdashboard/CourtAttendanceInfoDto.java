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
    private AttendanceStatsToday attendanceStatsToday;

    @JsonProperty("attendance_stats_last_seven_days")
    @Schema(description = "Attendance statistics for jurors at the court for the last seven days")
    private AttendanceStatsLastSevenDays attendanceStatsLastSevenDays;

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
    @Setter
    @Getter
    public static class AttendanceStatsToday {

        @JsonProperty("expected")
        @Schema(description = "Number of jurors expected to attend at the court")
        private int expected;

        @JsonProperty("checked_in")
        @Schema(description = "Number of jurors who have checked in at the court today")
        private int checkedIn;

        @JsonProperty("checked_out")
        @Schema(description = "Number of jurors who have checked out at the court today")
        private int checkedOut;

        @JsonProperty("on_trials")
        @Schema(description = "Number of jurors on trials at the court")
        private int onTrials;

        @JsonProperty("not_checked_in")
        @Schema(description = "Number of jurors who have not checked in at the court")
        private int notCheckedIn;

    }

    @Builder
    @Setter
    @Getter
    public static class AttendanceStatsLastSevenDays {

        @JsonProperty("expected")
        @Schema(description = "Number of jurors expected to attend at the court")
        private int expected;

        @JsonProperty("attended")
        @Schema(description = "Number of jurors who have attended the court")
        private int attended;

        @JsonProperty("on_trials")
        @Schema(description = "Number of jurors on trials at the court")
        private int onTrials;

        @JsonProperty("absent")
        @Schema(description = "Number of jurors who were absent from the court")
        private int absent;

    }
}
