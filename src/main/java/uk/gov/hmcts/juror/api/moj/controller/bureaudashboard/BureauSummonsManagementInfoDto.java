package uk.gov.hmcts.juror.api.moj.controller.bureaudashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Bureau summons management information DTO")
public class BureauSummonsManagementInfoDto {

    @Schema(description = "Number total open summons replies for jurors owned by the bureau")
    private int total;

    @Schema(description = "Number of open (todo/awaitinginfo/urgent) summons replies for jurors owned by the bureau")
    private int summonsToProcess;

    @Schema(description = "Number of standard(todo) summons replies owned by the bureau")
    private int standard;

    @Schema(description = "Number of overdue(urgent) summons owned by the bureau")
    private int overdue;

    @Schema(description = "Number of summons replies under 4 weeks before service start date that owned by the bureau")
    private int fourWeeks;

    @Schema(description = "Number of summons replies unassigned owned by the bureau")
    private int unassigned;

    @Schema(description = "Number of summons replies assigned to a jury officer owned by the bureau")
    private int assigned;

}
