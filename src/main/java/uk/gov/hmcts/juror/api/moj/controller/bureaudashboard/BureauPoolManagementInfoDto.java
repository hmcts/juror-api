package uk.gov.hmcts.juror.api.moj.controller.bureaudashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Bureau pool management information DTO")
public class BureauPoolManagementInfoDto {

   @Schema(description = "Number of pools that have not yet been summoned")
   private int poolsNotYetSummoned;

   @Schema(description = "Number of pools that are transferring next week")
   private int poolsTransferringNextWeek;

   @Schema(description = "Number of deferred jurors with start date next week")
   private int deferredJurorsWithStartDateNextWeek;

}
