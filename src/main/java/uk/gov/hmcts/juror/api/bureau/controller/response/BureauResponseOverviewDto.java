package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Dto for returning overview of responses assigned to an officer")
public class BureauResponseOverviewDto {

    @Schema(description = "Count of urgent responses not processed")
    private Long urgentsCount;
    @Schema(description = "Count of non-urgent responses in a pending state")
    private Long pendingCount;
    @Schema(description = "Count of non-urgent responses in to-do state")
    private Long todoCount;
}
