package uk.gov.hmcts.juror.api.moj.controller.response.expense;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SummaryExpenseDetailsDto {
    BigDecimal totalDraft;

    BigDecimal totalForApproval;

    BigDecimal totalApproved;

    BigDecimal totalSmartCard;

    public void addToTotalDraft(BigDecimal totalDraft) {
        this.totalDraft = this.totalDraft.add(totalDraft);
    }

    public void addToTotalForApproval(BigDecimal totalForApproval) {
        this.totalForApproval = this.totalForApproval.add(totalForApproval);
    }

    public void addToTotalApproved(BigDecimal totalApproved) {
        this.totalApproved = this.totalApproved.add(totalApproved);
    }

    public void addToTotalSmartCard(BigDecimal totalSmartCard) {
        this.totalSmartCard = this.totalSmartCard.add(totalSmartCard);
    }
}
