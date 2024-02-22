package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApproveExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PendingApproval extends JurorNumberAndPoolNumberDto {


    private String firstName;

    private String lastName;

    private BigDecimal amountDue;


    private ExpenseType expenseType;

    private boolean canApprove;

    @JsonProperty("revisions")
    private List<ApproveExpenseDto.DateToRevision> dateToRevisions;
}
