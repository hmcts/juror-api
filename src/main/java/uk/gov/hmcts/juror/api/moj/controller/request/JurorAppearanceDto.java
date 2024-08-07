package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.time.LocalDate;
import java.time.LocalTime;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Juror appearance request DTO")
public class JurorAppearanceDto {

    @JsonProperty("juror_number")
    @Pattern(regexp = JUROR_NUMBER)
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String jurorNumber;

    @JsonProperty("location_code")
    @NotBlank
    @Size(min = 3, max = 3)
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locationCode;

    @JsonProperty("attendance_date")
    @NotNull
    @Schema(description = "Attendance date of the juror")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;

    @JsonProperty("check_in_time")
    @Schema(description = "Check in time of the juror", implementation = String.class, pattern = "HH24:mm")
    private LocalTime checkInTime;

    @JsonProperty("check_out_time")
    @Schema(description = "Check out time of the juror", implementation = String.class, pattern = "HH24:mm")
    private LocalTime checkOutTime;

    @JsonProperty("appearance_stage")
    @Schema(description = "The appearance stage to set. This can be CHECKED_IN or CHECKED_OUT")
    @NotNull
    private AppearanceStage appearanceStage;

    @JsonProperty("juror_in_waiting")
    private boolean jurorInWaiting;

}
