package uk.gov.hmcts.juror.api.moj.controller.request.trial;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Jury details for returning jurors back to in waiting")
public class ReturnJuryDto {
    @JsonProperty("check_in")
    @Schema(description = "Check in time (formatted HH:mm 24hrs) for juror's that have not been checked in", format =
        "HH:mm", example = "09:00")
    @NotEmpty
    private String checkIn;

    @JsonProperty("check_out")
    @Schema(description = "Check out time (formatted HH:mm 24hrs) for jurors", format = "HH:mm", example = "15:00")
    private String checkOut;

    @JsonProperty("completed")
    @Schema(description = "Completion service flag for juror")
    @NotNull
    private Boolean completed;

    @JsonProperty("jurors")
    @NotNull
    @Schema(description = "A list of jurors to be returned")
    private List<JurorDetailRequestDto> jurors;

    @JsonProperty("attendance_date")
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate attendanceDate = LocalDate.now();


    @JsonSetter("attendance_date")
    public void setAttendanceDate(LocalDate date) {
        if (date != null) {
            attendanceDate = date;
        }
    }
}
