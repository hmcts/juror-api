package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PendingApprovalList {

    private List<PendingApproval> pendingApproval;

    private long totalPendingCash;
    private long totalPendingBacs;
}
