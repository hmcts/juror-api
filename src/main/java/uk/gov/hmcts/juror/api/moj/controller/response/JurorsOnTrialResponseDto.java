package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JurorsOnTrialResponseDto {


    private List<JurorsOnTrialResponseData> trialsList;

    @Builder
    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class JurorsOnTrialResponseData  {

        private String trialNumber;

        private String parties;

        private String trialType;

        private String judge;

        private String courtroom;

        private long numberAttended;

        private long totalJurors;

        private String attendanceAudit;
    }
}
