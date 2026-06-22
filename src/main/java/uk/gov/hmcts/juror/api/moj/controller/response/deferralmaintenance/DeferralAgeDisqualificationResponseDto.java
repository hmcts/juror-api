package uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.controller.response.AgeDisqualifiedJurorDto;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@Builder
@Schema(description = "Response DTO containing eligible count and age-disqualified juror details "
    + "for a deferral operation")
public class DeferralAgeDisqualificationResponseDto {

    @Schema(description = "Count of jurors successfully processed")
    @Builder.Default
    private int eligible = 0;

    @Schema(description = "Jurors disqualified due to age (will be 76+ on service start date)")
    @Builder.Default
    private List<AgeDisqualifiedJurorDto> ageDisqualified = new ArrayList<>();

}
