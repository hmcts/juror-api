package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Confirmation of trial creation")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TrialSummaryDto {

    private String trialNumber;

    private String defendants;

    private String trialType;

    private JudgeDto judge;

    private CourtroomsDto courtroomsDto;

    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate trialStartDate;

    private Boolean protectedTrial;

    private Boolean isActive;

    private Boolean isJuryEmpanelled;

    private LocalDate trialEndDate;

    private String courtRoomLocationName;
}
