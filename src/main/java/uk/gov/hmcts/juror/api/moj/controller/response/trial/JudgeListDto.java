package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "A List of judges for a court location")
public class JudgeListDto {

    @JsonProperty("judges")
    private List<JudgeDto> judges;
}
