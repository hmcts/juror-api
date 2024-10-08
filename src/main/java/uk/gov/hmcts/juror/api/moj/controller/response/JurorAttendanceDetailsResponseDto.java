package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Juror attendance tab response DTO")
public class JurorAttendanceDetailsResponseDto {

    @JsonProperty("next_date")
    @Schema(description = "Next date the juror in expected in court")
    private LocalDate nextDate;

    @JsonProperty("on_call")
    @Schema(description = "Flag to indicate if the juror is on call")
    private boolean onCall;

    @JsonProperty("attendances")
    @Schema(description = "Number of days juror attended court")
    private int attendances;

    @JsonProperty("non_attendances")
    @Schema(description = "Number of days juror non attended")
    private int nonAttendances;

    @JsonProperty("absences")
    @Schema(description = "Number of days juror was absent but expected in court")
    private int absences;

    @JsonProperty("appearances")
    @Schema(description = "Number of days juror was physically in court but not necessarily confirmed")
    private int appearances;

    @JsonProperty("juror_attendance_response_data")
    @Schema(description = "List of Juror appearance records")
    private List<JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    @Schema(description = "Juror appearance data")
    public static class JurorAttendanceResponseData {

        @JsonProperty("attendance_date")
        @Schema(description = "Attendance date of the juror")
        private LocalDate attendanceDate;

        @JsonProperty("check_in_time")
        @Schema(description = "Check in time of the juror", implementation = String.class, pattern = "HH24:mm")
        private LocalTime checkInTime;

        @JsonProperty("check_out_time")
        @Schema(description = "Check out time of the juror", implementation = String.class, pattern = "HH24:mm")
        private LocalTime checkOutTime;

        @JsonProperty("hours")
        @Schema(description = "Number of hours the juror spent at court to one decimal place")
        private String hours;

        @JsonProperty("travel_time")
        @Schema(description = "Number of hours the juror spent travelling")
        private LocalTime travelTime;

        @JsonProperty("attendance_type")
        @Schema(description = "The type of attendance of the juror (includes non-attendance and absence)")
        private AttendanceType attendanceType;

        @JsonProperty("editable")
        @Schema(description = "Indicates if the current user can edit this record")
        private boolean editable;

        public JurorAttendanceResponseData(Appearance appearance) {
            this.attendanceDate = appearance.getAttendanceDate();
            this.checkInTime = appearance.getTimeIn();
            this.checkOutTime = appearance.getTimeOut();
            this.travelTime = appearance.getTravelTime();
            this.attendanceType = appearance.getAttendanceType();
            this.editable = SecurityUtil.getLocCode().equals(appearance.getLocCode());
            double hours = 0.0;
            if (this.checkOutTime != null && this.checkInTime != null) {
                hours = (double) Duration.between(this.checkInTime, this.checkOutTime).toMinutes() / 60;
            }
            this.hours = String.format("%.1f", hours);
        }
    }
}
