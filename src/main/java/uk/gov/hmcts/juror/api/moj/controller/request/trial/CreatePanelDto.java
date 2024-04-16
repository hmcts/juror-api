package uk.gov.hmcts.juror.api.moj.controller.request.trial;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "create panel request information")
public class CreatePanelDto {
    @JsonProperty("trial_number")
    @Length(max = 16)
    @NotBlank
    private String trialNumber;

    @JsonProperty("number_requested")
    @NotNull
    private int numberRequested;

    @JsonProperty("pool_numbers")
    private List<String> poolNumbers = new ArrayList<>();

    @JsonProperty("court_location_code")
    @CourtLocationCode
    @NotBlank
    private String courtLocationCode;

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
