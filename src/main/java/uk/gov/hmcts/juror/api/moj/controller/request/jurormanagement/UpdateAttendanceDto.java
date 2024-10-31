package uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.UpdateAttendanceStatus;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Update attendance (appearance) details")
public class UpdateAttendanceDto {
    @JsonProperty("commonData")
    @Valid
    @Schema(description = "Common data relating to updating attendance details")
    private CommonData commonData;

    @JsonProperty("juror")
    @Schema(description = "List of (one or more) jurors to update attendance record of")
    private List<String> juror;

    @JsonProperty("trial_number")
    @Schema(description = "Trial number of the jurors that attendance is being updated for, where applicable")
    @Length(max = 16)
    private String trialNumber;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Schema(description = "Common data relating to updating attendance details")
    public static class CommonData {
        @JsonProperty("status")
        @NotNull(message = "status is mandatory")
        @Schema(description = "Enum values for attendance status, required for updating attendance details, e.g."
            + "CHECK_IN, "
            + "CHECK_OUT, "
            + "CONFIRM_ATTENDANCE")
        private UpdateAttendanceStatus status;

        @JsonProperty("attendanceDate")
        @NotNull(message = "attendanceDate is mandatory")
        @Schema(description = "Attendance date for jury service")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate attendanceDate;

        @JsonProperty("locationCode")
        @Size(min = 3, max = 3)
        @NumericString
        @Schema(description = "Unique 3 digit code to identify a court location")
        private String locationCode;

        @JsonProperty("checkInTime")
        @Schema(description = "Check in time of the juror", implementation = String.class, pattern = "HH24:mm")
        private LocalTime checkInTime;

        @JsonProperty("checkOutTime")
        @Schema(description = "Check out time of the juror", implementation = String.class, pattern = "HH24:mm")
        private LocalTime checkOutTime;

        @JsonProperty("singleJuror")
        @Schema(description = "Boolean flag to indicate if a single juror is to be updated")
        @NotNull
        private Boolean singleJuror;
    }

    @JsonProperty("juror_in_waiting")
    private boolean jurorInWaiting;
}
