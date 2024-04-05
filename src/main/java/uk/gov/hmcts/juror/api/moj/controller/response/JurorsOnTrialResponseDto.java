package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "A List of trials and attendances for jurors on trial")
public class JurorsOnTrialResponseDto {

    @JsonProperty("trials_list")
    private List<JurorsOnTrialResponseData> trialsList;

    @Builder
    @Getter
    @Setter
    public static class JurorsOnTrialResponseData  {
        @JsonProperty("trial_number")
        private String trialNumber;

        @JsonProperty("parties")
        private String parties;

        @JsonProperty("trial_type")
        private String trialType;

        @JsonProperty("judge")
        private String judge;

        @JsonProperty("courtroom")
        private String courtroom;

        @JsonProperty("jurors_attended")
        private long numberAttended;

        @JsonProperty("total_jurors")
        private long totalJurors;

        @JsonProperty("attendance_audit")
        private String attendanceAudit;
    }
}
