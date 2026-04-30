package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "A List of trials")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TrialListDto {

    private String trialNumber;

    private String defendants;

    private String trialType;

    private String judge;

    private String courtroom;

    private String courtLocationName;

    private String courtLocationCode;

    private LocalDate startDate;

    private Boolean isActive;
}
