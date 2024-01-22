package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;

import java.math.BigDecimal;
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

    @JsonProperty("absences")
    @Schema(description = "Number of days juror was absent but expected in court")
    private int absences;

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
        private BigDecimal travelTime;

        @JsonProperty("attendance_type")
        @Schema(description = "Full/half day or absent")
        private AttendanceType attendanceType;
    }
}