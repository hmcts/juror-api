package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.domain.HasTotals;

import java.math.BigDecimal;
import java.util.Optional;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@ToString(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExpenseTotal<T extends ExpenseDetailsDto> extends ExpenseValuesDto {

    private int totalDays;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal totalDue;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal totalPaid;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    private final boolean hasTotals;

    public ExpenseTotal() {
        this(false);
    }

    public ExpenseTotal(boolean hasTotals) {
        super();
        totalDays = 0;
        this.hasTotals = hasTotals;
    }

    public void add(T expenseDetailsDto) {
        totalDays++;
        lossOfEarnings = getLossOfEarnings().add(
            expenseDetailsDto.getLossOfEarnings());
        extraCare = getExtraCare().add(expenseDetailsDto.getExtraCare());
        other = getOther().add(expenseDetailsDto.getOther());
        publicTransport = getPublicTransport().add(expenseDetailsDto.getPublicTransport());
        taxi = getTaxi().add(expenseDetailsDto.getTaxi());
        motorcycle = getMotorcycle().add(expenseDetailsDto.getMotorcycle());
        car = getCar().add(expenseDetailsDto.getCar());
        bicycle = getBicycle().add(expenseDetailsDto.getBicycle());
        parking = getParking().add(expenseDetailsDto.getParking());
        foodAndDrink = getFoodAndDrink().add(expenseDetailsDto.getFoodAndDrink());
        smartCard = getSmartCard().add(expenseDetailsDto.getSmartCard());
        if (hasTotals && expenseDetailsDto instanceof HasTotals totals) {
            totalPaid = getTotalPaid().add(totals.getTotalPaid());
            totalDue = getTotalDue().add(totals.getTotalDue());
        }
    }

    @JsonProperty("total_outstanding")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public BigDecimal getTotalOutstanding() {
        if (hasTotals || totalPaid != null || totalDue != null) {
            return getTotalDue()
                .subtract(getTotalPaid());
        }
        return null;
    }

    @JsonProperty("total_due")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public BigDecimal getTotalDue() {
        if (hasTotals || totalDue != null) {
            return Optional.ofNullable(totalDue).orElse(BigDecimal.ZERO);
        }
        return null;
    }

    @JsonProperty("total_paid")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public BigDecimal getTotalPaid() {
        if (hasTotals || totalPaid != null) {
            return Optional.ofNullable(totalPaid).orElse(BigDecimal.ZERO);
        }
        return null;
    }
}
