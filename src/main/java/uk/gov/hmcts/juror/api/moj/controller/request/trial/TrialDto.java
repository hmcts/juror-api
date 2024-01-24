package uk.gov.hmcts.juror.api.moj.controller.request.trial;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Information relating to a trial")
public class TrialDto {
    @JsonProperty("case_number")
    @Schema(description = "A unique number assigned to a trial", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 16, message = "Case number must have at least 1 alphanumeric value and maximum of 16")
    @NotBlank
    private String caseNumber;

    @JsonProperty("trial_type")
    @Schema(description = "The type of trial, for example criminal or civil",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private TrialType trialType;

    @JsonProperty("defendant")
    @Schema(description = "Depending on the trial type, a defendant is one of two types, Defendant or Respondent.  "
        + "The data captured for either is the same, free text", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 16, message = "Defendant must have at least 1 alphanumeric value and maximum of 16")
    @NotBlank
    private String defendant;

    @JsonProperty("start_date")
    @Schema(description = "The date the trial starts")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;

    @JsonProperty("judge_id")
    @Schema(description = "The id of the judge sitting the trial")
    @NotNull(message = "Judge cannot be null")
    private Long judgeId;

    @JsonProperty("court_location")
    @Schema(description = "The court in which the trial is to be held")
    @NotBlank
    private String courtLocation;

    @JsonProperty("courtroom_id")
    @Schema(description = "The courtroom, within the court, in which the trial is to be held")
    @NotNull(message = "Courtroom cannot be null")
    private Long courtroomId;

    @JsonProperty("protected_trial")
    @Schema(description = "A flag to indicate whether jurors are to be anonymous (protected) or not")
    private boolean protectedTrial;
}
