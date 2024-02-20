package uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Deferral response")
public class DeferralResponseDto {
    @JsonProperty("count_jurors_postponed")
    @Schema(description = "Count of number of jurors postponed")
    private int countJurorsPostponed;
}
