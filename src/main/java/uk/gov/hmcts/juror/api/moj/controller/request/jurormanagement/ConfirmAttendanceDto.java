package uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Confirm attendance (appearance) details for a single juror")
public class ConfirmAttendanceDto {

    @JsonProperty("juror_number")
    @JurorNumber
    @NotBlank
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jurorNumber;

    @JsonProperty("attendanceDate")
    @NotNull(message = "attendanceDate is mandatory")
    @Schema(description = "Attendance date for jury service")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;

    @JsonProperty("locationCode")
    @Size(min = 3, max = 3)
    @NotBlank
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locationCode;

    @JsonProperty("checkInTime")
    @NotNull(message = "Check in time is mandatory")
    @Schema(description = "Check in time of the juror", implementation = String.class, pattern = "HH24:mm")
    private LocalTime checkInTime;

    @JsonProperty("checkOutTime")
    @NotNull(message = "Check out time is mandatory")
    @Schema(description = "Check out time of the juror", implementation = String.class, pattern = "HH24:mm")
    private LocalTime checkOutTime;

}