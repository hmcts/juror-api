package uk.gov.hmcts.juror.api.moj.controller.bureaudashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Bureau notification management information DTO")
public class BureauNotificationManagementInfoDto {

    @Schema(description = "Number of open summons replies less than 4 weeks to start date for jurors owned by the bureau")
    private int fourWeeksSummonsReplies;


}
