package uk.gov.hmcts.juror.api.moj.controller.request.trial;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "The list containing the details of empanelled jurors")
public class JurorListRequestDto {
    @JsonProperty("jurors")
    @NotNull
    private List<JurorDetailRequestDto> jurors;

    @JsonProperty("trial_number")
    @NotBlank
    @Schema(name = "Trial number", description = "Identification for the running trial")
    @Length(max = 16)
    private String trialNumber;

    @JsonProperty("court_location_code")
    @NotBlank
    @Schema(name = "Court location Code", description = "3-digit code representing the court location")
    private String courtLocationCode;

    @JsonProperty("number_requested")
    private int numberRequested;

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
