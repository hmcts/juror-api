package uk.gov.hmcts.juror.api.moj.controller.response.expense;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExpenseCount {
    private long totalDraft;
    private long totalForApproval;
    private long totalForReapproval;
    private long totalApproved;
}
