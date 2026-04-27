package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CombinedSimplifiedExpenseDetailDto {

    private List<SimplifiedExpenseDetailDto> expenseDetails;

    private Total total;

    public CombinedSimplifiedExpenseDetailDto() {
        this.expenseDetails = new ArrayList<>();
        this.total = new Total();
    }

    public void addSimplifiedExpenseDetailDto(SimplifiedExpenseDetailDto simplifiedExpenseDetailDto) {
        this.expenseDetails.add(simplifiedExpenseDetailDto);
        this.total.add(simplifiedExpenseDetailDto);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Total {


        private int totalAttendances;

        private BigDecimal financialLoss;

        private BigDecimal travel;

        private BigDecimal foodAndDrink;

        private BigDecimal smartcard;

        private BigDecimal totalDue;

        private BigDecimal totalPaid;

        private BigDecimal balanceToPay;


        public Total() {
            this.totalAttendances = 0;
            this.financialLoss = BigDecimal.ZERO;
            this.travel = BigDecimal.ZERO;
            this.foodAndDrink = BigDecimal.ZERO;
            this.smartcard = BigDecimal.ZERO;
            this.totalDue = BigDecimal.ZERO;
            this.totalPaid = BigDecimal.ZERO;
            this.balanceToPay = BigDecimal.ZERO;
        }

        public void add(SimplifiedExpenseDetailDto simplifiedExpenseDetailDto) {
            this.totalAttendances++;
            this.financialLoss = this.financialLoss.add(simplifiedExpenseDetailDto.getFinancialLoss());
            this.travel = this.travel.add(simplifiedExpenseDetailDto.getTravel());
            this.foodAndDrink = this.foodAndDrink.add(simplifiedExpenseDetailDto.getFoodAndDrink());
            this.smartcard = this.smartcard.add(simplifiedExpenseDetailDto.getSmartcard());
            this.totalDue = this.totalDue.add(simplifiedExpenseDetailDto.getTotalDue());
            this.totalPaid = this.totalPaid.add(simplifiedExpenseDetailDto.getTotalPaid());
            this.balanceToPay = this.balanceToPay.add(simplifiedExpenseDetailDto.getBalanceToPay());
        }
    }
}
