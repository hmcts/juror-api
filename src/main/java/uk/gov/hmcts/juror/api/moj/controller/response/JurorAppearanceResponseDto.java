package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
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
@Schema(description = "Juror appearance response DTO")
public class JurorAppearanceResponseDto {

    @JsonProperty("juror_appearance_response_data")
    @Schema(description = "List of Juror appearance records")
    public List<JurorAppearanceResponseDto.JurorAppearanceResponseData> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    @Schema(description = "Juror appearance data")
    public static class JurorAppearanceResponseData {
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

        @JsonProperty("noShow")
        @Schema(description = "Flag to indicate if juror failed to attend")
        private Boolean noShow;

        @JsonProperty("appStage")
        @Schema(description = "Stage in the attendance journey, e.g. 1=Checked_in, 2=Checked_out")
        private AppearanceStage appStage;

        @JsonProperty("police_check")
        @Schema(description = "Police check details")
        private PoliceCheck policeCheck;

        @JsonProperty("appearance_confirmed")
        @Schema(description = "Flag to indicate if juror appearance has been confirmed for the day")
        private Boolean appearanceConfirmed;
    }
}
