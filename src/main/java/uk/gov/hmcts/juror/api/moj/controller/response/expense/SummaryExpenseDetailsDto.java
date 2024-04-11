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

    public void SummaryExpenseDetailsDto() {
        this.totalDraft = BigDecimal.ZERO;
        this.totalForApproval = BigDecimal.ZERO;
        this.totalApproved = BigDecimal.ZERO;
        this.totalSmartCard = BigDecimal.ZERO;
    }

    public void addToTotalDraft(BigDecimal draft) {
        this.totalDraft = this.totalDraft.add(draft);
    }

    public void addToTotalForApproval(BigDecimal forApproval) {
        this.totalForApproval = this.totalForApproval.add(forApproval);
    }

    public void addToTotalApproved(BigDecimal approved) {
        this.totalApproved = this.totalApproved.add(approved);
    }

    public void addToTotalSmartCard(BigDecimal totalSmartCard) {
        this.totalSmartCard = this.totalSmartCard.add(totalSmartCard);
    }
}
