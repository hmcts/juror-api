package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class TrialSummaryDto {

    @JsonProperty("trial_number")
    private String trialNumber;

    @JsonProperty
    private String defendants;

    @JsonProperty("trial_type")
    private String trialType;

    @JsonProperty("judge")
    private JudgeDto judge;

    @JsonProperty("courtroom")
    private CourtroomsDto courtroomsDto;

    @JsonProperty("start_date")
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate trialStartDate;

    @JsonProperty("protected")
    private Boolean protectedTrial;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_jury_empanelled")
    private Boolean isJuryEmpanelled;

    @JsonProperty("trial_end_date")
    private LocalDate trialEndDate;
}
