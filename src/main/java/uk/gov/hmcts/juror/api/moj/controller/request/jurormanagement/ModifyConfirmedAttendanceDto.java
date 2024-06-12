package uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.validation.dto.ConditionalDtoValidation;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@ConditionalDtoValidation(
    conditionalProperty = "modifyAttendanceType", values = {"ATTENDANCE"},
    requiredProperties = {"checkInTime", "checkOutTime"},
    message = "Check-in and Check-out times are required for ATTENDANCE modification type")
@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Modify a confirmed attendance (appearance) details")
public class ModifyConfirmedAttendanceDto {

    @JurorNumber
    @NotBlank(message = "juror is mandatory")
    @Schema(description = "Juror to update attendance record of")
    private String jurorNumber;

    @NotNull(message = "attendanceDate is mandatory")
    @Schema(description = "Attendance date for jury service")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;

    @NotNull(message = "modifyAttendanceType is mandatory")
    @Schema(description = "Type of modification to be made to the attendance record")
    private ModifyAttendanceType modifyAttendanceType;

    @Schema(description = "Check in time of the juror", implementation = String.class, pattern = "HH24:mm")
    private LocalTime checkInTime;

    @Schema(description = "Check out time of the juror", implementation = String.class, pattern = "HH24:mm")
    private LocalTime checkOutTime;

    public enum ModifyAttendanceType {
        ATTENDANCE,
        NON_ATTENDANCE,
        ABSENCE,
        DELETE;
    }

}