package uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.time.LocalTime;
import java.util.List;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Attendance details response DTO")
public class AttendanceDetailsResponse {

    @JsonProperty("details")
    @Schema(description = "List of Juror appearance details")
    private List<Details> details;

    @JsonProperty("summary")
    @Schema(description = "Summary of attendance details")
    private Summary summary;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    @Schema(description = "Attendance details")
    public static class Details {
        @JsonProperty("juror_number")
        @Pattern(regexp = JUROR_NUMBER)
        @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty
        private String jurorNumber;

        @JsonProperty("first_name")
        @NotEmpty
        @Pattern(regexp = NO_PIPES_REGEX)
        @Length(max = 20)
        @Schema(description = "Juror first name")
        private String firstName;

        @JsonProperty("last_name")
        @NotEmpty
        @Length(max = 20)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror last name")
        private String lastName;

        @JsonProperty("juror_status")
        @NotNull
        @Schema(description = "Juror status")
        private Integer jurorStatus;

        @JsonProperty("check_in_time")
        @Schema(description = "Check in time of the juror", implementation = String.class, pattern = "HH24:mm")
        private LocalTime checkInTime;

        @JsonProperty("check_out_time")
        @Schema(description = "Check out time of the juror", implementation = String.class, pattern = "HH24:mm")
        private LocalTime checkOutTime;

        @JsonProperty("isNoShow")
        @Schema(description = "Flag to indicate if juror was failed to attend (no-show)")
        private Boolean isNoShow;

        @JsonProperty("appStage")
        @Schema(description = "Stage in the attendance journey, e.g. 1=Checked_in, 2=Checked_out")
        private AppearanceStage appearanceStage;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    @Schema(description = "Summary details")
    public static class Summary {
        @JsonProperty("checkedIn")
        @Schema(description = "Count of checked-in jurors")
        private long checkedIn;

        @JsonProperty("checkedOut")
        @Schema(description = "Count of checked-out jurors")
        private long checkedOut;

        @JsonProperty("checkedInAndOut")
        @Schema(description = "Count of checked-in and checked-out jurors")
        private long checkedInAndOut;

        @JsonProperty("panelled")
        @Schema(description = "Count of panelled jurors")
        private long panelled;

        @JsonProperty("absent")
        @Schema(description = "Count of jurors due to attend but were not checked in")
        private long absent;

        @JsonProperty("deleted")
        @Schema(description = "Count of juror attendance records deleted")
        private long deleted;

        @JsonProperty("additionalInformation")
        @Schema(description = "Additional information provided by the api")
        @Size(max = 100)
        private String additionalInformation;
    }
}
