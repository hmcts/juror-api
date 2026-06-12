package uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.controller.response.AgeDisqualifiedJurorDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Result of Reassigning pool member(s)")
public class ReassignPoolMembersResultDto implements Serializable {

    @Schema(name = "numberReassigned", description = "The number of jurors successfully reassigned to the new pool")
    private int numberReassigned;

    @Schema(name = "newPoolNumber", description = "The new pool number jurors have been reassigned to")
    String newPoolNumber;

    @Schema(description = "Jurors disqualified due to age (will be 76+ on service start date)")
    @Builder.Default
    private List<AgeDisqualifiedJurorDto> ageDisqualified = new ArrayList<>();

}
